package com.codespace.tutorias.Helpers;

/**
 * Estados canónicos del ciclo de vida de una Tutoría.
 *
 * Decisión de unificación SRS ↔ código: el CU-06 del SRS se refiere a una
 * sesión "ACTIVA" como precondición para comentar; en el código y en BD se
 * usa "PROGRAMADA" para representar exactamente el mismo estado (sesión
 * agendada, aún no completada ni cancelada). PROGRAMADA es el término
 * canónico; ACTIVA queda como sinónimo del SRS.
 */
public final class EstadosTutoria {

    public static final String PROGRAMADA = "PROGRAMADA";
    public static final String COMPLETADA = "COMPLETADA";
    public static final String CANCELADA = "CANCELADA";

    /**
     * Estado transitorio que el job de recordatorios asigna ~15 min antes
     * del inicio. No esta en el SRS; se centraliza aqui para documentarlo.
     * Una sesion en este estado ya no aparece en /tutoria/disponibles.
     */
    public static final String POR_INICIAR = "A PUNTO DE INICIAR";

    private EstadosTutoria() {}
}
