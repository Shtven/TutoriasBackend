package com.codespace.tutorias.Repositories;

import com.codespace.tutorias.Models.Tutoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TutoriaRepository extends JpaRepository<Tutoria, Integer> {

    @Query("SELECT tu FROM Tutoria tu JOIN tu.horario h JOIN h.tutor t WHERE t.matricula=:matricula AND tu.estado='PROGRAMADA'")
    List<Tutoria> findAllByMatricula(String matricula);

    @Query("SELECT tu FROM Tutoria tu WHERE tu.estado='PROGRAMADA' AND tu.fecha >= CURRENT_DATE")
    List<Tutoria> findAllProgramadas();

    boolean existsByHorario_IdHorario(int idHorario);
}
