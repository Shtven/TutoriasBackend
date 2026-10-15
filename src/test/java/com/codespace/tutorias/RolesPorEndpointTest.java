package com.codespace.tutorias;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

/**
 * Matriz de roles por endpoint (docs/API.md § 11). Cada endpoint protegido
 * debe responder 401 sin token y 403 con un rol no permitido; con un rol
 * permitido, nunca 401 ni 403. La tabla tiene que cubrir todos los
 * endpoints: uno nuevo sin fila hace fallar {@link #laTablaCubreTodosLosEndpoints()}.
 *
 * Los bodies pasan @Valid a propósito: la validación corre antes de
 * @PreAuthorize, y un body inválido daría 400 en lugar de 403.
 */
class RolesPorEndpointTest extends PruebaIntegracion {

    private static final String TUTORADO = "TUTORADO";
    private static final String TUTOR = "TUTOR";
    private static final String ADMIN = "ADMIN";
    private static final Set<String> TODOS = Set.of(TUTORADO, TUTOR, ADMIN);

    private static final String HORARIOS = "[{\"dia\":\"LUNES\",\"horaInicio\":\"10:00\",\"horaFin\":\"12:00\"}]";

    /** patron = como lo registra Spring MVC; url = con ids de ejemplo. */
    private record Endpoint(HttpMethod metodo, String patron, String url, Set<String> roles, String body) {
        String clave() {
            return metodo.name() + " " + patron;
        }
    }

    private static Endpoint e(HttpMethod metodo, String patron, Set<String> roles) {
        return e(metodo, patron, roles, null);
    }

    private static Endpoint e(HttpMethod metodo, String patron, Set<String> roles, String body) {
        String url = patron.replaceAll("\\{matricula}", "S21004411").replaceAll("\\{[^}]+}", "1");
        return new Endpoint(metodo, patron, url, roles, body);
    }

    private static final List<Endpoint> ENDPOINTS = List.of(
            e(HttpMethod.GET, "/usuarios/me", TODOS),

            e(HttpMethod.GET, "/materias", Set.of(TUTOR, ADMIN)),
            e(HttpMethod.GET, "/materias/{idMateria}", Set.of(TUTOR, ADMIN)),
            e(HttpMethod.POST, "/materias", Set.of(ADMIN), "{\"nombre\":\"Redes\",\"nrc\":52310}"),
            e(HttpMethod.PUT, "/materias/{idMateria}", Set.of(ADMIN), "{\"nombre\":\"Redes\",\"nrc\":52310}"),
            e(HttpMethod.DELETE, "/materias/{idMateria}", Set.of(ADMIN)),

            e(HttpMethod.GET, "/catalogos/edificios", Set.of(TUTOR, ADMIN)),

            e(HttpMethod.GET, "/tutorias", Set.of(TUTORADO)),
            e(HttpMethod.GET, "/tutorias/{idTutoria:\\d+}", TODOS),
            e(HttpMethod.GET, "/tutorias/mis-tutorias", Set.of(TUTOR)),
            e(HttpMethod.POST, "/tutorias", Set.of(TUTOR),
                    "{\"idMateria\":1,\"edificio\":1,\"aula\":1,\"horarios\":" + HORARIOS + "}"),
            e(HttpMethod.PUT, "/tutorias/{idTutoria}", Set.of(TUTOR),
                    "{\"edificio\":1,\"aula\":1,\"horarios\":" + HORARIOS + "}"),
            e(HttpMethod.PATCH, "/tutorias/{idTutoria}/estado", Set.of(TUTOR, ADMIN), "{\"estado\":\"INACTIVA\"}"),
            e(HttpMethod.GET, "/tutorias/{idTutoria}/inscritos", Set.of(TUTOR, ADMIN)),
            e(HttpMethod.GET, "/tutorias/{idTutoria}/sesiones", Set.of(TUTOR, ADMIN)),
            e(HttpMethod.GET, "/tutorias/{idTutoria}/historial-asistencia", Set.of(TUTOR, ADMIN)),

            e(HttpMethod.POST, "/horarios/{idHorario}/temas", Set.of(TUTOR), "{\"tema\":\"Árboles AVL\"}"),
            e(HttpMethod.DELETE, "/temas/{idTema}", Set.of(TUTOR)),

            e(HttpMethod.POST, "/inscripciones", Set.of(TUTORADO), "{\"idTutoria\":1}"),
            e(HttpMethod.GET, "/inscripciones", Set.of(TUTORADO)),
            e(HttpMethod.GET, "/inscripciones/tutorias/{idTutoria}", Set.of(TUTORADO)),
            e(HttpMethod.DELETE, "/inscripciones/tutorias/{idTutoria}", Set.of(TUTORADO)),
            e(HttpMethod.GET, "/inscripciones/semana", Set.of(TUTORADO)),

            e(HttpMethod.GET, "/sesiones/hoy", Set.of(TUTOR)),
            e(HttpMethod.POST, "/sesiones/abrir-lista", Set.of(TUTOR), "{\"idHorario\":1,\"fecha\":\"2026-10-05\"}"),
            e(HttpMethod.POST, "/sesiones/cancelar", Set.of(TUTOR), "{\"idHorario\":1,\"fecha\":\"2026-10-05\"}"),
            e(HttpMethod.GET, "/sesiones/{idSesion}/lista", Set.of(TUTOR, ADMIN)),
            e(HttpMethod.PUT, "/sesiones/{idSesion}/lista", Set.of(TUTOR),
                    "{\"marcas\":[{\"matricula\":\"S23014587\",\"asistio\":true}]}"),
            e(HttpMethod.POST, "/sesiones/{idSesion}/cerrar-lista", Set.of(TUTOR)),

            e(HttpMethod.GET, "/tutorias/{idTutoria}/comentarios", TODOS),
            e(HttpMethod.POST, "/tutorias/{idTutoria}/comentarios", Set.of(TUTORADO), "{\"comentario\":\"Gracias\"}"),
            e(HttpMethod.DELETE, "/comentarios/{idComentario}", Set.of(TUTORADO, ADMIN)),

            e(HttpMethod.GET, "/admin/resumen", Set.of(ADMIN)),
            e(HttpMethod.GET, "/admin/tutorias", Set.of(ADMIN)),
            e(HttpMethod.GET, "/admin/tutores", Set.of(ADMIN)),
            e(HttpMethod.POST, "/admin/tutores", Set.of(ADMIN),
                    "{\"nombre\":\"Jorge\",\"apellidos\":\"Castillo Rivera\",\"matricula\":\"S21004411\","
                            + "\"correo\":\"zs21004411@estudiantes.uv.mx\"}"),
            e(HttpMethod.DELETE, "/admin/tutores/{matricula}", Set.of(ADMIN)),
            e(HttpMethod.POST, "/admin/tutorias/cierre-periodo", Set.of(ADMIN), "{\"confirmar\":false}")
    );

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    private final Map<String, String> tokens = new HashMap<>();

    @BeforeEach
    void usuarios() {
        tokens.put(TUTORADO, usuario("S23014587", "Valeria", "Mendoza Cruz", TUTORADO));
        tokens.put(TUTOR, usuario("S21009342", "Rocío", "Hernández Aguilar", TUTOR));
        tokens.put(ADMIN, usuario("25480", "Adriana", "Salas Ortega", ADMIN));
    }

    private MockHttpServletRequestBuilder peticion(Endpoint endpoint, String token) {
        MockHttpServletRequestBuilder req = request(endpoint.metodo(), endpoint.url());
        if (endpoint.body() != null) req.contentType(MediaType.APPLICATION_JSON).content(endpoint.body());
        return token == null ? req : conToken(req, token);
    }

    private int status(Endpoint endpoint, String token) throws Exception {
        return mvc.perform(peticion(endpoint, token)).andReturn().getResponse().getStatus();
    }

    @Test
    void laTablaCubreTodosLosEndpoints() {
        Set<String> registrados = handlerMapping.getHandlerMethods().entrySet().stream()
                .filter(h -> h.getValue().getBeanType().getPackageName().equals("com.codespace.tutorias.Controllers"))
                .flatMap(h -> h.getKey().getMethodsCondition().getMethods().stream()
                        .flatMap(m -> h.getKey().getPatternValues().stream().map(p -> m.name() + " " + p)))
                .filter(clave -> !clave.contains(" /auth/"))
                .collect(Collectors.toCollection(TreeSet::new));
        Set<String> enTabla = ENDPOINTS.stream().map(Endpoint::clave).collect(Collectors.toCollection(TreeSet::new));

        assertThat(registrados).as("endpoints de los controladores (sin /auth/**)").isEqualTo(enTabla);
    }

    @Test
    void sinTokenTodoEndpointProtegidoResponde401() throws Exception {
        List<String> fallas = new ArrayList<>();
        for (Endpoint endpoint : ENDPOINTS) {
            int status = status(endpoint, null);
            if (status != 401) fallas.add(endpoint.clave() + " -> " + status);
        }
        assertThat(fallas).as("deberían responder 401 sin token").isEmpty();
    }

    @Test
    void conUnRolNoPermitidoResponde403() throws Exception {
        List<String> fallas = new ArrayList<>();
        for (Endpoint endpoint : ENDPOINTS) {
            for (String rol : TODOS) {
                if (endpoint.roles().contains(rol)) continue;
                int status = status(endpoint, tokens.get(rol));
                if (status != 403) fallas.add(endpoint.clave() + " como " + rol + " -> " + status);
            }
        }
        assertThat(fallas).as("deberían responder 403").isEmpty();
    }

    @Test
    void conUnRolPermitidoNoSeRechazaPorPermisos() throws Exception {
        List<String> fallas = new ArrayList<>();
        for (Endpoint endpoint : ENDPOINTS) {
            for (String rol : endpoint.roles()) {
                // Los ids de ejemplo no existen: se espera 200 o 400 de negocio, nunca 401/403.
                int status = status(endpoint, tokens.get(rol));
                if (status == 401 || status == 403 || status >= 500) {
                    fallas.add(endpoint.clave() + " como " + rol + " -> " + status);
                }
            }
        }
        assertThat(fallas).as("un rol permitido no debería recibir 401, 403 ni 5xx").isEmpty();
    }
}
