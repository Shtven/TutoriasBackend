package com.codespace.tutorias.DTO.Responsive;

public record InscripcionAnteriorResponsive(
        TutoriaResumenResponsive tutoria,
        InscripcionInfoResponsive inscripcion,
        long inasistencias
) {}
