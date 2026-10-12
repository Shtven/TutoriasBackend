package com.codespace.tutorias.DTO.Responsive;

import java.util.List;

public record HistorialTutoriaResponsive(
        int idTutoria,
        String materia,
        int edificio,
        int aula,
        List<HorarioResponsive> horarios,
        CupoResponsive cupo
) {}
