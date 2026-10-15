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
 * JWT de sesión del backend (HS256): sub = matrícula, rol, tipo = SESION.
 * Se usa en Authorization para toda la API.
 */
@Component
public class JWTUtils {

    public static final String TIPO_SESION = "SESION";

    @Value("${api.key}")
    private String apiKey;

    @Value("${api.jwt.expiration}")
    private long expiration;

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
