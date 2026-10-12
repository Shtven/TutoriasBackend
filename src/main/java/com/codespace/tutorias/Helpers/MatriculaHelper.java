package com.codespace.tutorias.Helpers;

import com.codespace.tutorias.Exceptions.BusinessException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Normaliza la matrícula capturada en el registro. Se guarda con prefijo
 * "S" + 8 dígitos ("S24011236"). Se acepta el prefijo de correo "z"
 * ("zS24011236") y cualquier combinación de mayúsculas/minúsculas.
 *
 * No se valida que coincida con el correo (las cuentas de Google no lo
 * permiten). Los ADMIN se asignan por BD y pueden tener otro formato.
 */
public final class MatriculaHelper {

    private static final Pattern PATRON =
            Pattern.compile("^z?s(\\d{8})$", Pattern.CASE_INSENSITIVE);

    private MatriculaHelper() {}

    public static String normalizar(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new BusinessException("La matrícula es obligatoria.");
        }

        Matcher m = PATRON.matcher(raw.trim());
        if (!m.matches()) {
            throw new BusinessException(
                    "Matrícula inválida: debe ser S seguida de 8 dígitos (p. ej. S24011236).");
        }

        return "S" + m.group(1);
    }

    /**
     * Para el login con contraseña: si tiene formato de alumno la normaliza;
     * si no (la matrícula de un ADMIN asignada por BD), la deja tal cual.
     */
    public static String normalizarSiAplica(String raw) {
        String limpia = raw.trim();
        Matcher m = PATRON.matcher(limpia);
        return m.matches() ? "S" + m.group(1) : limpia;
    }
}
