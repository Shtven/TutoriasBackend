package com.codespace.tutorias.DTO.Responsive;

import java.time.LocalDate;
import java.time.LocalTime;

/** miAsistencia: ASISTIO | NO_ASISTIO | PENDIENTE | NO_CUENTA | SIN_MARCA. */
public record MiSesionResponsive(
        Integer idSesion,
        LocalDate fecha,
        String dia,
        LocalTime horaInicio,
        LocalTime horaFin,
        String estadoSesion,
        String miAsistencia
) {}
