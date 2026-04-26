package com.codespace.tutorias.Repositories;

import com.codespace.tutorias.Models.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface HorarioRepository extends JpaRepository<Horario, Integer> {

    @Query("SELECT h FROM Horario h JOIN h.tutor t WHERE t.matricula=:matricula")
    List<Horario> findByMatricula(String matricula);
}
