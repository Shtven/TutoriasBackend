package com.codespace.tutorias.Repositories;

import com.codespace.tutorias.Models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    Optional<Usuario> findByProveedorAndIdExterno(String proveedor, String idExterno);

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    List<Usuario> findByRol_RolOrderByNombreAscApellidosAsc(String rol);
}
