package com.codespace.tutorias.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MarcaRequest(
        @NotBlank(message = "La matrícula es obligatoria.") String matricula,
        @NotNull(message = "Indica si asistió.") Boolean asistio
) {}
