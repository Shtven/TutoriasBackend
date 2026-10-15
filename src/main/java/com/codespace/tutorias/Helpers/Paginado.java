package com.codespace.tutorias.Helpers;

import com.codespace.tutorias.Exceptions.ApiResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Resultado de un listado paginable: la porción a devolver y el total de
 * elementos que cumplen el filtro (antes de recortar).
 */
public record Paginado<T>(List<T> datos, long total) {

    public static final String TOTAL = "X-Total-Count";
    public static final String PAGINA = "X-Page";
    public static final String TAMANO = "X-Page-Size";

    /** data = la lista, como siempre; la paginación viaja en cabeceras. */
    public ResponseEntity<ApiResponse<List<T>>> respuesta(String mensaje, Pagina pagina) {
        ResponseEntity.BodyBuilder ok = ResponseEntity.ok().header(TOTAL, String.valueOf(total));
        if (pagina != null) {
            ok.header(PAGINA, String.valueOf(pagina.page()))
              .header(TAMANO, String.valueOf(pagina.size()));
        }
        return ok.body(new ApiResponse<>(true, mensaje, datos));
    }
}
