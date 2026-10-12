package com.codespace.tutorias.DTO.Request;

import jakarta.validation.constraints.NotNull;

public record InscripcionRequest(
        @NotNull(message = "La tutoría es obligatoria.") Integer idTutoria
) {}
