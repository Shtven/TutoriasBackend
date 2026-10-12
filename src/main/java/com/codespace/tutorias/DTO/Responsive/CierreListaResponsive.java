package com.codespace.tutorias.DTO.Responsive;

import java.util.List;

public record CierreListaResponsive(
        boolean todosMarcados,
        boolean sesionIniciada,
        boolean noCancelada,
        boolean puedeCerrar,
        List<BajaResponsive> bajasPrevistas
) {}
