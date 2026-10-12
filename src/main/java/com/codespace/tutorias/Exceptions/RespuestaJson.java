package com.codespace.tutorias.Exceptions;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Escribe un ApiResponse de error directamente en la respuesta, para los
 * puntos donde no interviene el GlobalExceptionHandler (filtros de seguridad).
 */
public final class RespuestaJson {

    private RespuestaJson() {}

    public static void escribir(HttpServletResponse response, int status, String mensaje) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"success\":false,\"message\":\"" + escapar(mensaje) + "\",\"data\":null}");
    }

    private static String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
