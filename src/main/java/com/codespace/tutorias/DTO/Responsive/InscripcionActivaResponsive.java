package com.codespace.tutorias.DTO.Responsive;

public record InscripcionActivaResponsive(
        TutoriaResumenResponsive tutoria,
        InscripcionInfoResponsive inscripcion,
        long inasistencias,
        int inasistenciasPermitidas,
        ProximaSesionResponsive proximaSesion
) {}
