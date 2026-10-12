package com.codespace.tutorias.Services;

import com.codespace.tutorias.DTO.Mapping.UsuarioMapping;
import com.codespace.tutorias.DTO.Responsive.UsuarioResponsive;
import com.codespace.tutorias.Exceptions.BusinessException;
import com.codespace.tutorias.Models.Usuario;
import com.codespace.tutorias.Repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioMapping usuarioMapping;

    @Transactional(readOnly = true)
    public UsuarioResponsive obtenerActual(String matricula) {
        return usuarioMapping.toDTO(buscar(matricula));
    }

    /** Usuario autenticado; si el JWT apunta a alguien que ya no existe, se rechaza. */
    public Usuario buscar(String matricula) {
        return usuarioRepository.findById(matricula)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado."));
    }
}
