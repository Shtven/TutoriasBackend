package com.codespace.tutorias.DTO.Request;

import jakarta.validation.constraints.NotBlank;

public record EstadoTutoriaRequest(
        @NotBlank(message = "El estado es obligatorio.") String estado
) {}
