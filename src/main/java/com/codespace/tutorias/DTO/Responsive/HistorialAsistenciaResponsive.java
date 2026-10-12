package com.codespace.tutorias.DTO.Responsive;

import java.util.List;

public record HistorialAsistenciaResponsive(
        HistorialTutoriaResponsive tutoria,
        ResumenHistorialResponsive resumen,
        List<SesionColumnaResponsive> sesiones,
        List<FilaHistorialResponsive> tutorados
) {}
