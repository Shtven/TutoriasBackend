package com.codespace.tutorias.DTO.Responsive;

import java.time.LocalDate;
import java.util.List;

public record SesionesHoyResponsive(
        LocalDate fecha,
        List<SesionResponsive> sesiones,
        List<SesionResponsive> listasSinCerrar
) {}
