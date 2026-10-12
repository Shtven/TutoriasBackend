package com.codespace.tutorias.DTO.Responsive;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.List;

public record TutoriaExplorarResponsive(
        @JsonUnwrapped TutoriaResumenResponsive tutoria,
        List<ChoqueResponsive> choques
) {}
