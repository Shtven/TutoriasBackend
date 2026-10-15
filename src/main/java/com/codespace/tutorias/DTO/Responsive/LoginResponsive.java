package com.codespace.tutorias.DTO.Responsive;

/** Respuesta de /auth/signin y /auth/signup: JWT de sesión + usuario. */
public record LoginResponsive(
        String token,
        UsuarioResponsive usuario
) {}
