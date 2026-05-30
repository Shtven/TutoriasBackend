package com.codespace.tutorias.Helpers;

import com.codespace.tutorias.Exceptions.BusinessException;

import java.text.Normalizer;

/**
 * Dias de la semana permitidos para un Horario. Se persiste como String
 * en BD por compatibilidad con datos existentes; este enum sirve para
 * normalizar y validar la entrada del usuario.
 *
 * El metodo {@link #normalizar(String)} es el unico punto de entrada
 * desde los servicios: aplica trim, quita acentos (MIÉRCOLES -> MIERCOLES),
 * pasa a mayusculas, y rechaza valores fuera del set permitido.
 */
public enum Dia {
    LUNES, MARTES, MIERCOLES, JUEVES, VIERNES, SABADO, DOMINGO;

    /**
     * Normaliza un dia recibido del cliente:
     *   - trim
     *   - quita acentos (NFD + strip marks)
     *   - to upper case
     *   - valida contra el set de dias permitidos
     *
     * @return el dia normalizado (MAYUSCULAS, sin acentos)
     * @throws BusinessException si es null, vacio o no corresponde a un dia valido
     */
    public static String normalizar(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new BusinessException("El dia es obligatorio.");
        }

        String sinAcentos = Normalizer.normalize(raw.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String enMayusculas = sinAcentos.toUpperCase();

        try {
            return Dia.valueOf(enMayusculas).name();
        } catch (IllegalArgumentException e) {
            throw new BusinessException(
                    "Dia invalido: '" + raw + "'. Valores permitidos: " +
                            "LUNES, MARTES, MIERCOLES, JUEVES, VIERNES, SABADO, DOMINGO."
            );
        }
    }
}
