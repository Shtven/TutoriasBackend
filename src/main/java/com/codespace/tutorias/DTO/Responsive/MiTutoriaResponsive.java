package com.codespace.tutorias.DTO.Responsive;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record MiTutoriaResponsive(
        @JsonUnwrapped TutoriaResumenResponsive tutoria,
        ProximaSesionResponsive proximaSesion,
        long tutoradosEnRiesgo
) {}
