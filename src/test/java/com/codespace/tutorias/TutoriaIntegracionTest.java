package com.codespace.tutorias;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

/** Pantallas 03, 04, 06, 09, 10, 11 y 16. */
class TutoriaIntegracionTest extends PruebaIntegracion {

    private static final String LUN_MIE_10_12 = "[" +
            "{\"dia\":\"LUNES\",\"horaInicio\":\"10:00\",\"horaFin\":\"12:00\",\"temas\":[\"Listas enlazadas\",\"Pilas y colas\",\"Recursividad\"]}," +
            "{\"dia\":\"miércoles\",\"horaInicio\":\"10:00\",\"horaFin\":\"12:00\",\"temas\":[\"Árboles binarios de búsqueda\"]}]";

    private String rocio;     // tutora
    private String jorge;     // tutor
    private String valeria;   // tutorada
    private String admin;
    private int estructuras;  // idMateria
    private int basesDatos;

    @BeforeEach
    void datos() {
        rocio = usuario("S21009342", "Rocío", "Hernández Aguilar", "TUTOR");
        jorge = usuario("S21004411", "Jorge", "Castillo Rivera", "TUTOR");
        valeria = usuario("S23014587", "Valeria", "Mendoza Cruz", "TUTORADO");
        admin = usuario("25480", "Adriana", "Salas Ortega", "ADMIN");
        estructuras = materia("Estructuras de Datos", 48215);
        basesDatos = materia("Bases de Datos", 48241);
    }

    @Test
    void crearTutoriaComoEnLaPantalla10() throws Exception {
        int redes = materia("Redes de Computadoras", 52310);

        mvc.perform(postCon(rocio, "{\"idMateria\":" + redes + ",\"edificio\":1,\"aula\":10,\"horarios\":[" +
                        "{\"dia\":\"VIERNES\",\"horaInicio\":\"12:00\",\"horaFin\":\"14:00\",\"temas\":[\"Enrutamiento estático\",\"VLAN\"]}," +
                        "{\"dia\":\"MARTES\",\"horaInicio\":\"08:00\",\"horaFin\":\"10:00\",\"temas\":[\"Modelo OSI\",\"Direccionamiento IPv4\",\"Cálculo de subredes\"]}]}",
                        "/tutorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tutoría creada."))
                .andExpect(jsonPath("$.data.materia.nombre").value("Redes de Computadoras"))
                .andExpect(jsonPath("$.data.materia.nrc").value(52310))
                .andExpect(jsonPath("$.data.tutor.nombreCompleto").value("Rocío Hernández Aguilar"))
                .andExpect(jsonPath("$.data.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.data.edificio").value(1))
                .andExpect(jsonPath("$.data.aula").value(10))
                // Ordenados por día de la semana; horas en HH:mm
                .andExpect(jsonPath("$.data.horarios[0].dia").value("MARTES"))
                .andExpect(jsonPath("$.data.horarios[0].horaInicio").value("08:00"))
                .andExpect(jsonPath("$.data.horarios[0].temas", hasSize(3)))
                .andExpect(jsonPath("$.data.horarios[1].dia").value("VIERNES"))
                .andExpect(jsonPath("$.data.horarios[1].temas[1].tema").value("VLAN"))
                .andExpect(jsonPath("$.data.cupo.ocupados").value(0))
                .andExpect(jsonPath("$.data.cupo.maximo").value(7))
                .andExpect(jsonPath("$.data.cupo.disponibles").value(7))
                .andExpect(jsonPath("$.data.esDueno").value(true))
                .andExpect(jsonPath("$.data.panelTutorado").value(nullValue()))
                // Lunes 08:00 -> próxima: martes 6, recordatorio 06:00
                .andExpect(jsonPath("$.data.proximaSesion.fecha").value("2026-10-06"))
                .andExpect(jsonPath("$.data.proximaSesion.recordatorioA").value("06:00"));
    }

    @Test
    void validacionesAlCrear() throws Exception {
        String base = "{\"idMateria\":" + estructuras + ",\"edificio\":2,\"aula\":7,\"horarios\":";

        mvc.perform(postCon(rocio, base + "[{\"dia\":\"SABADO\",\"horaInicio\":\"10:00\",\"horaFin\":\"12:00\"}]}", "/tutorias"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Día inválido: solo de lunes a viernes."));

        mvc.perform(postCon(rocio, base + "[{\"dia\":\"LUNES\",\"horaInicio\":\"12:00\",\"horaFin\":\"10:00\"}]}", "/tutorias"))
                .andExpect(jsonPath("$.message").value("La hora de fin debe ser posterior a la de inicio."));

        mvc.perform(postCon(rocio, base + "[{\"dia\":\"LUNES\",\"horaInicio\":\"10:00\",\"horaFin\":\"12:00\"}," +
                        "{\"dia\":\"LUNES\",\"horaInicio\":\"11:00\",\"horaFin\":\"13:00\"}]}", "/tutorias"))
                .andExpect(jsonPath("$.message").value("Los horarios 1 y 2 se traslapan."));

        mvc.perform(postCon(rocio, base + "[]}", "/tutorias"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.horarios").value("Agrega al menos un horario."));

        mvc.perform(postCon(rocio, "{\"idMateria\":" + estructuras + ",\"edificio\":9,\"aula\":7,\"horarios\":" +
                        "[{\"dia\":\"LUNES\",\"horaInicio\":\"10:00\",\"horaFin\":\"12:00\"}]}", "/tutorias"))
                .andExpect(jsonPath("$.message").value("El aula seleccionada no existe."));

        String temas = "\"T1\",\"T2\",\"T3\",\"T4\",\"T5\",\"T6\",\"T7\",\"T8\",\"T9\",\"T10\",\"T11\"";
        mvc.perform(postCon(rocio, base + "[{\"dia\":\"LUNES\",\"horaInicio\":\"10:00\",\"horaFin\":\"12:00\",\"temas\":[" + temas + "]}]}", "/tutorias"))
                .andExpect(jsonPath("$.message").value("Máximo 10 temas por horario."));

        // Contiguos sí se permiten (10–12 y 12–14).
        crearTutoria(rocio, estructuras, 2, 7, "[{\"dia\":\"LUNES\",\"horaInicio\":\"10:00\",\"horaFin\":\"12:00\"}," +
                "{\"dia\":\"LUNES\",\"horaInicio\":\"12:00\",\"horaFin\":\"14:00\"}]");

        // Choque con otra tutoría del mismo tutor.
        mvc.perform(postCon(rocio, "{\"idMateria\":" + basesDatos + ",\"edificio\":1,\"aula\":3,\"horarios\":" +
                        "[{\"dia\":\"LUNES\",\"horaInicio\":\"11:00\",\"horaFin\":\"13:00\"}]}", "/tutorias"))
                .andExpect(jsonPath("$.message").value("El horario del lunes 11:00–13:00 choca con tu tutoría de Estructuras de Datos."));

        // Aula ocupada por otro tutor.
        mvc.perform(postCon(jorge, "{\"idMateria\":" + basesDatos + ",\"edificio\":2,\"aula\":7,\"horarios\":" +
                        "[{\"dia\":\"LUNES\",\"horaInicio\":\"09:00\",\"horaFin\":\"11:00\"}]}", "/tutorias"))
                .andExpect(jsonPath("$.message").value("El Edificio 2 · Aula 7 está ocupado el lunes de 10:00 a 12:00."));
    }

    @Test
    void soloTutoresCreanYTutoradoNoVeCatalogoDeMaterias() throws Exception {
        mvc.perform(postCon(valeria, "{\"idMateria\":" + estructuras + ",\"edificio\":1,\"aula\":1,\"horarios\":" +
                        "[{\"dia\":\"LUNES\",\"horaInicio\":\"10:00\",\"horaFin\":\"12:00\"}]}", "/tutorias"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("No tienes permiso para esta acción."));
        mvc.perform(getCon(valeria, "/materias")).andExpect(status().isForbidden());
        mvc.perform(getCon(rocio, "/catalogos/edificios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].edificio").value(1))
                .andExpect(jsonPath("$.data[0].aulas", hasSize(12)));
    }

    @Test
    void explorarFiltraYMarcaChoques() throws Exception {
        int ed = crearTutoria(rocio, estructuras, 2, 7, LUN_MIE_10_12);
        int bd = crearTutoria(jorge, basesDatos, 1, 9, "[{\"dia\":\"MARTES\",\"horaInicio\":\"16:00\",\"horaFin\":\"18:00\"}," +
                "{\"dia\":\"JUEVES\",\"horaInicio\":\"16:00\",\"horaFin\":\"18:00\"}]");
        String daniela = usuario("S21007711", "Daniela", "Ortiz Méndez", "TUTOR");
        int prob = crearTutoria(daniela, materia("Probabilidad y Estadística", 47790), 1, 6,
                "[{\"dia\":\"JUEVES\",\"horaInicio\":\"17:00\",\"horaFin\":\"19:00\"}]");
        int calculo = crearTutoria(rocio, materia("Cálculo Integral", 47718), 1, 3,
                "[{\"dia\":\"MARTES\",\"horaInicio\":\"12:00\",\"horaFin\":\"14:00\"}]");
        for (int i = 0; i < 7; i++) {
            String m = String.format("S230000%02d", i);
            usuario(m, "Tutorado", "Lleno " + i, "TUTORADO");
            inscribir(calculo, m);
        }
        inscribir(bd, "S23014587"); // Valeria ya está en Bases de Datos

        mvc.perform(getCon(valeria, "/tutorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(3)))             // sin Bases de Datos (inscrita)
                .andExpect(jsonPath("$.data[?(@.idTutoria == " + calculo + ")].cupo.disponibles").value(contains(0)))
                .andExpect(jsonPath("$.data[?(@.idTutoria == " + prob + ")].choques[0].dia").value(contains("JUEVES")))
                .andExpect(jsonPath("$.data[?(@.idTutoria == " + prob + ")].choques[0].conTutoria.materia").value(contains("Bases de Datos")))
                .andExpect(jsonPath("$.data[?(@.idTutoria == " + prob + ")].choques[0].conTutoria.horaInicio").value(contains("16:00")))
                .andExpect(jsonPath("$.data[?(@.idTutoria == " + ed + ")].choques", contains(empty())));

        mvc.perform(getCon(valeria, "/tutorias?soloConLugar=true&ocultarChoques=true"))
                .andExpect(jsonPath("$.data[*].idTutoria", contains(ed)));
        mvc.perform(getCon(valeria, "/tutorias?dia=Miércoles"))
                .andExpect(jsonPath("$.data[*].idTutoria", contains(ed)));
        mvc.perform(getCon(valeria, "/tutorias?q=rocio hernandez"))
                .andExpect(jsonPath("$.data[*].materia.nombre", containsInAnyOrder("Cálculo Integral", "Estructuras de Datos")));
        mvc.perform(getCon(valeria, "/tutorias?q=4779"))
                .andExpect(jsonPath("$.data[*].idTutoria", contains(prob)));
    }

    @Test
    void panelDeInscripcionEnSusSeisEstados() throws Exception {
        int ed = crearTutoria(rocio, estructuras, 2, 7, LUN_MIE_10_12);
        String url = "/tutorias/" + ed;

        mvc.perform(getCon(valeria, url))
                .andExpect(jsonPath("$.data.panelTutorado.estado").value("CON_LUGAR"))
                .andExpect(jsonPath("$.data.panelTutorado.puedeInscribirse").value(true))
                .andExpect(jsonPath("$.data.panelTutorado.inscripcion").value(nullValue()))
                .andExpect(jsonPath("$.data.horarios[0].temas[0].tema").value("Listas enlazadas"))
                .andExpect(jsonPath("$.data.esDueno").value(false))
                .andExpect(jsonPath("$.data.proximaSesion.fecha").value("2026-10-05"))
                .andExpect(jsonPath("$.data.proximaSesion.horaInicio").value("10:00"));

        // Después de la sesión del lunes, la próxima es el miércoles.
        reloj.fijar(LUNES, LocalTime.of(12, 30));
        mvc.perform(getCon(valeria, url)).andExpect(jsonPath("$.data.proximaSesion.fecha").value("2026-10-07"));

        // Choque con una inscripción en otra tutoría.
        int otra = crearTutoria(jorge, basesDatos, 1, 9, "[{\"dia\":\"MIERCOLES\",\"horaInicio\":\"11:00\",\"horaFin\":\"13:00\"}]");
        int idOtra = inscribir(otra, "S23014587");
        mvc.perform(getCon(valeria, url))
                .andExpect(jsonPath("$.data.panelTutorado.estado").value("CHOQUE_HORARIO"))
                .andExpect(jsonPath("$.data.panelTutorado.puedeInscribirse").value(false))
                .andExpect(jsonPath("$.data.panelTutorado.choques[0].conTutoria.materia").value("Bases de Datos"));
        jdbc.update("UPDATE inscripcion SET estado = 'BAJA', fecha_baja = now() WHERE id_inscripcion = ?", idOtra);

        // Llena.
        for (int i = 0; i < 7; i++) {
            String m = String.format("S230000%02d", i);
            usuario(m, "Tutorado", "Lleno " + i, "TUTORADO");
            inscribir(ed, m);
        }
        mvc.perform(getCon(valeria, url))
                .andExpect(jsonPath("$.data.panelTutorado.estado").value("LLENA"))
                .andExpect(jsonPath("$.data.cupo.disponibles").value(0));
        jdbc.update("DELETE FROM inscripcion WHERE id_tutoria = ?", ed);

        // Inscripción activa.
        int idInscripcion = inscribir(ed, "S23014587");
        mvc.perform(getCon(valeria, url))
                .andExpect(jsonPath("$.data.panelTutorado.estado").value("INSCRIPCION_ACTIVA"))
                .andExpect(jsonPath("$.data.panelTutorado.inscripcion.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.data.panelTutorado.inscripcion.inasistencias").value(0))
                .andExpect(jsonPath("$.data.panelTutorado.inscripcion.inasistenciasPermitidas").value(2));

        // Baja automática (prevalece sobre todo).
        jdbc.update("UPDATE inscripcion SET estado = 'BAJA_AUTOMATICA', fecha_baja = now() WHERE id_inscripcion = ?", idInscripcion);
        mvc.perform(getCon(valeria, url))
                .andExpect(jsonPath("$.data.panelTutorado.estado").value("BAJA_AUTOMATICA"))
                .andExpect(jsonPath("$.data.panelTutorado.inscripcion.estado").value("BAJA_AUTOMATICA"))
                .andExpect(jsonPath("$.data.panelTutorado.inscripcion.fechaBaja", endsWith("-06:00")));

        // No activa (para otro tutorado).
        String diego = usuario("S23011873", "Diego", "Ramírez Soto", "TUTORADO");
        mvc.perform(patchCon(rocio, "{\"estado\":\"INACTIVA\"}", "/tutorias/{id}/estado", ed)).andExpect(status().isOk());
        mvc.perform(getCon(diego, url))
                .andExpect(jsonPath("$.data.panelTutorado.estado").value("NO_ACTIVA"))
                .andExpect(jsonPath("$.data.proximaSesion").value(nullValue()));
    }

    @Test
    void editarSoloSinInscritosYConservaHistorialDeHorarios() throws Exception {
        int ed = crearTutoria(rocio, estructuras, 2, 7, LUN_MIE_10_12);
        int lunes = horario(ed, "LUNES");
        int miercoles = horario(ed, "MIERCOLES");

        String body = "{\"edificio\":2,\"aula\":4,\"horarios\":[" +
                "{\"idHorario\":" + lunes + ",\"dia\":\"LUNES\",\"horaInicio\":\"10:00\",\"horaFin\":\"12:00\",\"temas\":[\"Listas enlazadas\",\"Grafos\"]}," +
                "{\"idHorario\":" + miercoles + ",\"dia\":\"JUEVES\",\"horaInicio\":\"10:00\",\"horaFin\":\"12:00\",\"temas\":[\"Montículos\"]}]}";

        mvc.perform(putCon(jorge, body, "/tutorias/{id}", ed))
                .andExpect(jsonPath("$.message").value("No tienes permiso sobre este recurso."));

        mvc.perform(putCon(rocio, body, "/tutorias/{id}", ed))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.aula").value(4))
                .andExpect(jsonPath("$.data.horarios[*].dia", contains("LUNES", "JUEVES")))
                .andExpect(jsonPath("$.data.horarios[0].idHorario").value(lunes))
                .andExpect(jsonPath("$.data.horarios[0].temas[*].tema", contains("Listas enlazadas", "Grafos")))
                .andExpect(jsonPath("$.data.horarios[1].temas[*].tema", contains("Montículos")));

        assertThat(jdbc.queryForObject("SELECT estado FROM horario WHERE id_horario = ?", String.class, miercoles))
                .isEqualTo("INACTIVO");

        inscribir(ed, "S23014587");
        mvc.perform(putCon(rocio, body, "/tutorias/{id}", ed))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("No se pueden cambiar horarios ni lugar con tutorados inscritos."));
    }

    @Test
    void temasSeEditanConInscritos() throws Exception {
        int ed = crearTutoria(rocio, estructuras, 2, 7, LUN_MIE_10_12);
        inscribir(ed, "S23014587");
        int lunes = horario(ed, "LUNES");

        String respuesta = mvc.perform(postCon(rocio, "{\"tema\":\"  Tablas hash  \"}", "/horarios/{id}/temas", lunes))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tema agregado."))
                .andExpect(jsonPath("$.data.tema").value("Tablas hash"))
                .andReturn().getResponse().getContentAsString();
        Integer idTema = com.jayway.jsonpath.JsonPath.read(respuesta, "$.data.idTema");

        mvc.perform(postCon(rocio, "{\"tema\":\"tablas HASH\"}", "/horarios/{id}/temas", lunes))
                .andExpect(jsonPath("$.message").value("El tema ya existe en este horario."));
        mvc.perform(postCon(jorge, "{\"tema\":\"Otro\"}", "/horarios/{id}/temas", lunes))
                .andExpect(jsonPath("$.message").value("No tienes permiso sobre este recurso."));
        mvc.perform(postCon(rocio, "{\"tema\":\"" + "x".repeat(61) + "\"}", "/horarios/{id}/temas", lunes))
                .andExpect(jsonPath("$.message").value("El tema no puede exceder 60 caracteres."));

        mvc.perform(deleteCon(rocio, "/temas/{id}", idTema))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tema eliminado."));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM temas WHERE id_horario = ?", Integer.class, lunes)).isEqualTo(3);
    }

    @Test
    void desactivarAvisaAInscritosYMisTutoriasMuestraRiesgo() throws Exception {
        int ed = crearTutoria(rocio, estructuras, 2, 7, LUN_MIE_10_12);
        int lunes = horario(ed, "LUNES");
        usuario("S23017731", "Mariana", "López Herrera", "TUTORADO");
        int mariana = inscribir(ed, "S23017731");
        int valeriaIns = inscribir(ed, "S23014587");
        sesionCerrada(lunes, LUNES.minusWeeks(2), Map.of(mariana, false, valeriaIns, true));
        sesionCerrada(lunes, LUNES.minusWeeks(1), Map.of(mariana, false, valeriaIns, true));

        mvc.perform(getCon(rocio, "/tutorias/mis-tutorias"))
                .andExpect(jsonPath("$.data[0].idTutoria").value(ed))
                .andExpect(jsonPath("$.data[0].cupo.ocupados").value(2))
                .andExpect(jsonPath("$.data[0].tutoradosEnRiesgo").value(1))
                .andExpect(jsonPath("$.data[0].proximaSesion.fecha").value("2026-10-05"));

        mvc.perform(getCon(rocio, "/tutorias/{id}/inscritos", ed))
                .andExpect(jsonPath("$.data.cupo.ocupados").value(2))
                .andExpect(jsonPath("$.data.inscritos[0].nombreCompleto").value("Mariana López Herrera"))
                .andExpect(jsonPath("$.data.inscritos[0].inasistencias").value(2))
                .andExpect(jsonPath("$.data.inscritos[0].enRiesgo").value(true))
                .andExpect(jsonPath("$.data.inscritos[1].enRiesgo").value(false));

        mvc.perform(patchCon(jorge, "{\"estado\":\"INACTIVA\"}", "/tutorias/{id}/estado", ed))
                .andExpect(jsonPath("$.message").value("No tienes permiso sobre este recurso."));
        mvc.perform(patchCon(rocio, "{\"estado\":\"ACTIVA\"}", "/tutorias/{id}/estado", ed))
                .andExpect(jsonPath("$.message").value("Estado inválido."));

        mvc.perform(patchCon(rocio, "{\"estado\":\"INACTIVA\"}", "/tutorias/{id}/estado", ed))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tutoría desactivada."));
        verify(emailHelper, timeout(3000)).enviarCorreo(eq("s23017731@prueba.mx"), org.mockito.ArgumentMatchers.contains("Tutoría finalizada"), anyString());
        verify(emailHelper, timeout(3000)).enviarCorreo(eq("s23014587@prueba.mx"), org.mockito.ArgumentMatchers.contains("Tutoría finalizada"), anyString());

        mvc.perform(patchCon(admin, "{\"estado\":\"INACTIVA\"}", "/tutorias/{id}/estado", ed))
                .andExpect(jsonPath("$.message").value("La tutoría ya está inactiva."));
        mvc.perform(getCon(rocio, "/tutorias/mis-tutorias")).andExpect(jsonPath("$.data", hasSize(0)));
        mvc.perform(getCon(rocio, "/tutorias/mis-tutorias?estado=INACTIVA")).andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    void experienciasEducativasDelAdmin() throws Exception {
        mvc.perform(postCon(admin, "{\"nombre\":\"Redes de Computadoras\",\"nrc\":52310}", "/materias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Experiencia educativa creada."))
                .andExpect(jsonPath("$.data.nrc").value(52310))
                .andExpect(jsonPath("$.data.tutoriasActivas").value(0));

        mvc.perform(postCon(admin, "{\"nombre\":\"Arquitectura de Computadoras\",\"nrc\":52310}", "/materias"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Este NRC ya está registrado para Redes de Computadoras."));

        mvc.perform(postCon(rocio, "{\"nombre\":\"X\",\"nrc\":1}", "/materias")).andExpect(status().isForbidden());

        crearTutoria(rocio, estructuras, 2, 7, LUN_MIE_10_12);
        mvc.perform(getCon(admin, "/materias"))
                .andExpect(jsonPath("$.data[*].nombre", contains("Bases de Datos", "Estructuras de Datos", "Redes de Computadoras")))
                .andExpect(jsonPath("$.data[1].tutoriasActivas").value(1));
        mvc.perform(getCon(admin, "/materias?q=estructuras")).andExpect(jsonPath("$.data", hasSize(1)));

        mvc.perform(deleteCon(admin, "/materias/{id}", estructuras))
                .andExpect(jsonPath("$.message").value("No se puede eliminar: la experiencia educativa tiene tutorías registradas."));
        mvc.perform(putCon(admin, "{\"nombre\":\"Bases de Datos I\",\"nrc\":48241}", "/materias/{id}", basesDatos))
                .andExpect(jsonPath("$.data.nombre").value("Bases de Datos I"));
        mvc.perform(deleteCon(admin, "/materias/{id}", basesDatos))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Experiencia educativa eliminada."));
    }
}
