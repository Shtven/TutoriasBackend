package com.codespace.tutorias.Helpers;

import org.springframework.stereotype.Component;

import java.time.*;

/**
 * Fuente única de "ahora" para las reglas de negocio. Toda regla de tiempo
 * se evalúa en America/Mexico_City, sin importar la zona del servidor.
 * Envuelve un {@link Clock} inyectable para poder fijar la hora en pruebas.
 */
@Component
public class Reloj {

    public static final ZoneId ZONA = ZoneId.of("America/Mexico_City");

    private final Clock clock;

    public Reloj(Clock clock) {
        this.clock = clock;
    }

    public ZonedDateTime ahora() {
        return ZonedDateTime.now(clock).withZoneSameInstant(ZONA);
    }

    public LocalDate hoy() {
        return ahora().toLocalDate();
    }

    /** Marca de tiempo para columnas timestamptz. */
    public OffsetDateTime marca() {
        return ahora().toOffsetDateTime();
    }

    public ZonedDateTime en(LocalDate fecha, LocalTime hora) {
        return ZonedDateTime.of(fecha, hora, ZONA);
    }

    /** true si ya llegó (o pasó) la fecha + hora indicadas. */
    public boolean yaLlego(LocalDate fecha, LocalTime hora) {
        return !ahora().isBefore(en(fecha, hora));
    }

    /** Convierte una marca de tiempo leída de BD a la zona de negocio. */
    public static OffsetDateTime enZona(OffsetDateTime valor) {
        return valor == null ? null : valor.atZoneSameInstant(ZONA).toOffsetDateTime();
    }
}
