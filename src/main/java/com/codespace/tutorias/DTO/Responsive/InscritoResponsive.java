package com.codespace.tutorias.DTO.Responsive;

public record InscritoResponsive(String matricula, String nombreCompleto, String correo, java.time.OffsetDateTime fechaInscripcion, long inasistencias, boolean enRiesgo) {}
