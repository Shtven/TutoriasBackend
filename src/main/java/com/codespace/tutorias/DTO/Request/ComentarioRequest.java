package com.codespace.tutorias.DTO.Request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComentarioRequest(
        @NotBlank(message = "El comentario es obligatorio.")
        @Size(max = 255, message = "El comentario no puede exceder 255 caracteres.") String comentario
) {}
