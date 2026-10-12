package com.codespace.tutorias.DTO.Responsive;

import java.time.LocalDate;
import java.time.LocalTime;

/** idSesion = null en sesiones virtuales (sin fila en BD). */
public record SesionResponsive(
        Integer idSesion,
        int idHorario,
        int idTutoria,
        String materia,
        LocalDate fecha,
        String dia,
        LocalTime horaInicio,
        LocalTime horaFin,
        int edificio,
        int aula,
        String estado,
        long inscritos,
        long marcados,
        long asistieron,
        LocalTime recordatorioA,
        boolean terminada,
        AccionesSesionResponsive acciones
) {}
