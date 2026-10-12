package com.codespace.tutorias.DTO.Responsive;

public record AdminResumenResponsive(
        long tutoriasActivas,
        long inscripcionesActivas,
        long lugaresDisponibles,
        long lugaresTotales,
        long bajasAutomaticas
) {}
