package com.codespace.tutorias.DTO.Request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ActualizarTutoriaRequest(
        @NotNull(message = "El edificio es obligatorio.") Integer edificio,
        @NotNull(message = "El aula es obligatoria.") Integer aula,
        @NotEmpty(message = "Agrega al menos un horario.") List<@Valid HorarioRequest> horarios
) {}
