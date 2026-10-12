package com.codespace.tutorias.DTO.Responsive;

import java.util.List;

public record MiInscripcionResponsive(
        TutoriaDetalleResponsive tutoria,
        InscripcionInfoResponsive inscripcion,
        MiResumenAsistenciaResponsive resumen,
        ProximaSesionResponsive proximaSesion,
        List<MiSesionResponsive> historial
) {}
