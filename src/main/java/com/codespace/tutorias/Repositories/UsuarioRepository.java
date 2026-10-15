package com.codespace.tutorias.Repositories;

import com.codespace.tutorias.Models.Usuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    List<Usuario> findByRol_RolOrderByNombreAscApellidosAsc(String rol);

    /** Con bloqueo de fila: dos signups simultáneos no reclaman la misma cuenta. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM Usuario u WHERE u.matricula = :matricula")
    Optional<Usuario> bloquear(String matricula);
}
