package com.codespace.tutorias.DTO.Responsive;

import java.time.LocalTime;

public record SemanaItemResponsive(String dia, LocalTime horaInicio, LocalTime horaFin, int idTutoria, MateriaResponsive materia) {}
