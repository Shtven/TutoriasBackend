package com.codespace.tutorias.DTO.Responsive;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InscripcionInfoResponsive(
        String estado,
        OffsetDateTime fechaInscripcion,
        OffsetDateTime fechaBaja,
        Boolean nueva
) {}
