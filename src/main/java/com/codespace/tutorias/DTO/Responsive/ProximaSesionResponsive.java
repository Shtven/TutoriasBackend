package com.codespace.tutorias.DTO.Responsive;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** Próxima sesión; cada vista llena solo los campos que muestra. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProximaSesionResponsive(
        Integer idTutoria,
        MateriaResponsive materia,
        Integer idHorario,
        LocalDate fecha,
        String dia,
        LocalTime horaInicio,
        LocalTime horaFin,
        Integer edificio,
        Integer aula,
        List<String> temas,
        LocalTime recordatorioA,
        Boolean primera
) {}
