package com.codespace.tutorias;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Paginación opcional de GET /materias y GET /admin/tutorias (punto 6 de los pendientes del front). */
class PaginacionIntegracionTest extends PruebaIntegracion {

    private static final String HORARIO = "[{\"dia\":\"LUNES\",\"horaInicio\":\"%02d:00\",\"horaFin\":\"%02d:00\"}]";

    @Test
    void materiasSinParametrosTraeTodoYConParametrosPagina() throws Exception {
        String admin = usuario("25480", "Adriana", "Salas Ortega", "ADMIN");
        for (String[] ee : new String[][]{{"Álgebra lineal", "101"}, {"Bases de datos", "102"},
                {"Cálculo", "103"}, {"Diseño de software", "104"}, {"Estructuras de datos", "105"}}) {
            materia(ee[0], Integer.parseInt(ee[1]));
        }

        // Sin page ni size: lista completa, como antes.
        mvc.perform(getCon(admin, "/materias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(5)))
                .andExpect(header().string("X-Total-Count", "5"))
                .andExpect(header().doesNotExist("X-Page"));

        mvc.perform(getCon(admin, "/materias?page=0&size=2"))
                .andExpect(jsonPath("$.data[*].nombre", contains("Álgebra lineal", "Bases de datos")))
                .andExpect(header().string("X-Total-Count", "5"))
                .andExpect(header().string("X-Page", "0"))
                .andExpect(header().string("X-Page-Size", "2"));

        mvc.perform(getCon(admin, "/materias?page=2&size=2"))
                .andExpect(jsonPath("$.data[*].nombre", contains("Estructuras de datos")));
        mvc.perform(getCon(admin, "/materias?page=9&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(0)))
                .andExpect(header().string("X-Total-Count", "5"));

        // Solo size: página 0. El total cuenta lo filtrado, no todo el catálogo.
        mvc.perform(getCon(admin, "/materias?q=datos&size=1"))
                .andExpect(jsonPath("$.data[*].nombre", contains("Bases de datos")))
                .andExpect(header().string("X-Total-Count", "2"))
                .andExpect(header().string("X-Page", "0"));
    }

    @Test
    void parametrosInvalidosSon400() throws Exception {
        String admin = usuario("25480", "Adriana", "Salas Ortega", "ADMIN");

        mvc.perform(getCon(admin, "/materias?size=101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("size debe estar entre 1 y 100."));
        mvc.perform(getCon(admin, "/materias?page=-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("page debe ser 0 o mayor."));
        mvc.perform(getCon(admin, "/admin/tutorias?page=uno"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Valor inválido para 'page'."));
    }

    @Test
    void tutoriasDelAdminPaginanDespuesDeFiltrar() throws Exception {
        String admin = usuario("25480", "Adriana", "Salas Ortega", "ADMIN");
        String rocio = usuario("S21009342", "Rocío", "Hernández Aguilar", "TUTOR");
        String jorge = usuario("S21004411", "Jorge", "Castillo Rivera", "TUTOR");
        crearTutoria(rocio, materia("Bases de datos", 201), 2, 7, HORARIO.formatted(8, 10));
        crearTutoria(rocio, materia("Cálculo", 202), 2, 7, HORARIO.formatted(10, 12));
        crearTutoria(rocio, materia("Redes", 203), 2, 7, HORARIO.formatted(12, 14));
        crearTutoria(jorge, materia("Álgebra lineal", 204), 1, 9, HORARIO.formatted(8, 10));

        mvc.perform(getCon(admin, "/admin/tutorias?matriculaTutor=S21009342&page=1&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[*].materia.nombre", contains("Redes")))
                .andExpect(jsonPath("$.data[0].cupo.maximo").value(7))
                .andExpect(header().string("X-Total-Count", "3"));

        mvc.perform(getCon(admin, "/admin/tutorias"))
                .andExpect(jsonPath("$.data", hasSize(4)))
                .andExpect(header().string("X-Total-Count", "4"));
    }

    @Test
    void elNavegadorPuedeLeerLasCabecerasDePaginacion() throws Exception {
        String admin = usuario("25480", "Adriana", "Salas Ortega", "ADMIN");

        mvc.perform(getCon(admin, "/materias?size=5").header(HttpHeaders.ORIGIN, "http://localhost:5173"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, containsString("X-Total-Count")));
    }
}
