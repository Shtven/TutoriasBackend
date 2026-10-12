package com.codespace.tutorias.DTO.Request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

/**
 * Login con contraseña (POST /auth/signin) por matrícula o por correo.
 * Acepta pwd, el nombre de la versión anterior.
 */
public record LoginManualRequest(
        String matricula,
        String correo,

        @JsonAlias("pwd")
        @NotBlank(message = "La contraseña es obligatoria.") String password
) {}
