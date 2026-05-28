package com.codespace.tutorias.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    // Lista separada por comas en .env / application.properties.
    // Spring inyecta cada elemento como un item de la lista.
    @Value("${cors.allowed-origins}")
    private List<String> allowedOrigins;

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        // 🌐 Origenes exactos (dominio fijo). Con allowCredentials=true no se
        // admite "*", por eso se listan los dominios permitidos.
        config.setAllowedOrigins(
                allowedOrigins.stream().map(String::trim).toList()
        );

        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
