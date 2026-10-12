package com.codespace.tutorias.DTO.Responsive;

public record ResumenHistorialResponsive(
        long sesionesCompletadas,
        long sesionesCanceladas,
        long marcasAsistio,
        long marcasTotales,
        Integer asistenciaPct,
        long bajasAutomaticas
) {}
