package com.codespace.tutorias.DTO.Request;

import jakarta.validation.constraints.NotBlank;

public record TemaRequest(
        @NotBlank(message = "El tema es obligatorio.") String tema
) {}
