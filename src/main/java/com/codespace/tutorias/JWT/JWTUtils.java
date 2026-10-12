package com.codespace.tutorias.JWT;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

/**
 * JWT propios del backend (HS256). Hay dos tipos y no son intercambiables:
 * <ul>
 *   <li>SESION: se usa en Authorization para toda la API.</li>
 *   <li>REGISTRO: lleva la identidad ya verificada por el proveedor durante
 *       el primer acceso; solo lo acepta POST /auth/registro.</li>
 * </ul>
 */
@Component
public class JWTUtils {

    public static final String TIPO_SESION = "SESION";
    public static final String TIPO_REGISTRO = "REGISTRO";

    @Value("${api.key}")
    private String apiKey;

    @Value("${api.jwt.expiration}")
    private long expiration;

    @Value("${auth.registro.expiracion-minutos:15}")
    private long minutosRegistro;

    public String generarToken(String matricula, String rol) {
        return Jwts.builder()
                .setSubject(matricula)
                .claim("rol", rol)
                .claim("tipo", TIPO_SESION)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(llave(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generarTokenRegistro(IdentidadVerificada identidad) {
        return Jwts.builder()
                .setSubject(identidad.idExterno())
                .claim("tipo", TIPO_REGISTRO)
                .claim("proveedor", identidad.proveedor())
                .claim("correo", identidad.correo())
                .claim("nombre", identidad.nombre())
                .claim("apellidos", identidad.apellidos())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + minutosRegistro * 60_000))
                .signWith(llave(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Valida firma y expiración. Lanza JwtException si no es válido. */
    public Claims leer(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(llave())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key llave() {
        return Keys.hmacShaKeyFor(apiKey.getBytes(StandardCharsets.UTF_8));
    }
}
