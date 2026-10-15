package com.codespace.tutorias;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * cors.allowed-origins de application-test.properties lleva espacios, una
 * barra final y un comodín de PR preview, como podría quedar en Render.
 */
class CorsIntegracionTest extends PruebaIntegracion {

    private org.springframework.test.web.servlet.ResultActions preflight(String origen, String metodo) throws Exception {
        return mvc.perform(options("/tutorias/12/estado")
                .header(HttpHeaders.ORIGIN, origen)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, metodo)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization, content-type"));
    }

    @Test
    void preflightDeOrigenesPermitidosConTodosLosMetodos() throws Exception {
        for (String origen : new String[]{"http://localhost:5173", "http://localhost:4173",
                "https://tutorias-front-pr-17.onrender.com"}) {
            for (String metodo : new String[]{"GET", "POST", "PUT", "PATCH", "DELETE"}) {
                preflight(origen, metodo)
                        .andExpect(status().isOk())
                        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origen))
                        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString(metodo)))
                        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, containsStringIgnoringCase("Authorization")))
                        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, containsStringIgnoringCase("Content-Type")));
            }
        }
    }

    @Test
    void origenNoPermitidoSeRechaza() throws Exception {
        for (String origen : new String[]{"https://otro-sitio.com", "http://localhost:8081",
                "https://tutorias-front.onrender.com.atacante.com"}) {
            preflight(origen, "GET").andExpect(status().isForbidden());
        }
    }

    @Test
    void cabeceraNoPermitidaSeRechaza() throws Exception {
        mvc.perform(options("/tutorias")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "x-cabecera-rara"))
                .andExpect(status().isForbidden());
    }

    @Test
    void respuestaRealLlevaElOrigenYLosErroresTambien() throws Exception {
        // Un 401 también debe llevar la cabecera; si no, el navegador oculta el mensaje.
        mvc.perform(get("/usuarios/me").header(HttpHeaders.ORIGIN, "http://localhost:4173"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4173"));
    }
}
