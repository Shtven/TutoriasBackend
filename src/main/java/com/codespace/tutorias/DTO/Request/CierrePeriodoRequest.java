package com.codespace.tutorias.DTO.Request;

import jakarta.validation.constraints.NotNull;

public record CierrePeriodoRequest(
        @NotNull(message = "Confirma el cierre del periodo.") Boolean confirmar
) {}
