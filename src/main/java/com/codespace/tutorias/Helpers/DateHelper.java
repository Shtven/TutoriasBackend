package com.codespace.tutorias.Helpers;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.*;

import static tools.jackson.databind.type.LogicalType.DateTime;

public class DateHelper {

    public static boolean haySolapamiento(Time inicio1, Time fin1,
                                          Time inicio2, Time fin2) {

        return inicio1.toLocalTime().isBefore(fin2.toLocalTime()) && inicio2.toLocalTime().isBefore(fin1.toLocalTime());
    }

    public static boolean faltaMenosDe15Minutos(LocalDate fecha, LocalTime horaInicio) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime inicioTutoria = LocalDateTime.of(fecha, horaInicio);
        return Duration.between(now, inicioTutoria).toMinutes() < 15;
    }

    public static boolean yaComenzo(LocalDate fecha, Time horaInicio) {

        ZoneId zonaMexico = ZoneId.of("America/Mexico_City");

        ZonedDateTime ahora = ZonedDateTime.now(zonaMexico);

        ZonedDateTime inicioTutoria = ZonedDateTime.of(
                fecha,
                horaInicio.toLocalTime(),
                zonaMexico
        );

        return ahora.isAfter(inicioTutoria);
    }
}

