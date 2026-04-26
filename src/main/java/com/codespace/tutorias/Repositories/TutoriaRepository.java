package com.codespace.tutorias.Repositories;

import com.codespace.tutorias.Models.Tutoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TutoriaRepository extends JpaRepository<Tutoria, Integer> {

    @Query("SELECT tu FROM Tutoria tu JOIN tu.horario h JOIN h.tutor t WHERE t.matricula=:matricula")
    List<Tutoria> findAllByMatricula(String matricula);
}
