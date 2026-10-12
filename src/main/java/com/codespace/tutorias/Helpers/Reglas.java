package com.codespace.tutorias.Helpers;

/** Constantes de negocio (docs/REGLAS_DE_NEGOCIO.md). */
public final class Reglas {

    public static final int CUPO_MAXIMO = 7;
    /** La inasistencia número INASISTENCIAS_PERMITIDAS + 1 causa baja automática. */
    public static final int INASISTENCIAS_PERMITIDAS = 2;
    public static final int MINUTOS_LIMITE_CANCELACION = 15;
    public static final int HORAS_RECORDATORIO = 2;
    public static final int MAX_TEMAS_POR_HORARIO = 10;
    public static final int MAX_CARACTERES_TEMA = 60;
    public static final int MAX_CARACTERES_COMENTARIO = 255;

    private Reglas() {}
}
