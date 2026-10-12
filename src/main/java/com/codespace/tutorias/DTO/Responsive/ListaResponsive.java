package com.codespace.tutorias.DTO.Responsive;

import java.util.List;

public record ListaResponsive(
        SesionResponsive sesion,
        List<TutoradoListaResponsive> tutorados,
        ResumenListaResponsive resumen,
        CierreListaResponsive cierre
) {}
