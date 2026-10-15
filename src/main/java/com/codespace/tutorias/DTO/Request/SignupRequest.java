package com.codespace.tutorias.DTO.Request;

import com.codespace.tutorias.Helpers.MatriculaHelper;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Límites = columnas de usuario (001_esquema.sql). */
public record SignupRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 50, message = "El nombre no puede exceder 50 caracteres.") String nombre,

        @NotBlank(message = "Los apellidos son obligatorios.")
        @Size(max = 200, message = "Los apellidos no pueden exceder 200 caracteres.") String apellidos,

        @NotBlank(message = "La matrícula es obligatoria.")
        @Pattern(regexp = MatriculaHelper.REGEX, message = MatriculaHelper.MENSAJE_INVALIDA) String matricula,

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no es válido.")
        @Size(max = 255, message = "El correo no puede exceder 255 caracteres.") String correo,

        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres.") String pwd,

        @NotBlank(message = "El rol es obligatorio.")
        @Pattern(regexp = "(?i)TUTORADO|TUTOR", message = "El rol debe ser TUTORADO o TUTOR.") String rol
) {}
