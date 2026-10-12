package com.codespace.tutorias.Helpers;

import java.text.Normalizer;

public final class Texto {

    private Texto() {}

    /** Para búsquedas: minúsculas y sin acentos. */
    public static String normalizar(String texto) {
        if (texto == null) return "";
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .trim();
    }

    public static boolean contiene(String texto, String buscado) {
        return normalizar(texto).contains(normalizar(buscado));
    }
}
