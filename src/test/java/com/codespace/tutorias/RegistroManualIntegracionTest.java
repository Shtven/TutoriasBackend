package com.codespace.tutorias;

import com.codespace.tutorias.JWT.IdentidadVerificada;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Registro y login con contraseña, junto al login con proveedor (docs/AUTENTICACION.md § 9). */
class RegistroManualIntegracionTest extends PruebaIntegracion {

    private static final String SOFIA = """
            {"matricula":"zs24011236","nombre":"Sofía","apellidos":"Ramos Gutiérrez",
             "correo":"ZS24011236@Estudiantes.UV.mx","password":"secreto123","rol":"tutorado"}""";

    @Autowired
    private PasswordEncoder passwordEncoder;

    private ResultActions postJson(String url, String json) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private void rechaza(String url, String json, String mensaje) throws Exception {
        postJson(url, json)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value(mensaje));
    }

    private String hashDe(String matricula) {
        return jdbc.queryForObject("SELECT password_hash FROM usuario WHERE matricula = ?", String.class, matricula);
    }

    @Test
    void signupCreaLaCuentaYEntraDirecto() throws Exception {
        String respuesta = postJson("/auth/signup", SOFIA)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Registro exitoso."))
                .andExpect(jsonPath("$.data.registroPendiente").value(false))
                .andExpect(jsonPath("$.data.rol").value("TUTORADO"))
                .andExpect(jsonPath("$.data.usuario.matricula").value("S24011236"))
                .andExpect(jsonPath("$.data.usuario.nombre").value("Sofía"))
                .andExpect(jsonPath("$.data.usuario.apellidos").value("Ramos Gutiérrez"))
                .andExpect(jsonPath("$.data.usuario.correo").value("zs24011236@estudiantes.uv.mx"))
                .andExpect(jsonPath("$.data.usuario.proveedor").value(nullValue()))
                .andReturn().getResponse().getContentAsString();

        String token = JsonPath.read(respuesta, "$.data.token");
        mvc.perform(getCon(token, "/usuarios/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.matricula").value("S24011236"));

        // Se guarda el hash BCrypt, nunca la contraseña, y no sale en la respuesta.
        String hash = hashDe("S24011236");
        assertThat(hash).startsWith("$2").doesNotContain("secreto123");
        assertThat(passwordEncoder.matches("secreto123", hash)).isTrue();
        assertThat(respuesta).doesNotContain("password").doesNotContain(hash);
    }

    @Test
    void signupYSigninAceptanLosCamposDeLaVersionAnterior() throws Exception {
        postJson("/auth/signup", """
                {"matricula":"S21009342","nombre":"Rocío","apellidoP":"Hernández","apellidoM":"Aguilar",
                 "correo":"zs21009342@estudiantes.uv.mx","pwd":"clave-segura","rol":"TUTOR"}""")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.usuario.apellidos").value("Hernández Aguilar"))
                .andExpect(jsonPath("$.data.rol").value("TUTOR"));

        postJson("/auth/signin", "{\"matricula\":\"zS21009342\",\"pwd\":\"clave-segura\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login"))
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.rol").value("TUTOR"))
                .andExpect(jsonPath("$.data.usuario.matricula").value("S21009342"));
    }

    @Test
    void signupValidaRolDominioContrasenaApellidosYDuplicados() throws Exception {
        rechaza("/auth/signup", SOFIA.replace("tutorado", "ADMIN"), "Rol inválido.");
        rechaza("/auth/signup", SOFIA.replace("Estudiantes.UV.mx", "hotmail.com"),
                "El correo debe ser @estudiantes.uv.mx o @gmail.com.");
        rechaza("/auth/signup", SOFIA.replace("\"apellidos\":\"Ramos Gutiérrez\",", ""),
                "Los apellidos son obligatorios.");
        rechaza("/auth/signup", SOFIA.replace("zs24011236\"", "24011236\""),
                "Matrícula inválida: debe ser S seguida de 8 dígitos (p. ej. S24011236).");

        postJson("/auth/signup", SOFIA.replace("secreto123", "corta"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error de validación"))
                .andExpect(jsonPath("$.data.password").value("La contraseña debe tener entre 8 y 72 caracteres."));

        // 40 letras, pero 80 bytes: más que los 72 que admite BCrypt.
        rechaza("/auth/signup", SOFIA.replace("secreto123", "ñ".repeat(40)),
                "La contraseña es demasiado larga: usa menos letras con acento o símbolos.");

        postJson("/auth/signup", SOFIA).andExpect(status().isOk());
        rechaza("/auth/signup", SOFIA.replace("ZS24011236@", "otra@"),
                "La matrícula ya está registrada con otra cuenta.");
        rechaza("/auth/signup", SOFIA.replace("zs24011236\"", "zs24099999\""), "El correo ya está registrado.");
    }

    @Test
    void signinPorCorreoYMensajesDeError() throws Exception {
        postJson("/auth/signup", SOFIA).andExpect(status().isOk());

        postJson("/auth/signin", "{\"correo\":\" ZS24011236@estudiantes.uv.mx \",\"password\":\"secreto123\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.usuario.matricula").value("S24011236"));

        String incorrectas = "Matrícula, correo o contraseña incorrectos.";
        rechaza("/auth/signin", "{\"matricula\":\"S24011236\",\"password\":\"otra-clave\"}", incorrectas);
        rechaza("/auth/signin", "{\"matricula\":\"S29999999\",\"password\":\"secreto123\"}", incorrectas);
        // Más de 72 bytes: se rechaza sin llegar a BCrypt (que lanzaría un error 500).
        rechaza("/auth/signin", "{\"matricula\":\"S24011236\",\"password\":\"" + "ñ".repeat(40) + "\"}", incorrectas);
        rechaza("/auth/signin", "{\"password\":\"secreto123\"}", "Escribe tu matrícula o tu correo.");

        usuario("S23014587", "Valeria", "Mendoza Cruz", "TUTORADO");
        rechaza("/auth/signin", "{\"matricula\":\"S23014587\",\"password\":\"secreto123\"}",
                "Esta cuenta no tiene contraseña: inicia sesión con Microsoft o Google.");
    }

    @Test
    void adminConContrasenaPuestaEnBdEntraConSuMatricula() throws Exception {
        jdbc.update("INSERT INTO usuario (matricula, nombre, apellidos, correo, id_rol, password_hash) " +
                        "VALUES ('25480', 'Adriana', 'Salas Ortega', 'asalas@uv.mx', " +
                        "(SELECT id_rol FROM rol WHERE rol = 'ADMIN'), ?)",
                passwordEncoder.encode("admin-segura-1"));

        postJson("/auth/signin", "{\"matricula\":\"25480\",\"password\":\"admin-segura-1\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rol").value("ADMIN"));

        // La BD solo acepta hashes BCrypt, nunca texto plano.
        assertThatThrownBy(() -> jdbc.update("UPDATE usuario SET password_hash = 'admin-segura-1' WHERE matricula = '25480'"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void loginConProveedorLigaLaCuentaManualYDesactivaLaContrasena() throws Exception {
        postJson("/auth/signup", SOFIA).andExpect(status().isOk());
        when(verificadorIdToken.verificarMicrosoft("id-token-sofia")).thenReturn(new IdentidadVerificada(
                "MICROSOFT", "sub-sofia", "zs24011236@estudiantes.uv.mx", "Sofía", "Ramos Gutiérrez"));

        postJson("/auth/microsoft", "{\"idToken\":\"id-token-sofia\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.registroPendiente").value(false))
                .andExpect(jsonPath("$.data.usuario.matricula").value("S24011236"))
                .andExpect(jsonPath("$.data.usuario.proveedor").value("MICROSOFT"));

        assertThat(hashDe("S24011236")).isNull();
        rechaza("/auth/signin", "{\"matricula\":\"S24011236\",\"password\":\"secreto123\"}",
                "Esta cuenta inicia sesión con Microsoft.");
    }
}
