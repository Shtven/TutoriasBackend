package com.codespace.tutorias;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Arranca el contexto completo: el script de Supabase se ejecuta y Hibernate
 * valida (ddl-auto=validate) que las entidades coincidan con las tablas.
 */
class TutoriasApplicationTests extends PruebaIntegracion {

	@Test
	void contextLoadsYEsquemaCoincide() {
		assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM rol", Integer.class)).isEqualTo(3);
	}
}
