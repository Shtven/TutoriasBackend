package com.codespace.tutorias;

import com.jayway.jsonpath.JsonPath;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** POST /auth/signin y /auth/signup + docs/AUTENTICACION.md. */
class AuthIntegracionTest extends PruebaIntegracion {

    private static final String CREDENCIALES = "Usuario o contraseña incorrectos.";

    private ResultActions postJson(String url, String json) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private ResultActions signin(String campo, String valor, String pwd) throws Exception {
        return postJson("/auth/signin", "{\"" + campo + "\":\"" + valor + "\",\"pwd\":\"" + pwd + "\"}");
    }

    private static String signupJson(String matricula, String correo, String pwd, String rol) {
        return "{\"nombre\":\"Sofía\",\"apellidos\":\"Ramos Gutiérrez\",\"matricula\":\"" + matricula
                + "\",\"correo\":\"" + correo + "\",\"pwd\":\"" + pwd + "\",\"rol\":\"" + rol + "\"}";
    }

    private String tokenAdmin() {
        return usuario("25480", "Adriana", "Salas Ortega", "ADMIN");
    }

    // ------------------------------------------------------------------
    // Signup
    // ------------------------------------------------------------------

    @Test
    void signupCreaTutoradoConContrasenaCifradaYEntraDirecto() throws Exception {
        String respuesta = postJson("/auth/signup", signupJson("zs24011236", "ZS24011236@estudiantes.uv.mx", "secreta12", "tutorado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Cuenta creada."))
                .andExpect(jsonPath("$.data.usuario.matricula").value("S24011236"))
                .andExpect(jsonPath("$.data.usuario.correo").value("zs24011236@estudiantes.uv.mx"))
                .andExpect(jsonPath("$.data.usuario.rol").value("TUTORADO"))
                .andExpect(jsonPath("$.data.usuario.pwd").doesNotExist())
                .andExpect(jsonPath("$.data.usuario.passwordHash").doesNotExist())
                .andReturn().getResponse().getContentAsString();

        String hash = jdbc.queryForObject("SELECT password_hash FROM usuario WHERE matricula = 'S24011236'", String.class);
        assertThat(hash).startsWith("$2").doesNotContain("secreta12");
        assertThat(respuesta).doesNotContain("secreta12").doesNotContain(hash);

        String token = JsonPath.read(respuesta, "$.data.token");
        mvc.perform(getCon(token, "/usuarios/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.matricula").value("S24011236"))
                .andExpect(jsonPath("$.data.proveedor").value("LOCAL"));
    }

    @Test
    void signupValidaCadaCampoConSuNombreDelBody() throws Exception {
        postJson("/auth/signup", "{}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error de validación"))
                .andExpect(jsonPath("$.data.nombre").isString())
                .andExpect(jsonPath("$.data.apellidos").isString())
                .andExpect(jsonPath("$.data.matricula").isString())
                .andExpect(jsonPath("$.data.correo").isString())
                .andExpect(jsonPath("$.data.pwd").isString())
                .andExpect(jsonPath("$.data.rol").isString());

        postJson("/auth/signup", signupJson("24011236", "no-es-correo", "corta", "ADMIN"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.matricula").value("Matrícula inválida: debe ser S seguida de 8 dígitos (p. ej. S24011236)."))
                .andExpect(jsonPath("$.data.correo").value("El correo no es válido."))
                .andExpect(jsonPath("$.data.pwd").value("La contraseña debe tener entre 8 y 72 caracteres."))
                .andExpect(jsonPath("$.data.rol").value("El rol debe ser TUTORADO o TUTOR."));

        String nombreLargo = "x".repeat(51);
        postJson("/auth/signup", signupJson("S24011236", "a@b.mx", "secreta12", "TUTORADO").replace("Sofía", nombreLargo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.nombre").value("El nombre no puede exceder 50 caracteres."));

        // 30 caracteres de 3 bytes: cabe en @Size, pero pasa de los 72 bytes de BCrypt.
        postJson("/auth/signup", signupJson("S24011236", "a@b.mx", "€".repeat(30), "TUTORADO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.pwd").value("La contraseña es demasiado larga."));

        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM usuario", Integer.class)).isZero();
    }

    @Test
    void signupSoloAceptaLosDominiosConfiguradosYElAliasPassword() throws Exception {
        postJson("/auth/signup", signupJson("S24011236", "sofia@hotmail.com", "secreta12", "TUTORADO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.correo").value("El correo debe ser @estudiantes.uv.mx o @gmail.com."));

        // "password" en lugar de "pwd" (nombre de la versión del 12 de octubre).
        postJson("/auth/signup", signupJson("S24011236", "zs24011236@estudiantes.uv.mx", "secreta12", "TUTORADO")
                        .replace("\"pwd\"", "\"password\""))
                .andExpect(status().isOk());
        postJson("/auth/signin", "{\"matricula\":\"S24011236\",\"password\":\"secreta12\"}")
                .andExpect(status().isOk());
    }

    @Test
    void signupDuplicadosSalenPorCampo() throws Exception {
        usuarioConPwd("S24011236", "zs24011236@estudiantes.uv.mx", "TUTORADO", "secreta12");

        postJson("/auth/signup", signupJson("S24011236", "otro@estudiantes.uv.mx", "secreta12", "TUTORADO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error de validación"))
                .andExpect(jsonPath("$.data.matricula").value("La matrícula ya está registrada."));

        postJson("/auth/signup", signupJson("S24099999", "zs24011236@estudiantes.uv.mx", "secreta12", "TUTORADO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.correo").value("El correo ya está registrado."));
    }

    @Test
    void tutorSoloSeDaDeAltaSiElAdminLoPreRegistro() throws Exception {
        String admin = tokenAdmin();
        String json = signupJson("S21009342", "zs21009342@estudiantes.uv.mx", "secreta12", "TUTOR");

        postJson("/auth/signup", json)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.rol").value(
                        "Tu matrícula no está autorizada como tutor. Pide al administrador que te registre."));

        mvc.perform(postCon(admin, "{\"nombre\":\"Rocío\",\"apellidos\":\"Hernández Aguilar\","
                        + "\"matricula\":\"zS21009342\",\"correo\":\"zS21009342@estudiantes.uv.mx\"}", "/admin/tutores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tutor autorizado."))
                .andExpect(jsonPath("$.data.matricula").value("S21009342"))
                .andExpect(jsonPath("$.data.rol").value("TUTOR"));

        // Un pre-registro no puede entrar todavía.
        signin("matricula", "S21009342", "secreta12")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(CREDENCIALES));

        postJson("/auth/signup", json.replace("zs21009342@", "otra@"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.correo").value("El correo no coincide con el registrado para esta matrícula."));

        postJson("/auth/signup", json.replace("\"TUTOR\"", "\"TUTORADO\""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.rol").value("Tu matrícula está autorizada como tutor: elige el rol TUTOR."));

        postJson("/auth/signup", json)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.usuario.rol").value("TUTOR"))
                .andExpect(jsonPath("$.data.usuario.nombre").value("Sofía"));

        // Ya reclamado: nadie más puede tomarlo ni el admin borrarlo.
        postJson("/auth/signup", json)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.matricula").value("La matrícula ya está registrada."));
        mvc.perform(deleteCon(admin, "/admin/tutores/S21009342"))
                .andExpect(status().isBadRequest());

        signin("matricula", "S21009342", "secreta12").andExpect(status().isOk());
    }

    @Test
    void adminPreRegistraYQuitaTutores() throws Exception {
        String admin = tokenAdmin();
        usuarioConPwd("S23014587", "zs23014587@estudiantes.uv.mx", "TUTORADO", "secreta12");

        mvc.perform(postCon(admin, "{}", "/admin/tutores"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.matricula").isString())
                .andExpect(jsonPath("$.data.correo").isString());

        mvc.perform(postCon(admin, "{\"nombre\":\"A\",\"apellidos\":\"B\",\"matricula\":\"S23014587\","
                        + "\"correo\":\"nuevo@estudiantes.uv.mx\"}", "/admin/tutores"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.matricula").value("La matrícula ya está registrada."));

        mvc.perform(postCon(admin, "{\"nombre\":\"A\",\"apellidos\":\"B\",\"matricula\":\"S21004411\","
                        + "\"correo\":\"zs23014587@estudiantes.uv.mx\"}", "/admin/tutores"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.correo").value("El correo ya está registrado."));

        mvc.perform(postCon(admin, "{\"nombre\":\"Jorge\",\"apellidos\":\"Castillo Rivera\",\"matricula\":\"S21004411\","
                        + "\"correo\":\"zs21004411@estudiantes.uv.mx\"}", "/admin/tutores"))
                .andExpect(status().isOk());
        mvc.perform(getCon(admin, "/admin/tutores"))
                .andExpect(jsonPath("$.data[0].matricula").value("S21004411"));

        mvc.perform(deleteCon(admin, "/admin/tutores/S21004411"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Pre-registro eliminado."));
        mvc.perform(deleteCon(admin, "/admin/tutores/S23014587"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Solo se puede quitar un tutor pre-registrado que aún no crea su cuenta."));
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM usuario WHERE matricula = 'S21004411'", Integer.class)).isZero();
    }

    @Test
    void cuentaDelLoginConMicrosoftSeReclamaConSuCorreo() throws Exception {
        jdbc.update("INSERT INTO usuario (matricula, nombre, apellidos, correo, id_rol, proveedor, id_externo) " +
                "VALUES ('S23014587', 'Valeria', 'Mendoza Cruz', 'zs23014587@estudiantes.uv.mx', " +
                "(SELECT id_rol FROM rol WHERE rol = 'TUTORADO'), 'MICROSOFT', 'sub-valeria')");

        postJson("/auth/signup", signupJson("S23014587", "zs23014587@estudiantes.uv.mx", "secreta12", "TUTORADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.usuario.proveedor").value("LOCAL"));

        assertThat(jdbc.queryForObject("SELECT id_externo FROM usuario WHERE matricula = 'S23014587'", String.class)).isNull();
    }

    // ------------------------------------------------------------------
    // Signin
    // ------------------------------------------------------------------

    @Test
    void signinPorMatriculaOCorreoDevuelveJwtDeSesion() throws Exception {
        usuarioConPwd("S23014587", "zs23014587@estudiantes.uv.mx", "TUTORADO", "secreta123");

        String respuesta = signin("matricula", "S23014587", "secreta123")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login"))
                .andExpect(jsonPath("$.data.usuario.matricula").value("S23014587"))
                .andExpect(jsonPath("$.data.usuario.rol").value("TUTORADO"))
                .andReturn().getResponse().getContentAsString();

        Claims claims = jwtUtils.leer(JsonPath.read(respuesta, "$.data.token"));
        assertThat(claims.getSubject()).isEqualTo("S23014587");
        assertThat(claims.get("rol", String.class)).isEqualTo("TUTORADO");
        assertThat(claims.get("tipo", String.class)).isEqualTo("SESION");
        assertThat(claims.getExpiration()).isNotNull();

        signin("correo", "ZS23014587@estudiantes.uv.mx", "secreta123").andExpect(status().isOk());
        signin("matricula", "zs23014587", "secreta123").andExpect(status().isOk());
    }

    @Test
    void signinDaElMismoErrorSiNoExisteOLaContrasenaEsIncorrecta() throws Exception {
        usuarioConPwd("S23014587", "zs23014587@estudiantes.uv.mx", "TUTORADO", "secreta123");
        usuario("S23016620", "Andrea", "Morales Pineda", "TUTORADO"); // pre-registro sin contraseña

        for (ResultActions intento : new ResultActions[]{
                signin("matricula", "S23014587", "incorrecta"),
                signin("correo", "nadie@estudiantes.uv.mx", "secreta123"),
                signin("matricula", "S99999999", "secreta123"),
                signin("matricula", "S23016620", "secreta123")}) {
            intento.andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(CREDENCIALES))
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        postJson("/auth/signin", "{\"pwd\":\"secreta123\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.matricula").value("Escribe tu matrícula o correo."));
        postJson("/auth/signin", "{\"matricula\":\"S23014587\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.pwd").value("La contraseña es obligatoria."));
    }

    @Test
    void signinIgnoraUnTokenViejoEnElHeader() throws Exception {
        usuarioConPwd("S23014587", "zs23014587@estudiantes.uv.mx", "TUTORADO", "secreta123");

        mvc.perform(post("/auth/signin").header("Authorization", "Bearer token-vencido")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"matricula\":\"S23014587\",\"pwd\":\"secreta123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void cincoFallosBloqueanLaCuentaPorQuinceMinutos() throws Exception {
        usuarioConPwd("S23014587", "zs23014587@estudiantes.uv.mx", "TUTORADO", "secreta123");

        // Matrícula y correo cuentan juntos: son la misma cuenta.
        for (int i = 0; i < 3; i++) signin("matricula", "S23014587", "mal").andExpect(status().isUnauthorized());
        for (int i = 0; i < 2; i++) signin("correo", "zs23014587@estudiantes.uv.mx", "mal").andExpect(status().isUnauthorized());

        // Bloqueada incluso con la contraseña correcta.
        signin("matricula", "S23014587", "secreta123")
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Demasiados intentos fallidos. Intenta de nuevo en 15 minutos."));

        reloj.avanzarMinutos(14);
        signin("matricula", "S23014587", "secreta123")
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Demasiados intentos fallidos. Intenta de nuevo en 1 minuto."));

        reloj.avanzarMinutos(1);
        signin("matricula", "S23014587", "secreta123").andExpect(status().isOk());

        // El éxito limpia el contador: vuelve a tener 5 intentos.
        for (int i = 0; i < 4; i++) signin("matricula", "S23014587", "mal").andExpect(status().isUnauthorized());
        signin("matricula", "S23014587", "secreta123").andExpect(status().isOk());
    }

    @Test
    void adminEntraConHashGeneradoPorPgcrypto() throws Exception {
        jdbc.execute("CREATE EXTENSION IF NOT EXISTS pgcrypto");
        usuario("25480", "Adriana", "Salas Ortega", "ADMIN");
        // El mismo UPDATE que documenta 004_contrasenas.sql.
        jdbc.update("UPDATE usuario SET proveedor = 'LOCAL', id_externo = NULL, " +
                "password_hash = crypt('admin-secreta', gen_salt('bf', 10)) WHERE matricula = '25480'");

        signin("matricula", "25480", "admin-secreta")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.usuario.rol").value("ADMIN"));
    }

    // ------------------------------------------------------------------
    // Sesión
    // ------------------------------------------------------------------

    @Test
    void sinTokenOConTokenInvalidoEs401() throws Exception {
        mvc.perform(get("/usuarios/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Debes iniciar sesión."));

        mvc.perform(getCon("no-es-un-jwt", "/usuarios/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Token inválido o expirado."));
    }

    @Test
    void elLoginConProveedoresYaNoExiste() throws Exception {
        for (String ruta : new String[]{"/auth/microsoft", "/auth/google", "/auth/registro"}) {
            postJson(ruta, "{\"idToken\":\"x\"}").andExpect(status().isNotFound());
        }
    }

    @Test
    void loginDeDesarrolloEntraPorCorreo() throws Exception {
        usuario("S21009342", "Rocío", "Hernández Aguilar", "TUTOR");

        postJson("/auth/dev", "{\"correo\":\"S21009342@prueba.mx\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.usuario.rol").value("TUTOR"))
                .andExpect(jsonPath("$.data.token").isString());
    }
}
