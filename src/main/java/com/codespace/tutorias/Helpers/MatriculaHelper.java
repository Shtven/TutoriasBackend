package com.codespace.tutorias.Helpers;

import com.codespace.tutorias.Exceptions.BusinessException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Normaliza la matrícula capturada en el registro. Se guarda con prefijo
 * "S" + 8 dígitos ("S24011236"). Se acepta el prefijo de correo "z"
 * ("zS24011236") y cualquier combinación de mayúsculas/minúsculas.
 *
 * No se valida que coincida con el correo. Los ADMIN se asignan por BD y
 * pueden tener otro formato.
 */
public final class MatriculaHelper {

    /** Para @Pattern en los DTO: mismas reglas que {@link #normalizar}. */
    public static final String REGEX = "^[zZ]?[sS]\\d{8}$";

    public static final String MENSAJE_INVALIDA =
            "Matrícula inválida: debe ser S seguida de 8 dígitos (p. ej. S24011236).";

    private static final Pattern PATRON =
            Pattern.compile("^z?s(\\d{8})$", Pattern.CASE_INSENSITIVE);

    private MatriculaHelper() {}

    public static String normalizar(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new BusinessException("La matrícula es obligatoria.");
        }

        Matcher m = PATRON.matcher(raw.trim());
        if (!m.matches()) {
            throw new BusinessException(MENSAJE_INVALIDA);
        }

        return "S" + m.group(1);
    }

    /**
     * Para buscar en el login: normaliza si tiene el formato de alumno y,
     * si no (p. ej. un ADMIN "25480"), la deja tal cual sin espacios.
     */
    public static String paraBusqueda(String raw) {
        String limpio = raw.trim();
        Matcher m = PATRON.matcher(limpio);
        return m.matches() ? "S" + m.group(1) : limpio;
    }
}
