package com.codespace.tutorias.Repositories;

import com.codespace.tutorias.Models.Tema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TemaRepository extends JpaRepository<Tema, Integer> {

    @Query("SELECT t FROM Tema t WHERE t.tutoria.idTutoria = :idTutoria ORDER BY t.idTema ASC")
    List<Tema> findByTutoriaIdTutoria(@Param("idTutoria") int idTutoria);
}
