package com.codespace.tutorias.Exceptions;

/** Login bloqueado temporalmente por intentos fallidos (429). */
public class DemasiadosIntentosException extends RuntimeException {

    public DemasiadosIntentosException(long minutos) {
        super("Demasiados intentos fallidos. Intenta de nuevo en " + minutos
                + (minutos == 1 ? " minuto." : " minutos."));
    }
}
