package com.codespace.tutorias.DTO.Responsive;

public record MiResumenAsistenciaResponsive(
        long inasistencias,
        int inasistenciasPermitidas,
        long sesionesConAsistencia,
        long sesionesCanceladas
) {}
