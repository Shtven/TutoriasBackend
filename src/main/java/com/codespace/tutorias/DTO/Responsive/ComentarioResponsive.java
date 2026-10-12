package com.codespace.tutorias.DTO.Responsive;

import java.time.OffsetDateTime;

public record ComentarioResponsive(
        int idComentario,
        String matricula,
        String nombreCompleto,
        String comentario,
        OffsetDateTime fechaComentario,
        boolean esMio
) {}
