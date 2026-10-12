package com.codespace.tutorias;

import com.codespace.tutorias.Helpers.EmailHelper;
import com.codespace.tutorias.JWT.JWTUtils;
import com.codespace.tutorias.Services.VerificadorIdToken;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

/**
 * Base de las pruebas de integración: contexto completo contra PostgreSQL
 * con el esquema de db/supabase/001_esquema.sql, reloj ajustable y correo
 * y verificación de proveedores simulados.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(ConfigPrueba.class)
public abstract class PruebaIntegracion {

    static {
        EsquemaPrueba.crearUnaVez();
    }

    /** Lunes. Las pruebas fijan la hora relativa a esta semana. */
    protected static final LocalDate LUNES = LocalDate.of(2026, 10, 5);

    @MockitoBean
    protected EmailHelper emailHelper;

    @MockitoBean
    protected VerificadorIdToken verificadorIdToken;

    @Autowired
    protected RelojPrueba reloj;

    @Autowired
    protected JdbcTemplate jdbc;

    @Autowired
    protected JWTUtils jwtUtils;

    @Autowired
    private WebApplicationContext wac;

    protected MockMvc mvc;

    @BeforeEach
    void prepararBase() {
        jdbc.execute("TRUNCATE recordatorio, asistencia, sesion, inscripcion, comentarios, temas, " +
                "horario, tutoria, materia, usuario RESTART IDENTITY CASCADE");
        reloj.fijar(LUNES, LocalTime.of(8, 0));
        mvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilters(wac.getBean("springSecurityFilterChain", Filter.class))
                .build();
    }

    // ------------------------------------------------------------------
    // Datos
    // ------------------------------------------------------------------

    protected String usuario(String matricula, String nombre, String apellidos, String rol) {
        jdbc.update("INSERT INTO usuario (matricula, nombre, apellidos, correo, id_rol) " +
                        "VALUES (?, ?, ?, ?, (SELECT id_rol FROM rol WHERE rol = ?))",
                matricula, nombre, apellidos, matricula.toLowerCase() + "@prueba.mx", rol);
        return jwtUtils.generarToken(matricula, rol);
    }

    protected int materia(String nombre, int nrc) {
        return jdbc.queryForObject(
                "INSERT INTO materia (materia, identificador) VALUES (?, ?) RETURNING id_materia",
                Integer.class, nombre, nrc);
    }

    /** Crea una tutoría por la API y devuelve su id. horarios = arreglo JSON. */
    protected int crearTutoria(String tokenTutor, int idMateria, int edificio, int aula, String horarios) throws Exception {
        String json = "{\"idMateria\":" + idMateria + ",\"edificio\":" + edificio + ",\"aula\":" + aula
                + ",\"horarios\":" + horarios + "}";
        String respuesta = mvc.perform(postCon(tokenTutor, json, "/tutorias"))
                .andReturn().getResponse().getContentAsString();
        Integer id = com.jayway.jsonpath.JsonPath.read(respuesta, "$.data.idTutoria");
        return id;
    }

    protected int horario(int idTutoria, String dia) {
        return jdbc.queryForObject("SELECT id_horario FROM horario WHERE id_tutoria = ? AND dia = ? AND estado = 'ACTIVO'",
                Integer.class, idTutoria, dia);
    }

    /** Inscripción directa en BD (para preparar escenarios). */
    protected int inscribir(int idTutoria, String matricula) {
        return jdbc.queryForObject("INSERT INTO inscripcion (id_tutoria, matricula_tutorado, fecha_inscripcion, estado) " +
                "VALUES (?, ?, now() - interval '30 days', 'ACTIVA') RETURNING id_inscripcion", Integer.class, idTutoria, matricula);
    }

    /** Sesión ya cerrada con las marcas dadas (idInscripcion -> asistio). */
    protected int sesionCerrada(int idHorario, LocalDate fecha, java.util.Map<Integer, Boolean> marcas) {
        int idSesion = jdbc.queryForObject("INSERT INTO sesion (id_horario, fecha, hora_inicio, hora_fin, " +
                "fecha_lista_abierta, fecha_lista_cerrada) SELECT id_horario, ?, hora_inicio, hora_fin, now(), now() " +
                "FROM horario WHERE id_horario = ? RETURNING id_sesion", Integer.class, fecha, idHorario);
        marcas.forEach((idInscripcion, asistio) -> jdbc.update(
                "INSERT INTO asistencia (id_sesion, id_inscripcion, asistio) VALUES (?, ?, ?)", idSesion, idInscripcion, asistio));
        return idSesion;
    }

    // ------------------------------------------------------------------
    // Peticiones
    // ------------------------------------------------------------------

    protected MockHttpServletRequestBuilder conToken(MockHttpServletRequestBuilder req, String token) {
        return req.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    protected MockHttpServletRequestBuilder getCon(String token, String url, Object... vars) {
        return conToken(get(url, vars), token);
    }

    protected MockHttpServletRequestBuilder postCon(String token, String json, String url, Object... vars) {
        return conToken(post(url, vars), token).contentType(MediaType.APPLICATION_JSON).content(json);
    }

    protected MockHttpServletRequestBuilder putCon(String token, String json, String url, Object... vars) {
        return conToken(put(url, vars), token).contentType(MediaType.APPLICATION_JSON).content(json);
    }

    protected MockHttpServletRequestBuilder patchCon(String token, String json, String url, Object... vars) {
        return conToken(patch(url, vars), token).contentType(MediaType.APPLICATION_JSON).content(json);
    }

    protected MockHttpServletRequestBuilder deleteCon(String token, String url, Object... vars) {
        return conToken(delete(url, vars), token);
    }
}
