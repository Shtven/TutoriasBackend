package com.codespace.tutorias.Helpers;

import java.time.LocalTime;

public final class DateHelper {

    private DateHelper() {}

    /**
     * Dos rangos se traslapan si cada uno empieza antes de que termine el otro.
     * Los rangos contiguos (10:00–12:00 y 12:00–14:00) no se traslapan (RN-1).
     */
    public static boolean haySolapamiento(LocalTime inicio1, LocalTime fin1,
                                          LocalTime inicio2, LocalTime fin2) {
        return inicio1.isBefore(fin2) && inicio2.isBefore(fin1);
    }
}
