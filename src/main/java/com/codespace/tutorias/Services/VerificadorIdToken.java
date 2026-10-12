package com.codespace.tutorias.Services;

import com.codespace.tutorias.Exceptions.BusinessException;
import com.codespace.tutorias.Helpers.Proveedores;
import com.codespace.tutorias.JWT.IdentidadVerificada;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Verifica ID tokens de Microsoft Entra ID y Google con las llaves públicas
 * (JWKS) de cada proveedor: firma, iss, aud, exp y dominio permitido.
 * Ver docs/AUTENTICACION.md.
 */
@Service
public class VerificadorIdToken {

    private static final Logger log = LoggerFactory.getLogger(VerificadorIdToken.class);

    private static final String GOOGLE_JWKS = "https://www.googleapis.com/oauth2/v3/certs";
    private static final Set<String> GOOGLE_ISSUERS = Set.of("https://accounts.google.com", "accounts.google.com");

    @Value("${auth.google.client-id:}")
    private String googleClientId;

    @Value("${auth.google.dominio}")
    private String googleDominio;

    @Value("${auth.microsoft.client-id:}")
    private String microsoftClientId;

    @Value("${auth.microsoft.tenant-id:}")
    private String microsoftTenantId;

    @Value("${auth.microsoft.dominio}")
    private String microsoftDominio;

    private volatile JwtDecoder decoderGoogle;
    private volatile JwtDecoder decoderMicrosoft;

    public IdentidadVerificada verificarGoogle(String idToken) {
        if (googleClientId.isBlank()) {
            throw new BusinessException("El acceso con Google no está configurado.");
        }

        Jwt jwt = decodificar(decoderGoogle(), idToken);

        String correo = texto(jwt, "email");
        boolean verificado = Boolean.parseBoolean(String.valueOf(jwt.getClaims().get("email_verified")));
        if (correo == null || !verificado || !terminaEn(correo, googleDominio)) {
            throw new BusinessException("Solo se permiten cuentas @" + googleDominio + ".");
        }

        return identidad(Proveedores.GOOGLE, jwt, correo);
    }

    public IdentidadVerificada verificarMicrosoft(String idToken) {
        if (microsoftClientId.isBlank() || microsoftTenantId.isBlank()) {
            throw new BusinessException("El acceso con Microsoft no está configurado.");
        }

        Jwt jwt = decodificar(decoderMicrosoft(), idToken);

        // Primero el tenant: el claim email no está verificado en apps multi-tenant.
        if (!microsoftTenantId.equalsIgnoreCase(texto(jwt, "tid"))) {
            throw new BusinessException("Solo se permiten cuentas @" + microsoftDominio + ".");
        }

        String correo = texto(jwt, "email");
        if (correo == null) correo = texto(jwt, "preferred_username");
        if (correo == null || !terminaEn(correo, microsoftDominio)) {
            throw new BusinessException("Solo se permiten cuentas @" + microsoftDominio + ".");
        }

        return identidad(Proveedores.MICROSOFT, jwt, correo);
    }

    // ------------------------------------------------------------------

    private Jwt decodificar(JwtDecoder decoder, String idToken) {
        try {
            return decoder.decode(idToken);
        } catch (JwtException e) {
            log.info("ID token rechazado: {}", e.getMessage());
            throw new BusinessException("Token del proveedor inválido o expirado.");
        }
    }

    private JwtDecoder decoderGoogle() {
        if (decoderGoogle == null) {
            NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(GOOGLE_JWKS).build();
            decoder.setJwtValidator(validador(
                    new JwtClaimValidator<Object>(JwtClaimNames.ISS, iss -> GOOGLE_ISSUERS.contains(String.valueOf(iss))),
                    googleClientId));
            decoderGoogle = decoder;
        }
        return decoderGoogle;
    }

    private JwtDecoder decoderMicrosoft() {
        if (decoderMicrosoft == null) {
            String base = "https://login.microsoftonline.com/" + microsoftTenantId;
            NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(base + "/discovery/v2.0/keys").build();
            String issuer = base + "/v2.0";
            decoder.setJwtValidator(validador(
                    new JwtClaimValidator<Object>(JwtClaimNames.ISS, iss -> issuer.equals(String.valueOf(iss))),
                    microsoftClientId));
            decoderMicrosoft = decoder;
        }
        return decoderMicrosoft;
    }

    /** exp/nbf (por defecto) + issuer + audiencia = nuestro client id. */
    private OAuth2TokenValidator<Jwt> validador(OAuth2TokenValidator<Jwt> issuer, String clientId) {
        OAuth2TokenValidator<Jwt> audiencia = new JwtClaimValidator<List<String>>(
                JwtClaimNames.AUD, aud -> aud != null && aud.contains(clientId));
        return new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefault(), issuer, audiencia);
    }

    private IdentidadVerificada identidad(String proveedor, Jwt jwt, String correo) {
        String nombre = texto(jwt, "given_name");
        String apellidos = texto(jwt, "family_name");

        if (nombre == null) {
            // Sin claims opcionales: se divide "name" (primer token = nombre).
            String completo = texto(jwt, "name");
            if (completo == null) completo = correo.substring(0, correo.indexOf('@'));
            String[] partes = completo.trim().split("\\s+", 2);
            nombre = partes[0];
            if (apellidos == null) apellidos = partes.length > 1 ? partes[1] : "";
        }
        if (apellidos == null) apellidos = "";

        return new IdentidadVerificada(
                proveedor,
                jwt.getSubject(),
                correo.trim().toLowerCase(),
                recortar(nombre.trim(), 50),
                recortar(apellidos.trim(), 200));
    }

    private static String texto(Jwt jwt, String claim) {
        Object valor = jwt.getClaims().get(claim);
        return valor == null || String.valueOf(valor).isBlank() ? null : String.valueOf(valor);
    }

    private static boolean terminaEn(String correo, String dominio) {
        return correo.toLowerCase().endsWith("@" + dominio.toLowerCase());
    }

    private static String recortar(String texto, int max) {
        return texto.length() <= max ? texto : texto.substring(0, max);
    }
}
