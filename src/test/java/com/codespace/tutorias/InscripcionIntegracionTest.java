package com.codespace.tutorias;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pantallas 04, 05, 07, 08 + comentarios + admin (15). */
class InscripcionIntegracionTest extends PruebaIntegracion {

    private String rocio;
    private String jorge;
    private String valeria;
    private int ed;   // Estructuras de Datos: lun y mié 10–12
    private int bd;   // Bases de Datos: mar y jue 16–18
    private int prob; // Probabilidad: jue 17–19

    @BeforeEach
    void datos() throws Exception {
        rocio = usuario("S21009342", "Rocío", "Hernández Aguilar", "TUTOR");
        jorge = usuario("S21004411", "Jorge", "Castillo Rivera", "TUTOR");
        String daniela = usuario("S21007711", "Daniela", "Ortiz Méndez", "TUTOR");
        valeria = usuario("S23014587", "Valeria", "Mendoza Cruz", "TUTORADO");
        ed = crearTutoria(rocio, materia("Estructuras de Datos", 48215), 2, 7,
                "[{\"dia\":\"LUNES\",\"horaInicio\":\"10:00\",\"horaFin\":\"12:00\",\"temas\":[\"Listas enlazadas\"]}," +
                        "{\"dia\":\"MIERCOLES\",\"horaInicio\":\"10:00\",\"horaFin\":\"12:00\"}]");
        bd = crearTutoria(jorge, materia("Bases de Datos", 48241), 1, 9,
                "[{\"dia\":\"MARTES\",\"horaInicio\":\"16:00\",\"horaFin\":\"18:00\",\"temas\":[\"Normalización\",\"Consultas con JOIN\",\"Índices\"]}," +
                        "{\"dia\":\"JUEVES\",\"horaInicio\":\"16:00\",\"horaFin\":\"18:00\"}]");
        prob = crearTutoria(daniela, materia("Probabilidad y Estadística", 47790), 1, 6,
                "[{\"dia\":\"JUEVES\",\"horaInicio\":\"17:00\",\"horaFin\":\"19:00\"}]");
    }

    private String inscripcion(int idTutoria) {
        return "{\"idTutoria\":" + idTutoria + "}";
    }

    @Test
    void inscribirseConValidacionesYCorreo() throws Exception {
        mvc.perform(postCon(valeria, inscripcion(bd), "/inscripciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Inscripción confirmada."))
                .andExpect(jsonPath("$.data.idTutoria").value(bd))
                .andExpect(jsonPath("$.data.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.data.fechaInscripcion").value("2026-10-05T08:00:00-06:00"));
        verify(emailHelper, timeout(3000)).enviarCorreo(eq("s23014587@prueba.mx"),
                eq("Inscripción confirmada · Bases de Datos"), anyString());

        mvc.perform(postCon(valeria, inscripcion(bd), "/inscripciones"))
                .andExpect(jsonPath("$.message").value("Ya estás inscrito en esta tutoría."));

        mvc.perform(postCon(valeria, inscripcion(prob), "/inscripciones"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "El jueves de 17:00 a 19:00 choca con tu inscripción en Bases de Datos (16:00–18:00)."));

        for (int i = 0; i < 7; i++) {
            String m = String.format("S230000%02d", i);
            usuario(m, "Tutorado", "Lleno " + i, "TUTORADO");
            inscribir(ed, m);
        }
        mvc.perform(postCon(valeria, inscripcion(ed), "/inscripciones"))
                .andExpect(jsonPath("$.message").value("La tutoría ya no tiene lugares disponibles."));

        mvc.perform(patchCon(jorge, "{\"estado\":\"INACTIVA\"}", "/tutorias/{id}/estado", bd));
        String diego = usuario("S23011873", "Diego", "Ramírez Soto", "TUTORADO");
        mvc.perform(postCon(diego, inscripcion(bd), "/inscripciones"))
                .andExpect(jsonPath("$.message").value("Esta tutoría ya no está disponible."));

        mvc.perform(postCon(rocio, inscripcion(prob), "/inscripciones")).andExpect(status().isForbidden());
    }

    @Test
    void misTutoriasActivasAnterioresYReinscripcion() throws Exception {
        mvc.perform(postCon(valeria, inscripcion(bd), "/inscripciones")).andExpect(status().isOk());
        mvc.perform(postCon(valeria, inscripcion(ed), "/inscripciones")).andExpect(status().isOk());

        // Lunes 08:00: la más próxima es Estructuras hoy 10:00.
        mvc.perform(getCon(valeria, "/inscripciones"))
                .andExpect(jsonPath("$.data.activas", hasSize(2)))
                .andExpect(jsonPath("$.data.activas[0].inscripcion.nueva").value(true))
                .andExpect(jsonPath("$.data.activas[0].inscripcion.fechaBaja").doesNotExist())
                .andExpect(jsonPath("$.data.activas[0].inasistenciasPermitidas").value(2))
                .andExpect(jsonPath("$.data.activas[0].proximaSesion.primera").value(true))
                .andExpect(jsonPath("$.data.proximaSesion.materia.nombre").value("Estructuras de Datos"))
                .andExpect(jsonPath("$.data.proximaSesion.fecha").value("2026-10-05"))
                .andExpect(jsonPath("$.data.proximaSesion.temas", contains("Listas enlazadas")))
                .andExpect(jsonPath("$.data.proximaSesion.recordatorioA").value("08:00"))
                .andExpect(jsonPath("$.data.anteriores", empty()));

        mvc.perform(getCon(valeria, "/inscripciones/semana"))
                .andExpect(jsonPath("$.data[*].dia", contains("LUNES", "MARTES", "MIERCOLES", "JUEVES")));

        // Baja voluntaria: pasa a anteriores y puede volver a inscribirse.
        mvc.perform(deleteCon(valeria, "/inscripciones/tutorias/{id}", bd))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Te diste de baja de la tutoría."));
        verify(emailHelper, timeout(3000)).enviarCorreo(eq("s23014587@prueba.mx"),
                eq("Baja de tutoría · Bases de Datos"), anyString());
        mvc.perform(deleteCon(valeria, "/inscripciones/tutorias/{id}", bd))
                .andExpect(jsonPath("$.message").value("No tienes una inscripción activa en esta tutoría."));

        mvc.perform(getCon(valeria, "/inscripciones"))
                .andExpect(jsonPath("$.data.activas", hasSize(1)))
                .andExpect(jsonPath("$.data.anteriores[0].tutoria.materia.nombre").value("Bases de Datos"))
                .andExpect(jsonPath("$.data.anteriores[0].inscripcion.estado").value("BAJA"))
                .andExpect(jsonPath("$.data.anteriores[0].inscripcion.fechaBaja").isString());

        reloj.avanzarMinutos(1);
        mvc.perform(postCon(valeria, inscripcion(bd), "/inscripciones")).andExpect(status().isOk());
        mvc.perform(getCon(valeria, "/inscripciones"))
                .andExpect(jsonPath("$.data.activas", hasSize(2)))
                .andExpect(jsonPath("$.data.anteriores", empty()));

        // Tras una baja automática ya no puede volver.
        jdbc.update("UPDATE inscripcion SET estado = 'BAJA_AUTOMATICA', fecha_baja = now() " +
                "WHERE id_tutoria = ? AND estado = 'ACTIVA'", bd);
        mvc.perform(postCon(valeria, inscripcion(bd), "/inscripciones"))
                .andExpect(jsonPath("$.message").value("No puedes volver a inscribirte: tuviste baja automática en esta tutoría."));
        mvc.perform(getCon(valeria, "/tutorias")).andExpect(jsonPath("$.data[*].idTutoria", not(hasItem(bd))));
    }

    @Test
    void miAsistenciaEnUnaTutoria() throws Exception {
        int idIns = inscribir(bd, "S23014587");
        int martes = horario(bd, "MARTES");
        int jueves = horario(bd, "JUEVES");
        sesionCerrada(jueves, LUNES.minusDays(4), Map.of(idIns, true));     // jue 1 oct
        sesionCerrada(martes, LUNES.minusDays(6), Map.of(idIns, false));    // mar 29 sep
        jdbc.update("INSERT INTO sesion (id_horario, fecha, hora_inicio, hora_fin, fecha_cancelada) " +
                "VALUES (?, '2026-09-24', '16:00', '18:00', now())", jueves);  // jue 24 sep cancelada

        mvc.perform(getCon(valeria, "/inscripciones/tutorias/{id}", bd))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tutoria.materia.nombre").value("Bases de Datos"))
                .andExpect(jsonPath("$.data.tutoria.panelTutorado.estado").value("INSCRIPCION_ACTIVA"))
                .andExpect(jsonPath("$.data.inscripcion.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.data.resumen.inasistencias").value(1))
                .andExpect(jsonPath("$.data.resumen.sesionesConAsistencia").value(1))
                .andExpect(jsonPath("$.data.resumen.sesionesCanceladas").value(1))
                .andExpect(jsonPath("$.data.proximaSesion.fecha").value("2026-10-06"))
                .andExpect(jsonPath("$.data.proximaSesion.recordatorioA").value("14:00"))
                .andExpect(jsonPath("$.data.historial[*].miAsistencia", contains("PENDIENTE", "ASISTIO", "NO_ASISTIO", "NO_CUENTA")))
                .andExpect(jsonPath("$.data.historial[0].idSesion").value(nullValue()))
                .andExpect(jsonPath("$.data.historial[0].estadoSesion").value("PROGRAMADA"));

        mvc.perform(getCon(valeria, "/inscripciones/tutorias/{id}", prob))
                .andExpect(jsonPath("$.message").value("No tienes inscripción en esta tutoría."));
    }

    @Test
    void comentariosDeTutorados() throws Exception {
        String diego = usuario("S23011873", "Diego", "Ramírez Soto", "TUTORADO");
        String comentario = "{\"comentario\":\"¿Podríamos repasar la notación Big-O antes del primer parcial?\"}";

        mvc.perform(postCon(diego, comentario, "/tutorias/{id}/comentarios", ed))
                .andExpect(jsonPath("$.message").value("Inscríbete para dejar comentarios."));

        inscribir(ed, "S23011873");
        String respuesta = mvc.perform(postCon(diego, comentario, "/tutorias/{id}/comentarios", ed))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comentario publicado."))
                .andExpect(jsonPath("$.data.esMio").value(true))
                .andReturn().getResponse().getContentAsString();
        int idComentario = JsonPath.read(respuesta, "$.data.idComentario");

        mvc.perform(postCon(diego, "{\"comentario\":\"" + "x".repeat(256) + "\"}", "/tutorias/{id}/comentarios", ed))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.comentario").value("El comentario no puede exceder 255 caracteres."));

        // Un tutorado no inscrito sí puede leerlos (pantalla 04); el tutor también.
        mvc.perform(getCon(valeria, "/tutorias/{id}/comentarios", ed))
                .andExpect(jsonPath("$.data[0].nombreCompleto").value("Diego Ramírez Soto"))
                .andExpect(jsonPath("$.data[0].esMio").value(false))
                .andExpect(jsonPath("$.data[0].fechaComentario", endsWith("-06:00")));
        mvc.perform(getCon(rocio, "/tutorias/{id}/comentarios", ed)).andExpect(jsonPath("$.data", hasSize(1)));

        mvc.perform(deleteCon(valeria, "/comentarios/{id}", idComentario))
                .andExpect(jsonPath("$.message").value("No tienes permiso sobre este recurso."));
        mvc.perform(deleteCon(diego, "/comentarios/{id}", idComentario))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comentario eliminado."));
    }

    @Test
    void panelDeAdministracion() throws Exception {
        String admin = usuario("25480", "Adriana", "Salas Ortega", "ADMIN");
        int idIns = inscribir(ed, "S23014587");
        usuario("S23016620", "Andrea", "Morales Pineda", "TUTORADO");
        int andrea = inscribir(ed, "S23016620");
        sesionCerrada(horario(ed, "LUNES"), LUNES.minusWeeks(1), Map.of(idIns, true, andrea, false));
        jdbc.update("UPDATE inscripcion SET estado = 'BAJA_AUTOMATICA', fecha_baja = '2026-08-20' WHERE id_inscripcion = ?", andrea);

        mvc.perform(getCon(rocio, "/admin/resumen")).andExpect(status().isForbidden());

        mvc.perform(getCon(admin, "/admin/resumen"))
                .andExpect(jsonPath("$.data.tutoriasActivas").value(3))
                .andExpect(jsonPath("$.data.inscripcionesActivas").value(1))
                .andExpect(jsonPath("$.data.lugaresTotales").value(21))
                .andExpect(jsonPath("$.data.lugaresDisponibles").value(20))
                .andExpect(jsonPath("$.data.bajasAutomaticas").value(1));
        mvc.perform(getCon(admin, "/admin/resumen?desde=2026-09-01"))
                .andExpect(jsonPath("$.data.bajasAutomaticas").value(0));

        mvc.perform(getCon(admin, "/admin/tutorias"))
                .andExpect(jsonPath("$.data[*].materia.nombre",
                        contains("Bases de Datos", "Estructuras de Datos", "Probabilidad y Estadística")))
                .andExpect(jsonPath("$.data[1].asistenciaPct").value(50))
                .andExpect(jsonPath("$.data[1].tutor.nombreCompleto").value("Rocío Hernández Aguilar"))
                .andExpect(jsonPath("$.data[0].asistenciaPct").value(nullValue()));
        mvc.perform(getCon(admin, "/admin/tutorias?matriculaTutor=S21004411"))
                .andExpect(jsonPath("$.data[*].idTutoria", contains(bd)));
        mvc.perform(getCon(admin, "/admin/tutores"))
                .andExpect(jsonPath("$.data[*].nombre", contains("Daniela", "Jorge", "Rocío")));

        // Cierre de periodo: rechazado con lista abierta, luego desactiva todo.
        reloj.fijar(LUNES, LocalTime.of(10, 5));
        mvc.perform(postCon(rocio, "{\"idHorario\":" + horario(ed, "LUNES") + ",\"fecha\":\"2026-10-05\"}", "/sesiones/abrir-lista"));
        mvc.perform(postCon(admin, "{\"confirmar\":true}", "/admin/tutorias/cierre-periodo"))
                .andExpect(jsonPath("$.message").value("No se puede cerrar el periodo: hay pases de lista abiertos en Estructuras de Datos."));
        jdbc.update("DELETE FROM sesion WHERE fecha = '2026-10-05'");

        mvc.perform(postCon(admin, "{\"confirmar\":false}", "/admin/tutorias/cierre-periodo"))
                .andExpect(jsonPath("$.message").value("Confirma el cierre del periodo."));
        mvc.perform(postCon(admin, "{\"confirmar\":true}", "/admin/tutorias/cierre-periodo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tutoriasDesactivadas").value(3));
        mvc.perform(getCon(admin, "/admin/tutorias?estado=INACTIVA")).andExpect(jsonPath("$.data", hasSize(3)));
        mvc.perform(getCon(valeria, "/inscripciones"))
                .andExpect(jsonPath("$.data.activas", empty()))
                .andExpect(jsonPath("$.data.anteriores[0].tutoria.estado").value("INACTIVA"));
    }
}
