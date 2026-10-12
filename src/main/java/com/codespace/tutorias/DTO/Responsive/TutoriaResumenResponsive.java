package com.codespace.tutorias.DTO.Responsive;

import java.util.List;

public record TutoriaResumenResponsive(
        int idTutoria,
        MateriaResponsive materia,
        UsuarioResumenResponsive tutor,
        int edificio,
        int aula,
        String estado,
        List<HorarioResponsive> horarios,
        CupoResponsive cupo
) {}
