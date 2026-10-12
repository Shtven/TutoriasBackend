package com.codespace.tutorias;

import com.codespace.tutorias.Helpers.Reloj;

import java.time.*;
import java.util.concurrent.atomic.AtomicReference;

/** Clock ajustable para fijar "ahora" en cada prueba. */
public class RelojPrueba extends Clock {

    private final AtomicReference<Instant> instante = new AtomicReference<>(Instant.now());

    public void fijar(LocalDate fecha, LocalTime hora) {
        instante.set(ZonedDateTime.of(fecha, hora, Reloj.ZONA).toInstant());
    }

    public void avanzarMinutos(long minutos) {
        instante.updateAndGet(i -> i.plus(Duration.ofMinutes(minutos)));
    }

    @Override
    public ZoneId getZone() {
        return Reloj.ZONA;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return instante.get();
    }
}
