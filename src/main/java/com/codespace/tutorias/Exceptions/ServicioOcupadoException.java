package com.codespace.tutorias.Exceptions;

/** No hubo turno para comparar la contraseña a tiempo (503). */
public class ServicioOcupadoException extends RuntimeException {

    public ServicioOcupadoException() {
        super("Hay muchas personas entrando a la vez. Intenta de nuevo en unos segundos.");
    }
}
