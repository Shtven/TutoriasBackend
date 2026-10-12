package com.codespace.tutorias.DTO.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record MarcasRequest(
        @NotEmpty(message = "Envía al menos una marca.") List<@Valid MarcaRequest> marcas
) {}
