package com.codespace.tutorias.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Las horas viajan como "HH:mm" (docs/README.md § Formatos). Jackson 3
 * escribe "HH:mm:ss" por defecto; la lectura acepta ambos formatos.
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    @Bean
    public JacksonModule horasSinSegundos() {
        return new SimpleModule("HorasSinSegundos").addSerializer(LocalTime.class,
                new StdSerializer<LocalTime>(LocalTime.class) {
                    @Override
                    public void serialize(LocalTime valor, JsonGenerator gen, SerializationContext ctx) {
                        gen.writeString(valor.format(HORA));
                    }
                });
    }
}
