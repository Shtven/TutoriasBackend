package com.codespace.tutorias.Repositories;

import com.codespace.tutorias.Models.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Integer> {

    @Query("SELECT a FROM Asistencia a JOIN FETCH a.usuario WHERE a.tutoria.idTutoria = :idTutoria")
    List<Asistencia> findByTutoriaIdTutoria(@Param("idTutoria") int idTutoria);

    @Query("SELECT a FROM Asistencia a JOIN FETCH a.usuario WHERE a.usuario.matricula = :matricula AND a.tutoria.idTutoria = :idTutoria")
    Optional<Asistencia> findByMatriculaTutoria(@Param("matricula") String matricula, @Param("idTutoria") int idTutoria);

    long countByTutoriaIdTutoria(int idTutoria);

    @Query("SELECT a FROM Asistencia a JOIN FETCH a.usuario WHERE a.usuario.matricula = :matricula")
    List<Asistencia> findByUsuarioMatricula(@Param("matricula") String matricula);

    @Query("SELECT a FROM Asistencia a " +
            "JOIN FETCH a.tutoria t " +
            "JOIN FETCH t.horario h " +
            "JOIN FETCH h.tutor " +
            "JOIN FETCH t.materia " +
            "WHERE a.usuario.matricula = :matricula AND t.estado = 'COMPLETADA'")
    List<Asistencia> findHistorialByMatricula(@Param("matricula") String matricula);

    @Query("SELECT AVG(a.calificacion) FROM Asistencia a " +
            "WHERE a.tutoria.horario.tutor.matricula = :matricula AND a.calificacion IS NOT NULL")
    Double calcularPromedioTutor(@Param("matricula") String matricula);

    @Query("SELECT COUNT(a) FROM Asistencia a " +
            "WHERE a.tutoria.horario.tutor.matricula = :matricula AND a.calificacion IS NOT NULL")
    long contarCalificacionesTutor(@Param("matricula") String matricula);

}
