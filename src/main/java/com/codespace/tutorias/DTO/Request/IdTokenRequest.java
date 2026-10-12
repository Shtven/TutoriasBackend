package com.codespace.tutorias.DTO.Request;

import jakarta.validation.constraints.NotBlank;

public record IdTokenRequest(
        @NotBlank(message = "El idToken es obligatorio.") String idToken
) {}
