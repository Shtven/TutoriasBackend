package com.codespace.tutorias.DTO.Responsive;

public record TutoradoListaResponsive(
        String matricula,
        String nombreCompleto,
        long inasistenciasPrevias,
        Boolean asistio,
        boolean causaraBaja
) {}
