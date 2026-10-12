package com.codespace.tutorias.DTO.Responsive;

import java.util.List;

public record CierreResultadoResponsive(
        int idSesion,
        String estado,
        long asistieron,
        long noAsistieron,
        List<BajaResponsive> bajasAplicadas
) {}
