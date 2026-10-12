package com.codespace.tutorias;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

/** RN-3: dos inscripciones simultáneas no pueden pasar el cupo de 7. */
class ConcurrenciaIntegracionTest extends PruebaIntegracion {

    @Test
    void elUltimoLugarSoloLoObtieneUno() throws Exception {
        String rocio = usuario("S21009342", "Rocío", "Hernández Aguilar", "TUTOR");
        int ed = crearTutoria(rocio, materia("Estructuras de Datos", 48215), 2, 7,
                "[{\"dia\":\"LUNES\",\"horaInicio\":\"10:00\",\"horaFin\":\"12:00\"}]");
        for (int i = 0; i < 6; i++) {
            String m = String.format("S230000%02d", i);
            usuario(m, "Tutorado", "Previo " + i, "TUTORADO");
            inscribir(ed, m);
        }
        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            tokens.add(usuario(String.format("S240000%02d", i), "Tutorado", "Nuevo " + i, "TUTORADO"));
        }

        ExecutorService pool = Executors.newFixedThreadPool(tokens.size());
        CountDownLatch salida = new CountDownLatch(1);
        List<Future<Integer>> resultados = new ArrayList<>();
        for (String token : tokens) {
            resultados.add(pool.submit(() -> {
                salida.await();
                return mvc.perform(postCon(token, "{\"idTutoria\":" + ed + "}", "/inscripciones"))
                        .andReturn().getResponse().getStatus();
            }));
        }
        salida.countDown();
        int exitosas = 0;
        for (Future<Integer> r : resultados) {
            if (r.get(30, TimeUnit.SECONDS) == 200) exitosas++;
        }
        pool.shutdown();

        assertThat(exitosas).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM inscripcion WHERE id_tutoria = ? AND estado = 'ACTIVA'",
                Integer.class, ed)).isEqualTo(7);
    }

    @Test
    void documentacionOpenApiResponde() throws Exception {
        int status = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/v3/api-docs"))
                .andReturn().getResponse().getStatus();
        assertThat(status).isEqualTo(200);
    }
}
