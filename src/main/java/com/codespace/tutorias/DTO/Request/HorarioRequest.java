package com.codespace.tutorias.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.List;

/** idHorario solo al editar, para conservar un horario existente. */
public record HorarioRequest(
        Integer idHorario,
        @NotBlank(message = "El día es obligatorio.") String dia,
        @NotNull(message = "La hora de inicio es obligatoria.") LocalTime horaInicio,
        @NotNull(message = "La hora de fin es obligatoria.") LocalTime horaFin,
        List<String> temas
) {}
