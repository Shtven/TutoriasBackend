package com.codespace.tutorias;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Las consultas por petición no crecen con las tutorías ni con las inscripciones (N+1). */
@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
class ConsultasPorEndpointTest extends PruebaIntegracion {

    private static final String YO = "S20000000";

    @Autowired
    private EntityManagerFactory emf;

    private String token;

    @Test
    void explorarNoCreceConElNumeroDeTutorias() throws Exception {
        datos(5, 2);
        long con5 = consultas(getCon(token, "/tutorias"));
        agregarTutorias(5, 40);
        long con40 = consultas(getCon(token, "/tutorias"));

        assertThat(con40).isEqualTo(con5).isLessThanOrEqualTo(8);
    }

    @Test
    void inscripcionesNoCreceConElNumeroDeInscripciones() throws Exception {
        datos(20, 2);
        long con2 = consultas(getCon(token, "/inscripciones"));
        for (int id : tutorias().subList(2, 12)) inscribir(id, YO);
        long con12 = consultas(getCon(token, "/inscripciones"));

        assertThat(con12).isEqualTo(con2).isLessThanOrEqualTo(10);
    }

    @Test
    void detalleSemanaYSesionTienenUnTope() throws Exception {
        datos(20, 4);
        assertThat(consultas(getCon(token, "/tutorias/{id}", tutorias().get(19)))).isLessThanOrEqualTo(9);
        assertThat(consultas(getCon(token, "/inscripciones/semana"))).isLessThanOrEqualTo(2);
        assertThat(consultas(getCon(token, "/usuarios/me"))).isEqualTo(1);
        assertThat(consultas(post("/auth/signin").contentType(MediaType.APPLICATION_JSON)
                .content("{\"matricula\":\"" + YO + "\",\"pwd\":\"secreta123\"}"))).isEqualTo(1);
    }

    private long consultas(MockHttpServletRequestBuilder req) throws Exception {
        Statistics s = emf.unwrap(SessionFactory.class).getStatistics();
        s.clear();
        mvc.perform(req).andExpect(status().isOk());
        return s.getPrepareStatementCount();
    }

    private void datos(int tutorias, int inscritoEn) {
        token = usuarioConPwd(YO, "s20000000@prueba.mx", "TUTORADO", "secreta123");
        agregarTutorias(0, tutorias);
        for (int id : tutorias().subList(0, inscritoEn)) inscribir(id, YO);
    }

    private List<Integer> tutorias() {
        return jdbc.queryForList("SELECT id_tutoria FROM tutoria ORDER BY id_tutoria", Integer.class);
    }

    /** Cada una con su propio tutor y materia, y horario con temas. */
    private void agregarTutorias(int desde, int hasta) {
        String[] dias = {"LUNES", "MARTES", "MIERCOLES", "JUEVES", "VIERNES"};
        for (int i = desde; i < hasta; i++) {
            String tutor = String.format("S3%07d", i);
            usuario(tutor, "Tutor" + i, "Apellido", "TUTOR");
            Integer id = jdbc.queryForObject("INSERT INTO tutoria (id_materia, matricula_tutor, edificio, aula, estado) " +
                    "VALUES (?, ?, 1, 1, 'ACTIVA') RETURNING id_tutoria", Integer.class, materia("Materia " + i, 10000 + i), tutor);
            Integer horario = jdbc.queryForObject("INSERT INTO horario (id_tutoria, dia, hora_inicio, hora_fin, estado) " +
                            "VALUES (?, ?, ?::time, ?::time, 'ACTIVO') RETURNING id_horario", Integer.class, id, dias[i % 5],
                    String.format("%02d:00", 7 + i / 5), String.format("%02d:00", 8 + i / 5));
            jdbc.update("INSERT INTO temas (tema, id_horario) VALUES ('Tema A', ?), ('Tema B', ?)", horario, horario);
        }
    }
}
