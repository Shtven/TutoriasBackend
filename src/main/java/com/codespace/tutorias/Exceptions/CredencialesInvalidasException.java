package com.codespace.tutorias.Exceptions;

/**
 * Login fallido (401). Siempre el mismo mensaje, exista o no la cuenta,
 * para no revelar qué matrículas o correos están registrados.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Usuario o contraseña incorrectos.");
    }
}
