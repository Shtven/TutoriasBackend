package com.codespace.tutorias.Repositories;

import com.codespace.tutorias.Models.Comentario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Integer> {

    List<Comentario> findByTutoria_IdTutoriaOrderByFechaComentarioDescIdComentarioDesc(int idTutoria);
}
