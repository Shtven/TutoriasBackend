package com.codespace.tutorias.Repositories;
import com.codespace.tutorias.Models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsuarioRepository extends JpaRepository<Usuario,String> {


        @Query("SELECT u FROM Usuario u WHERE u.correo = :correo")
        Boolean existsByCorreo(@Param("correo") String correo);
}
