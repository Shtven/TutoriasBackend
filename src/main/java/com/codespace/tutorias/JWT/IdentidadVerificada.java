package com.codespace.tutorias.JWT;

/**
 * Identidad confirmada por un proveedor externo (firma, iss, aud, exp y
 * dominio ya validados). Es lo único en lo que confía el registro.
 */
public record IdentidadVerificada(
        String proveedor,
        String idExterno,
        String correo,
        String nombre,
        String apellidos
) {}
