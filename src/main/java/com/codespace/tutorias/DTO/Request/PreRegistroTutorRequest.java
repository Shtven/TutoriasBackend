package com.codespace.tutorias.DTO.Request;

import com.codespace.tutorias.Helpers.MatriculaHelper;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** El admin autoriza a un tutor; la persona crea su contraseña con POST /auth/signup. */
public record PreRegistroTutorRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 50, message = "El nombre no puede exceder 50 caracteres.") String nombre,

        @NotBlank(message = "Los apellidos son obligatorios.")
        @Size(max = 200, message = "Los apellidos no pueden exceder 200 caracteres.") String apellidos,

        @NotBlank(message = "La matrícula es obligatoria.")
        @Pattern(regexp = MatriculaHelper.REGEX, message = MatriculaHelper.MENSAJE_INVALIDA) String matricula,

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no es válido.")
        @Size(max = 255, message = "El correo no puede exceder 255 caracteres.") String correo
) {}
