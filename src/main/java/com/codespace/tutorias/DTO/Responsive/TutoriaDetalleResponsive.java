package com.codespace.tutorias.DTO.Responsive;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

/** panelTutorado solo se llena para el rol TUTORADO. */
public record TutoriaDetalleResponsive(
        @JsonUnwrapped TutoriaResumenResponsive tutoria,
        ProximaSesionResponsive proximaSesion,
        boolean esDueno,
        PanelTutoradoResponsive panelTutorado
) {}
