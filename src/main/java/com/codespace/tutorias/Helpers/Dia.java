package com.codespace.tutorias.Helpers;

import com.codespace.tutorias.Exceptions.BusinessException;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * Días permitidos para un horario (RN-1: lunes a viernes). Se persiste como
 * String sin acentos; {@link #normalizar(String)} es el único punto de entrada
 * desde el cliente.
 */
public enum Dia {
    LUNES(DayOfWeek.MONDAY, "lunes"),
    MARTES(DayOfWeek.TUESDAY, "martes"),
    MIERCOLES(DayOfWeek.WEDNESDAY, "miércoles"),
    JUEVES(DayOfWeek.THURSDAY, "jueves"),
    VIERNES(DayOfWeek.FRIDAY, "viernes");

    private final DayOfWeek dayOfWeek;
    private final String nombre;

    Dia(DayOfWeek dayOfWeek, String nombre) {
        this.dayOfWeek = dayOfWeek;
        this.nombre = nombre;
    }

    public DayOfWeek dayOfWeek() {
        return dayOfWeek;
    }

    /** Para mensajes: "miércoles". */
    public String nombre() {
        return nombre;
    }

    /** null si la fecha cae en fin de semana. */
    public static Dia de(LocalDate fecha) {
        for (Dia dia : values()) {
            if (dia.dayOfWeek == fecha.getDayOfWeek()) return dia;
        }
        return null;
    }

    /** Primera fecha >= desde que cae en este día. */
    public LocalDate siguienteDesde(LocalDate desde) {
        return desde.with(TemporalAdjusters.nextOrSame(dayOfWeek));
    }

    public boolean coincide(LocalDate fecha) {
        return fecha.getDayOfWeek() == dayOfWeek;
    }

    /** Día guardado en BD (ya normalizado). */
    public static Dia de(String guardado) {
        return Dia.valueOf(guardado);
    }

    /**
     * Normaliza un día recibido del cliente: trim, quita acentos
     * (MIÉRCOLES -> MIERCOLES), mayúsculas y valida contra lunes a viernes.
     *
     * @throws BusinessException si es nulo, vacío o no es un día hábil
     */
    public static String normalizar(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new BusinessException("El día es obligatorio.");
        }

        String sinAcentos = Normalizer.normalize(raw.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        try {
            return Dia.valueOf(sinAcentos.toUpperCase()).name();
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Día inválido: solo de lunes a viernes.");
        }
    }
}
