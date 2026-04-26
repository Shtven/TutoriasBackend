package com.codespace.tutorias;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TutoriasApplication {

	public static void main(String[] args) {
		SpringApplication.run(TutoriasApplication.class, args);
	}

}
