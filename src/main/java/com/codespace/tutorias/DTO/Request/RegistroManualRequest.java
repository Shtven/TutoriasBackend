package com.codespace.tutorias.DTO.Request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Registro con datos capturados a mano (POST /auth/signup).
 *
 * Acepta también los nombres de la versión anterior: pwd en lugar de
 * password, y apellidoP + apellidoM en lugar de apellidos.
 */
public record RegistroManualRequest(
        @NotBlank(message = "La matrícula es obligatoria.") String matricula,

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 50, message = "El nombre no puede exceder 50 caracteres.") String nombre,

        @Size(max = 200, message = "Los apellidos no pueden exceder 200 caracteres.") String apellidos,
        String apellidoP,
        String apellidoM,

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no tiene un formato válido.")
        @Size(max = 255, message = "El correo no puede exceder 255 caracteres.") String correo,

        @JsonAlias("pwd")
        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.") String password,

        @NotBlank(message = "El rol es obligatorio.") String rol
) {}
