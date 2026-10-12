package com.codespace.tutorias.DTO.Responsive;

import java.time.OffsetDateTime;
import java.util.Map;

/** marcas: idSesion -> true | false | null (sin marca, cancelada o fuera de su inscripción). */
public record FilaHistorialResponsive(
        String matricula,
        String nombreCompleto,
        String estadoInscripcion,
        OffsetDateTime fechaBaja,
        Map<Integer, Boolean> marcas,
        Integer idSesionBaja,
        long asistio,
        long falto
) {}
