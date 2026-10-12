package com.codespace.tutorias.JWT;

import com.codespace.tutorias.Exceptions.RespuestaJson;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lee el JWT de sesión y deja en la request los atributos "matricula" y
 * "rol" que consumen los controladores con @RequestAttribute.
 */
@Component
public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtils jwtUtils;

    public JWTFilter(JWTUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // El login no usa el JWT de sesión; un token viejo en el header no debe bloquearlo.
        return request.getServletPath().startsWith("/auth/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String matricula;
            String rol;

            try {
                Claims claims = jwtUtils.leer(authHeader.substring(7));
                if (!JWTUtils.TIPO_SESION.equals(claims.get("tipo", String.class))) {
                    throw new IllegalArgumentException("No es un token de sesión");
                }
                matricula = claims.getSubject();
                rol = claims.get("rol", String.class);
            } catch (Exception e) {
                RespuestaJson.escribir(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "Token inválido o expirado.");
                return;
            }

            request.setAttribute("matricula", matricula);
            request.setAttribute("rol", rol);

            Authentication auth = new UsernamePasswordAuthenticationToken(
                    matricula, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}
