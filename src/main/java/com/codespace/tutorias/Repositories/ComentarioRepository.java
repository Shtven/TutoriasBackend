package com.codespace.tutorias.Repositories;

import com.codespace.tutorias.Models.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Integer> {

    @Query("SELECT c FROM Comentario c JOIN FETCH c.usuario WHERE c.tutoria.idTutoria = :idTutoria ORDER BY c.idComentario DESC")
    List<Comentario> findByTutoriaIdTutoria(@Param("idTutoria") int idTutoria);

    @Query("SELECT c FROM Comentario c JOIN FETCH c.tutoria WHERE c.usuario.matricula = :matricula ORDER BY c.idComentario DESC")
    List<Comentario> findByUsuarioMatricula(@Param("matricula") Integer matricula);
}
