package com.codespace.tutorias.Helpers;

import com.codespace.tutorias.Exceptions.BusinessException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Conversion y validacion de matriculas recibidas desde el cliente.
 *
 * El frontend manda la matricula como un String con prefijo institucional
 * ("zS20210123", "zs20210123" o "S20210123") y la BD la almacena como
 * Integer de 8 digitos. Este helper centraliza el contrato:
 *
 *   - Quita el prefijo (case-insensitive: zS / zs / Zs / ZS / s / S)
 *   - Exige que lo restante sean exactamente 8 digitos
 *   - Convierte a Integer
 *
 * Cualquier desviacion lanza {@link BusinessException} con mensaje claro
 * para el usuario final.
 */
public final class MatriculaHelper {

    private static final int DIGITOS = 8;

    // Prefijo opcional zs/s seguido de exactamente 8 digitos.
    // Acepta cualquier combinacion de mayusculas/minusculas en el prefijo.
    private static final Pattern PATRON =
            Pattern.compile("^(?:zs|s)?(\\d{" + DIGITOS + "})$", Pattern.CASE_INSENSITIVE);

    private MatriculaHelper() {}

    /**
     * Parsea la matricula recibida del cliente.
     *
     * @param raw el valor crudo del cliente, p.ej. "zS20210123" o "20210123".
     * @return la matricula como Integer (8 digitos numericos).
     * @throws BusinessException si es null/vacio, tiene prefijo invalido,
     *                           contiene caracteres no numericos despues del
     *                           prefijo o no tiene exactamente 8 digitos.
     */
    public static Integer parse(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new BusinessException("La matricula es obligatoria.");
        }

        Matcher m = PATRON.matcher(raw.trim());
        if (!m.matches()) {
            throw new BusinessException(
                    "Matricula invalida: debe tener " + DIGITOS +
                    " digitos, opcionalmente precedidos del prefijo 'zS' o 'S'."
            );
        }

        // Integer.parseInt es seguro: el regex ya garantiza 8 digitos.
        return Integer.parseInt(m.group(1));
    }
}
