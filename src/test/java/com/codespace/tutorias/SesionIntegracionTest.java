package com.codespace.tutorias;

import com.codespace.tutorias.Services.SesionService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pantallas 09, 11, 12, 13 y 14 + procesos automáticos (RN-6 a RN-11, RN-17). */
class SesionIntegracionTest extends PruebaIntegracion {

    @Autowired
    private SesionService sesionService;

    private String rocio;
    private int ed;
    private int lunes;
    private int miercoles;
    private int mariana;

    @BeforeEach
    void datos() throws Exception {
        rocio = usuario("S21009342", "Rocío", "Hernández Aguilar", "TUTOR");
        ed = crearTutoria(rocio, materia("Estructuras de Datos", 48215), 2, 7,
                "[{\"dia\":\"LUNES\",\"horaInicio\":\"10:00\",\"horaFin\":\"12:00\"}," +
                        "{\"dia\":\"MIERCOLES\",\"horaInicio\":\"10:00\",\"horaFin\":\"12:00\"}]");
        lunes = horario(ed, "LUNES");
        miercoles = horario(ed, "MIERCOLES");

        String[][] tutorados = {
                {"S23016620", "Andrea", "Morales Pineda"}, {"S23011873", "Diego", "Ramírez Soto"},
                {"S22019045", "Fernando", "Aguilar Ruiz"}, {"S23012298", "Kevin", "Domínguez Luna"},
                {"S23017731", "Mariana", "López Herrera"}};
        Map<Integer, Boolean> semana1 = new java.util.HashMap<>();
        Map<Integer, Boolean> semana2 = new java.util.HashMap<>();
        for (String[] t : tutorados) {
            usuario(t[0], t[1], t[2], "TUTORADO");
            int id = inscribir(ed, t[0]);
            if (t[0].equals("S23017731")) mariana = id;
            semana1.put(id, !t[0].equals("S23017731"));
            semana2.put(id, !t[0].equals("S23017731"));
        }
        // Mariana ya lleva 2 inasistencias (en riesgo).
        sesionCerrada(lunes, LUNES.minusWeeks(2), semana1);
        sesionCerrada(miercoles, LUNES.minusDays(5), semana2);
    }

    private String marcasTodosMenosMariana() {
        return "{\"marcas\":[" +
                "{\"matricula\":\"S23016620\",\"asistio\":true},{\"matricula\":\"S23011873\",\"asistio\":true}," +
                "{\"matricula\":\"S22019045\",\"asistio\":true},{\"matricula\":\"S23012298\",\"asistio\":true}," +
                "{\"matricula\":\"S23017731\",\"asistio\":false}]}";
    }

    @Test
    void paseDeListaCompletoConBajaAutomatica() throws Exception {
        String abrir = "{\"idHorario\":" + lunes + ",\"fecha\":\"2026-10-05\"}";

        // 08:00: sesión de hoy programada, aún no se puede abrir.
        mvc.perform(getCon(rocio, "/sesiones/hoy"))
                .andExpect(jsonPath("$.data.fecha").value("2026-10-05"))
                .andExpect(jsonPath("$.data.sesiones", hasSize(1)))
                .andExpect(jsonPath("$.data.sesiones[0].idSesion").value(nullValue()))
                .andExpect(jsonPath("$.data.sesiones[0].estado").value("PROGRAMADA"))
                .andExpect(jsonPath("$.data.sesiones[0].inscritos").value(5))
                .andExpect(jsonPath("$.data.sesiones[0].recordatorioA").value("08:00"))
                .andExpect(jsonPath("$.data.sesiones[0].acciones.abrirLista").value(false))
                .andExpect(jsonPath("$.data.sesiones[0].acciones.cancelar").value(true));
        mvc.perform(postCon(rocio, abrir, "/sesiones/abrir-lista"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La sesión aún no inicia."));

        // 10:05: se abre (crea la sesión).
        reloj.fijar(LUNES, LocalTime.of(10, 5));
        String respuesta = mvc.perform(postCon(rocio, abrir, "/sesiones/abrir-lista"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Pase de lista abierto."))
                .andExpect(jsonPath("$.data.sesion.estado").value("LISTA_ABIERTA"))
                .andExpect(jsonPath("$.data.tutorados", hasSize(5)))
                .andExpect(jsonPath("$.data.tutorados[0].nombreCompleto").value("Andrea Morales Pineda"))
                .andExpect(jsonPath("$.data.tutorados[0].asistio").value(nullValue()))
                .andExpect(jsonPath("$.data.resumen.sinMarca").value(5))
                .andExpect(jsonPath("$.data.cierre.puedeCerrar").value(false))
                .andReturn().getResponse().getContentAsString();
        int idSesion = JsonPath.read(respuesta, "$.data.sesion.idSesion");

        // Idempotente.
        mvc.perform(postCon(rocio, abrir, "/sesiones/abrir-lista"))
                .andExpect(jsonPath("$.data.sesion.idSesion").value(idSesion));

        mvc.perform(postCon(rocio, "{}", "/sesiones/{id}/cerrar-lista", idSesion))
                .andExpect(jsonPath("$.message").value("Faltan 5 tutorados por marcar."));
        mvc.perform(postCon(rocio, abrir, "/sesiones/cancelar"))
                .andExpect(jsonPath("$.message").value("No se puede cancelar una sesión con pase de lista."));

        // Pantalla 12: Mariana sumaría 3.
        mvc.perform(putCon(rocio, marcasTodosMenosMariana(), "/sesiones/{id}/lista", idSesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resumen.asistieron").value(4))
                .andExpect(jsonPath("$.data.resumen.noAsistieron").value(1))
                .andExpect(jsonPath("$.data.resumen.sinMarca").value(0))
                .andExpect(jsonPath("$.data.tutorados[?(@.matricula == 'S23017731')].inasistenciasPrevias").value(contains(2)))
                .andExpect(jsonPath("$.data.tutorados[?(@.matricula == 'S23017731')].causaraBaja").value(contains(true)))
                .andExpect(jsonPath("$.data.cierre.todosMarcados").value(true))
                .andExpect(jsonPath("$.data.cierre.sesionIniciada").value(true))
                .andExpect(jsonPath("$.data.cierre.puedeCerrar").value(true))
                .andExpect(jsonPath("$.data.cierre.bajasPrevistas[0].nombreCompleto").value("Mariana López Herrera"))
                .andExpect(jsonPath("$.data.cierre.bajasPrevistas[0].inasistencias").value(3));

        mvc.perform(putCon(rocio, "{\"marcas\":[{\"matricula\":\"S99999999\",\"asistio\":true}]}", "/sesiones/{id}/lista", idSesion))
                .andExpect(jsonPath("$.message").value("S99999999 no está inscrito en esta tutoría."));

        // Pantalla 13: cerrar.
        mvc.perform(postCon(rocio, "{}", "/sesiones/{id}/cerrar-lista", idSesion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Pase de lista cerrado."))
                .andExpect(jsonPath("$.data.estado").value("COMPLETADA"))
                .andExpect(jsonPath("$.data.asistieron").value(4))
                .andExpect(jsonPath("$.data.noAsistieron").value(1))
                .andExpect(jsonPath("$.data.bajasAplicadas", hasSize(1)))
                .andExpect(jsonPath("$.data.bajasAplicadas[0].matricula").value("S23017731"));

        assertThat(jdbc.queryForObject("SELECT estado FROM inscripcion WHERE id_inscripcion = ?", String.class, mariana))
                .isEqualTo("BAJA_AUTOMATICA");
        verify(emailHelper, timeout(3000)).enviarCorreo(eq("s23017731@prueba.mx"),
                org.mockito.ArgumentMatchers.contains("Baja automática"), anyString());
        verify(emailHelper, timeout(3000)).enviarCorreo(eq("s21009342@prueba.mx"),
                org.mockito.ArgumentMatchers.contains("Baja automática"), anyString());

        mvc.perform(putCon(rocio, marcasTodosMenosMariana(), "/sesiones/{id}/lista", idSesion))
                .andExpect(jsonPath("$.message").value("El pase de lista no está abierto."));

        // Ver lista de una sesión completada.
        mvc.perform(getCon(rocio, "/sesiones/{id}/lista", idSesion))
                .andExpect(jsonPath("$.data.sesion.estado").value("COMPLETADA"))
                .andExpect(jsonPath("$.data.tutorados", hasSize(5)))
                .andExpect(jsonPath("$.data.tutorados[?(@.matricula == 'S23017731')].inasistenciasPrevias").value(contains(2)))
                .andExpect(jsonPath("$.data.cierre.bajasPrevistas", empty()));

        // Pantalla 14.
        mvc.perform(getCon(rocio, "/tutorias/{id}/historial-asistencia", ed))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resumen.sesionesCompletadas").value(3))
                .andExpect(jsonPath("$.data.resumen.marcasTotales").value(15))
                .andExpect(jsonPath("$.data.resumen.marcasAsistio").value(12))
                .andExpect(jsonPath("$.data.resumen.asistenciaPct").value(80))
                .andExpect(jsonPath("$.data.resumen.bajasAutomaticas").value(1))
                .andExpect(jsonPath("$.data.tutoria.cupo.ocupados").value(4))
                .andExpect(jsonPath("$.data.sesiones", hasSize(3)))
                .andExpect(jsonPath("$.data.tutorados[?(@.matricula == 'S23017731')].estadoInscripcion").value(contains("BAJA_AUTOMATICA")))
                .andExpect(jsonPath("$.data.tutorados[?(@.matricula == 'S23017731')].idSesionBaja").value(contains(idSesion)))
                .andExpect(jsonPath("$.data.tutorados[?(@.matricula == 'S23017731')].falto").value(contains(3)))
                .andExpect(jsonPath("$.data.tutorados[?(@.matricula == 'S23016620')].marcas." + idSesion).value(contains(true)));
    }

    @Test
    void cancelarSesionFuturaConReglaDe15Minutos() throws Exception {
        reloj.fijar(LUNES, LocalTime.of(10, 5));
        String miercoles7 = "{\"idHorario\":" + miercoles + ",\"fecha\":\"2026-10-07\"}";

        mvc.perform(getCon(rocio, "/tutorias/{id}/sesiones", ed))
                .andExpect(jsonPath("$.data[0].fecha").value("2026-10-07"))
                .andExpect(jsonPath("$.data[0].estado").value("PROGRAMADA"))
                .andExpect(jsonPath("$.data[0].acciones.cancelar").value(true))
                .andExpect(jsonPath("$.data[1].fecha").value("2026-10-05"))
                .andExpect(jsonPath("$.data[1].acciones.abrirLista").value(true))
                .andExpect(jsonPath("$.data[*].estado", hasItem("COMPLETADA")));

        mvc.perform(postCon(rocio, "{\"idHorario\":" + miercoles + ",\"fecha\":\"2026-10-08\"}", "/sesiones/cancelar"))
                .andExpect(jsonPath("$.message").value("La fecha no corresponde al día del horario."));

        mvc.perform(postCon(rocio, miercoles7, "/sesiones/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sesión cancelada."))
                .andExpect(jsonPath("$.data.estado").value("CANCELADA"))
                .andExpect(jsonPath("$.data.idSesion").isNumber());
        verify(emailHelper, timeout(3000)).enviarCorreo(eq("s23016620@prueba.mx"),
                org.mockito.ArgumentMatchers.contains("Sesión cancelada"), anyString());

        mvc.perform(postCon(rocio, miercoles7, "/sesiones/cancelar"))
                .andExpect(jsonPath("$.message").value("La sesión ya está cancelada."));

        // El miércoles no hay recordatorio para la sesión cancelada.
        reloj.fijar(LUNES.plusDays(2), LocalTime.of(8, 30));
        assertThat(sesionService.procesarRecordatorios()).isZero();

        // A menos de 15 minutos ya no se puede cancelar.
        reloj.fijar(LUNES.plusDays(7), LocalTime.of(9, 50));
        mvc.perform(postCon(rocio, "{\"idHorario\":" + lunes + ",\"fecha\":\"2026-10-12\"}", "/sesiones/cancelar"))
                .andExpect(jsonPath("$.message").value("Ya no puedes cancelar: faltan menos de 15 minutos para la sesión."));
    }

    @Test
    void recordatoriosUnaSolaVezDosHorasAntes() throws Exception {
        reloj.fijar(LUNES, LocalTime.of(7, 59));
        assertThat(sesionService.procesarRecordatorios()).isZero();

        reloj.fijar(LUNES, LocalTime.of(8, 0));
        assertThat(sesionService.procesarRecordatorios()).isEqualTo(5);
        reloj.fijar(LUNES, LocalTime.of(9, 59));
        assertThat(sesionService.procesarRecordatorios()).isZero();
        verify(emailHelper, timeout(3000)).enviarCorreo(eq("s23017731@prueba.mx"),
                eq("Recordatorio · Estructuras de Datos a las 10:00"), anyString());
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM recordatorio WHERE fecha = '2026-10-05'", Integer.class))
                .isEqualTo(5);

        reloj.fijar(LUNES, LocalTime.of(10, 0));
        assertThat(sesionService.procesarRecordatorios()).isZero();
    }

    @Test
    void avisoDeListaSinCerrarYListasPendientes() throws Exception {
        reloj.fijar(LUNES, LocalTime.of(10, 5));
        mvc.perform(postCon(rocio, "{\"idHorario\":" + lunes + ",\"fecha\":\"2026-10-05\"}", "/sesiones/abrir-lista"))
                .andExpect(status().isOk());

        assertThat(sesionService.procesarAvisosDeCierre()).isZero();

        reloj.fijar(LUNES, LocalTime.of(12, 0));
        mvc.perform(getCon(rocio, "/sesiones/hoy"))
                .andExpect(jsonPath("$.data.sesiones[0].estado").value("LISTA_ABIERTA"))
                .andExpect(jsonPath("$.data.sesiones[0].terminada").value(true));
        assertThat(sesionService.procesarAvisosDeCierre()).isEqualTo(1);
        assertThat(sesionService.procesarAvisosDeCierre()).isZero();
        verify(emailHelper, timeout(3000)).enviarCorreo(eq("s21009342@prueba.mx"),
                org.mockito.ArgumentMatchers.contains("Pase de lista sin cerrar"), anyString());

        // Al día siguiente sigue pendiente; no se puede abrir la sesión de un día pasado.
        reloj.fijar(LUNES.plusDays(1), LocalTime.of(9, 0));
        mvc.perform(getCon(rocio, "/sesiones/hoy"))
                .andExpect(jsonPath("$.data.sesiones", empty()))
                .andExpect(jsonPath("$.data.listasSinCerrar", hasSize(1)))
                .andExpect(jsonPath("$.data.listasSinCerrar[0].fecha").value("2026-10-05"));
        mvc.perform(postCon(rocio, "{\"idHorario\":" + miercoles + ",\"fecha\":\"2026-09-30\"}", "/sesiones/abrir-lista"))
                .andExpect(jsonPath("$.message").value("Solo puedes pasar lista el día de la sesión."));

        // No se puede desactivar con lista abierta.
        mvc.perform(patchCon(rocio, "{\"estado\":\"INACTIVA\"}", "/tutorias/{id}/estado", ed))
                .andExpect(jsonPath("$.message").value("Cierra el pase de lista abierto antes de desactivar."));
    }

    @Test
    void otroTutorNoPuedeOperarSesiones() throws Exception {
        String jorge = usuario("S21004411", "Jorge", "Castillo Rivera", "TUTOR");
        reloj.fijar(LUNES, LocalTime.of(10, 5));
        mvc.perform(postCon(jorge, "{\"idHorario\":" + lunes + ",\"fecha\":\"2026-10-05\"}", "/sesiones/abrir-lista"))
                .andExpect(jsonPath("$.message").value("No tienes permiso sobre este recurso."));
        mvc.perform(getCon(jorge, "/tutorias/{id}/historial-asistencia", ed))
                .andExpect(jsonPath("$.message").value("No tienes permiso sobre este recurso."));
    }
}
