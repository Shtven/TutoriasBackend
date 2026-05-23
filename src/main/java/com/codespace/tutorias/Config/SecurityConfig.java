package com.codespace.tutorias.Config;

import com.codespace.tutorias.JWT.JWTFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JWTFilter jwtFilter;

    public SecurityConfig(JWTFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                // ❌ Desactivar CSRF (API REST)
                .csrf(AbstractHttpConfigurer::disable)

                // 🌐 CORS por defecto
                .cors(Customizer.withDefaults())

                // 🔐 Sin sesiones (JWT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 🔑 Reglas de autorización
                .authorizeHttpRequests(auth -> auth

                        // 🔓 Endpoints públicos
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/materia/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/auth/newPassword").permitAll()

                        // 🔒 Endpoints protegidos
                        .requestMatchers(HttpMethod.GET, "/tutorias/**")
                        .hasAnyRole("TUTOR", "TUTORADO", "ADMIN")

                        .requestMatchers("/tutoria/**")
                        .hasAnyRole("TUTOR", "ADMIN")

                        .requestMatchers("/horario/**")
                        .hasAnyRole("TUTOR", "ADMIN")

                        .requestMatchers("/asistencia/**")
                        .hasAnyRole("TUTOR", "TUTORADO", "ADMIN")

                        .requestMatchers("/comentarios/**")
                        .hasAnyRole("TUTOR", "TUTORADO", "ADMIN")

                        .requestMatchers("/temas/**")
                        .hasAnyRole("TUTOR", "TUTORADO", "ADMIN")

                        // 🔐 Todo lo demás requiere autenticación
                        .anyRequest().authenticated()
                )

                // 🚫 Desactivar login por defecto de Spring
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                // 🧩 Filtro JWT
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}