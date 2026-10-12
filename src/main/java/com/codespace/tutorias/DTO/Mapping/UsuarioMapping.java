package com.codespace.tutorias.DTO.Mapping;

import com.codespace.tutorias.DTO.Responsive.UsuarioResponsive;
import com.codespace.tutorias.DTO.Responsive.UsuarioResumenResponsive;
import com.codespace.tutorias.Models.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapping {

    public UsuarioResponsive toDTO(Usuario usuario) {
        return new UsuarioResponsive(
                usuario.getMatricula(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getCorreo(),
                usuario.getRol().getRol(),
                usuario.getProveedor());
    }

    public UsuarioResumenResponsive toResumen(Usuario usuario) {
        return new UsuarioResumenResponsive(
                usuario.getMatricula(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.nombreCompleto());
    }
}
