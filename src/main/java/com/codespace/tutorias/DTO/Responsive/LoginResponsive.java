package com.codespace.tutorias.DTO.Responsive;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Respuesta del login. Si registroPendiente = false trae token + usuario;
 * si es true trae tokenRegistro + perfil (primer acceso).
 *
 * rol repite usuario.rol: es lo que devolvía el login anterior
 * ({token, rol}) y se conserva para los clientes que lo leen así.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponsive(
        boolean registroPendiente,
        String token,
        String rol,
        UsuarioResponsive usuario,
        String tokenRegistro,
        PerfilProveedorResponsive perfil
) {
    public static LoginResponsive sesion(String token, UsuarioResponsive usuario) {
        return new LoginResponsive(false, token, usuario.rol(), usuario, null, null);
    }

    public static LoginResponsive registroPendiente(String tokenRegistro, PerfilProveedorResponsive perfil) {
        return new LoginResponsive(true, null, null, null, tokenRegistro, perfil);
    }
}
