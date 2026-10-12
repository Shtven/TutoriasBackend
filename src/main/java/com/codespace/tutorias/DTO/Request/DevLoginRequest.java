package com.codespace.tutorias.DTO.Request;

import jakarta.validation.constraints.NotBlank;

public record DevLoginRequest(
        @NotBlank(message = "El correo es obligatorio.") String correo
) {}
