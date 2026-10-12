package com.codespace.tutorias.DTO.Responsive;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record AdminTutoriaResponsive(
        @JsonUnwrapped TutoriaResumenResponsive tutoria,
        Integer asistenciaPct
) {}
