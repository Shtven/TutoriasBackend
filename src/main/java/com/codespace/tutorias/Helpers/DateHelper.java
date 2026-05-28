package com.codespace.tutorias.Helpers;

import java.time.*;

public class DateHelper {

    public static boolean haySolapamiento(LocalTime inicio1, LocalTime fin1,
                                          LocalTime inicio2, LocalTime fin2) {

        return inicio1.isBefore(fin2) && inicio2.isBefore(fin1);
    }

    public static boolean menosDe15Min(LocalDate fecha, LocalTime horaInicio) {
        ZoneId zona = ZoneId.of("America/Mexico_City");
        ZonedDateTime now = ZonedDateTime.now(zona);
        ZonedDateTime inicio = ZonedDateTime.of(fecha, horaInicio, zona);
        return Duration.between(now, inicio).toMinutes() < 15;
    }

    public static boolean yaComenzo(LocalDate fecha, LocalTime horaInicio) {

        ZoneId zonaMexico = ZoneId.of("America/Mexico_City");

        ZonedDateTime ahora = ZonedDateTime.now(zonaMexico);

        ZonedDateTime inicioTutoria = ZonedDateTime.of(
                fecha,
                horaInicio,
                zonaMexico
        );

        return ahora.isAfter(inicioTutoria);
    }
}

