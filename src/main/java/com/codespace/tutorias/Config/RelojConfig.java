package com.codespace.tutorias.Config;

import com.codespace.tutorias.Helpers.Reloj;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class RelojConfig {

    /** Reemplazable en pruebas para fijar la hora. */
    @Bean
    public Clock clock() {
        return Clock.system(Reloj.ZONA);
    }
}
