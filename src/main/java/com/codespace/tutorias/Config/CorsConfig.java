package com.codespace.tutorias.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * En producción no hay proxy: el navegador llama al backend desde el origen
 * del front (Static Site de Render). Ver docs/README.md § CORS.
 */
@Configuration
public class CorsConfig {

    // Lista separada por comas en .env / application.properties.
    // Spring inyecta cada elemento como un item de la lista.
    @Value("${cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // Patrones: un origen exacto ("https://front.onrender.com") o con
        // comodín para los PR previews ("https://front-pr-*.onrender.com").
        // Con allowCredentials=true no se admite "*" a secas. Se quitan los
        // espacios y la barra final, que el navegador nunca manda en Origin.
        config.setAllowedOriginPatterns(allowedOrigins.stream()
                .map(String::trim)
                .map(o -> o.endsWith("/") ? o.substring(0, o.length() - 1) : o)
                .filter(o -> !o.isEmpty())
                .toList());

        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
