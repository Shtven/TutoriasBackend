package com.codespace.tutorias.DTO.Request;

import jakarta.validation.constraints.NotBlank;

/** Llega matricula o correo (el front manda correo si lo escrito tiene '@'). */
public record SigninRequest(
        String matricula,
        String correo,
        @NotBlank(message = "La contraseña es obligatoria.") String pwd
) {}
