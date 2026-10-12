package com.codespace.tutorias.Helpers;

/**
 * Estados de una sesión. No se guardan: se derivan de las marcas de tiempo
 * de la fila (o de su ausencia, para las sesiones virtuales). Ver RN-6.
 */
public final class EstadosSesion {

    public static final String PROGRAMADA = "PROGRAMADA";
    public static final String LISTA_ABIERTA = "LISTA_ABIERTA";
    public static final String COMPLETADA = "COMPLETADA";
    public static final String CANCELADA = "CANCELADA";

    private EstadosSesion() {}
}
