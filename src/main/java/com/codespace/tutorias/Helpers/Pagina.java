package com.codespace.tutorias.Helpers;

import com.codespace.tutorias.Exceptions.BusinessException;

import java.util.List;

/**
 * Paginación opcional por query (?page=0&size=20). Sin ninguno de los dos
 * parámetros la respuesta trae la lista completa, como antes, para no
 * romper a los clientes que no paginan. El total va en X-Total-Count.
 */
public record Pagina(int page, int size) {

    public static final int TAMANO_POR_DEFECTO = 20;
    public static final int TAMANO_MAXIMO = 100;

    /** null = sin paginar. */
    public static Pagina de(Integer page, Integer size) {
        if (page == null && size == null) {
            return null;
        }
        int p = page == null ? 0 : page;
        int s = size == null ? TAMANO_POR_DEFECTO : size;
        if (p < 0) {
            throw new BusinessException("page debe ser 0 o mayor.");
        }
        if (s < 1 || s > TAMANO_MAXIMO) {
            throw new BusinessException("size debe estar entre 1 y " + TAMANO_MAXIMO + ".");
        }
        return new Pagina(p, s);
    }

    /** La porción de esta página; vacía si la página queda fuera del total. */
    public <T> List<T> recortar(List<T> lista) {
        long desde = (long) page * size;
        if (desde >= lista.size()) {
            return List.of();
        }
        return List.copyOf(lista.subList((int) desde, (int) Math.min(lista.size(), desde + size)));
    }
}
