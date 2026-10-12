package com.codespace.tutorias.DTO.Request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/** Identifica una sesión (registrada o virtual) por horario + fecha. */
public record SesionRequest(
        @NotNull(message = "El horario es obligatorio.") Integer idHorario,
        @NotNull(message = "La fecha es obligatoria.") LocalDate fecha
) {}
