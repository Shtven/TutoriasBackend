package com.codespace.tutorias.Repositories;

import com.codespace.tutorias.Models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    boolean existsByCorreo(String correo);
}
