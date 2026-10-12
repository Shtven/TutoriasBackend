package com.codespace.tutorias;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
class ConfigPrueba {

    @Bean
    @Primary
    RelojPrueba relojPrueba() {
        return new RelojPrueba();
    }
}
