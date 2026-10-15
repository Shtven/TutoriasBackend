package com.codespace.tutorias.Exceptions;

import java.util.Map;

/**
 * Error de negocio ligado a campos del body (p. ej. matrícula duplicada).
 * Sale igual que un @Valid: 400 con data = { campo: mensaje }.
 */
public class ValidacionException extends RuntimeException {

    private final Map<String, String> errores;

    public ValidacionException(String campo, String mensaje) {
        super(mensaje);
        this.errores = Map.of(campo, mensaje);
    }

    public Map<String, String> getErrores() {
        return errores;
    }
}
