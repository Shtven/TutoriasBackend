package com.codespace.tutorias;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Ejecuta db/supabase/001_esquema.sql completo (como el SQL Editor de
 * Supabase) una vez por ejecución de pruebas, antes de levantar el contexto.
 */
final class EsquemaPrueba {

    static final String URL = env("TEST_DB_URL", "jdbc:postgresql://localhost:5433/tutorias_test");
    static final String USUARIO = env("TEST_DB_USERNAME", "postgres");
    static final String PASSWORD = env("TEST_DB_PASSWORD", "");

    private static boolean creado = false;

    private EsquemaPrueba() {}

    static synchronized void crearUnaVez() {
        if (creado) return;
        try (Connection con = DriverManager.getConnection(URL, USUARIO, PASSWORD);
             Statement st = con.createStatement()) {
            st.execute(Files.readString(Path.of("db/supabase/001_esquema.sql")));
            creado = true;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo crear el esquema de pruebas en " + URL, e);
        }
    }

    private static String env(String nombre, String porDefecto) {
        String valor = System.getenv(nombre);
        return valor == null || valor.isBlank() ? porDefecto : valor;
    }
}
