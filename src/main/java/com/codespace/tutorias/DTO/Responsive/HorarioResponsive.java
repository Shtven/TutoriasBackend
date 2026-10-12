package com.codespace.tutorias.DTO.Responsive;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalTime;
import java.util.List;

/** temas solo viene en las vistas de detalle. */
public record HorarioResponsive(
        int idHorario,
        String dia,
        LocalTime horaInicio,
        LocalTime horaFin,
        String estado,
        @JsonInclude(JsonInclude.Include.NON_NULL) List<TemaResponsive> temas
) {}
