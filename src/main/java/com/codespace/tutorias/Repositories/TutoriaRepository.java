package com.codespace.tutorias.Repositories;

import com.codespace.tutorias.Models.Tutoria;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TutoriaRepository extends JpaRepository<Tutoria, Integer> {

    /** Bloquea la fila para contar el cupo sin carreras (RN-3). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Tutoria t WHERE t.idTutoria = :idTutoria")
    Optional<Tutoria> findByIdParaActualizar(int idTutoria);

    List<Tutoria> findByEstado(String estado);

    List<Tutoria> findByTutor_Matricula(String matricula);

    List<Tutoria> findByTutor_MatriculaAndEstado(String matricula, String estado);

    long countByEstado(String estado);

    boolean existsByMateria_IdMateria(int idMateria);

    /** [idMateria, tutorías activas] */
    @Query("SELECT t.materia.idMateria, COUNT(t) FROM Tutoria t WHERE t.estado = 'ACTIVA' GROUP BY t.materia.idMateria")
    List<Object[]> contarActivasPorMateria();
}
