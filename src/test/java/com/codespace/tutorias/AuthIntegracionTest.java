package com.codespace.tutorias;

import com.codespace.tutorias.Exceptions.BusinessException;
import com.codespace.tutorias.JWT.IdentidadVerificada;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Pantallas 01 y 02 + docs/AUTENTICACION.md. */
class AuthIntegracionTest extends PruebaIntegracion {

    private static final IdentidadVerificada SOFIA = new IdentidadVerificada(
            "MICROSOFT", "sub-sofia", "zs24011236@estudiantes.uv.mx", "Sofía", "Ramos Gutiérrez");

    private String postJson(String url, String json) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))
                .andReturn().getResponse().getContentAsString();
    }

    private String tokenRegistroDeSofia() throws Exception {
        when(verificadorIdToken.verificarMicrosoft("id-token-sofia")).thenReturn(SOFIA);
        String respuesta = postJson("/auth/microsoft", "{\"idToken\":\"id-token-sofia\"}");
        return JsonPath.read(respuesta, "$.data.tokenRegistro");
    }

    @Test
    void primerAccesoDevuelveTokenRegistroYPerfil() throws Exception {
        when(verificadorIdToken.verificarMicrosoft("id-token-sofia")).thenReturn(SOFIA);

        mvc.perform(post("/auth/microsoft").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"id-token-sofia\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registro pendiente"))
                .andExpect(jsonPath("$.data.registroPendiente").value(true))
                .andExpect(jsonPath("$.data.tokenRegistro").isString())
                .andExpect(jsonPath("$.data.token").doesNotExist())
                .andExpect(jsonPath("$.data.perfil.nombre").value("Sofía"))
                .andExpect(jsonPath("$.data.perfil.apellidos").value("Ramos Gutiérrez"))
                .andExpect(jsonPath("$.data.perfil.correo").value("zs24011236@estudiantes.uv.mx"))
                .andExpect(jsonPath("$.data.perfil.proveedor").value("MICROSOFT"));
    }

    @Test
    void registroCreaUsuarioNormalizaMatriculaYPermiteUsarLaApi() throws Exception {
        String tokenRegistro = tokenRegistroDeSofia();

        String respuesta = postJson("/auth/registro",
                "{\"tokenRegistro\":\"" + tokenRegistro + "\",\"matricula\":\"zs24011236\",\"rol\":\"TUTORADO\"}");

        assertThat((String) JsonPath.read(respuesta, "$.message")).isEqualTo("Registro exitoso.");
        assertThat((Boolean) JsonPath.read(respuesta, "$.data.registroPendiente")).isFalse();
        assertThat((String) JsonPath.read(respuesta, "$.data.usuario.matricula")).isEqualTo("S24011236");
        assertThat((String) JsonPath.read(respuesta, "$.data.usuario.rol")).isEqualTo("TUTORADO");

        String token = JsonPath.read(respuesta, "$.data.token");
        mvc.perform(getCon(token, "/usuarios/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.matricula").value("S24011236"))
                .andExpect(jsonPath("$.data.correo").value("zs24011236@estudiantes.uv.mx"))
                .andExpect(jsonPath("$.data.proveedor").value("MICROSOFT"));

        // Segundo login: ya registrado, entra directo.
        mvc.perform(post("/auth/microsoft").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"id-token-sofia\"}"))
                .andExpect(jsonPath("$.message").value("Login"))
                .andExpect(jsonPath("$.data.registroPendiente").value(false))
                .andExpect(jsonPath("$.data.usuario.matricula").value("S24011236"));
    }

    @Test
    void registroRechazaRolAdminYMatriculaInvalidaORepetida() throws Exception {
        String tokenRegistro = tokenRegistroDeSofia();

        mvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tokenRegistro\":\"" + tokenRegistro + "\",\"matricula\":\"S24011236\",\"rol\":\"ADMIN\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Rol inválido."));

        mvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tokenRegistro\":\"" + tokenRegistro + "\",\"matricula\":\"24011236\",\"rol\":\"TUTOR\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Matrícula inválida: debe ser S seguida de 8 dígitos (p. ej. S24011236)."));

        usuario("S24011236", "Otra", "Persona", "TUTORADO");
        mvc.perform(post("/auth/registro").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tokenRegistro\":\"" + tokenRegistro + "\",\"matricula\":\"S24011236\",\"rol\":\"TUTOR\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("La matrícula ya está registrada con otra cuenta."));
    }

    @Test
    void preRegistroDeAdminSeLigaPorCorreoEnElPrimerLogin() throws Exception {
        jdbc.update("INSERT INTO usuario (matricula, nombre, apellidos, correo, id_rol) " +
                "VALUES ('25480', 'Adriana', 'Salas Ortega', 'asalas@gmail.com', (SELECT id_rol FROM rol WHERE rol='ADMIN'))");
        when(verificadorIdToken.verificarGoogle("id-token-adriana")).thenReturn(new IdentidadVerificada(
                "GOOGLE", "sub-adriana", "asalas@gmail.com", "Adriana", "Salas Ortega"));

        mvc.perform(post("/auth/google").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"id-token-adriana\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.registroPendiente").value(false))
                .andExpect(jsonPath("$.data.usuario.rol").value("ADMIN"))
                .andExpect(jsonPath("$.data.usuario.matricula").value("25480"));

        assertThat(jdbc.queryForObject("SELECT id_externo FROM usuario WHERE matricula='25480'", String.class))
                .isEqualTo("sub-adriana");
    }

    @Test
    void errorDelProveedorSeDevuelveComo400() throws Exception {
        when(verificadorIdToken.verificarGoogle("malo"))
                .thenThrow(new BusinessException("Token del proveedor inválido o expirado."));

        mvc.perform(post("/auth/google").contentType(MediaType.APPLICATION_JSON).content("{\"idToken\":\"malo\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Token del proveedor inválido o expirado."));
    }

    @Test
    void tokenDeRegistroNoSirveComoSesionYSinTokenEs401() throws Exception {
        String tokenRegistro = tokenRegistroDeSofia();

        mvc.perform(getCon(tokenRegistro, "/usuarios/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Token inválido o expirado."));

        mvc.perform(get("/usuarios/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Debes iniciar sesión."));
    }

    @Test
    void loginDeDesarrolloEntraPorCorreo() throws Exception {
        usuario("S21009342", "Rocío", "Hernández Aguilar", "TUTOR");

        mvc.perform(post("/auth/dev").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"S21009342@prueba.mx\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.usuario.rol").value("TUTOR"))
                .andExpect(jsonPath("$.data.token").isString());
    }
}
