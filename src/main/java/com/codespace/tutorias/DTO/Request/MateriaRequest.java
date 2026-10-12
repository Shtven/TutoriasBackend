package com.codespace.tutorias.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record MateriaRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres.") String nombre,
        @NotNull(message = "El NRC es obligatorio.")
        @Positive(message = "El NRC debe ser un número positivo.") Integer nrc
) {}
