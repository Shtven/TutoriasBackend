package com.codespace.tutorias.DTO.Responsive;

import java.util.List;

public record MisInscripcionesResponsive(
        ProximaSesionResponsive proximaSesion,
        List<InscripcionActivaResponsive> activas,
        List<InscripcionAnteriorResponsive> anteriores
) {}
