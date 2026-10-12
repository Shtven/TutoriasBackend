package com.codespace.tutorias.DTO.Request;

import jakarta.validation.constraints.NotBlank;

public record RegistroRequest(
        @NotBlank(message = "El tokenRegistro es obligatorio.") String tokenRegistro,
        @NotBlank(message = "La matrícula es obligatoria.") String matricula,
        @NotBlank(message = "El rol es obligatorio.") String rol
) {}
