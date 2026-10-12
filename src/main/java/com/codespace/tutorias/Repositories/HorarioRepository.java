package com.codespace.tutorias.Repositories;

import com.codespace.tutorias.Models.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HorarioRepository extends JpaRepository<Horario, Integer> {

    /** Horarios vigentes (ACTIVO en tutoría ACTIVA) de un tutor. */
    @Query("SELECT h FROM Horario h WHERE h.tutoria.tutor.matricula = :matricula " +
            "AND h.estado = 'ACTIVO' AND h.tutoria.estado = 'ACTIVA'")
    List<Horario> findVigentesDeTutor(String matricula);

    /** Horarios vigentes que ocupan un aula. */
    @Query("SELECT h FROM Horario h WHERE h.tutoria.edificio = :edificio AND h.tutoria.aula = :aula " +
            "AND h.estado = 'ACTIVO' AND h.tutoria.estado = 'ACTIVA'")
    List<Horario> findVigentesEnAula(int edificio, int aula);

    /** Horarios vigentes de las tutorías donde el tutorado tiene inscripción ACTIVA. */
    @Query("SELECT h FROM Horario h, Inscripcion i WHERE i.tutoria = h.tutoria " +
            "AND i.tutorado.matricula = :matricula AND i.estado = 'ACTIVA' " +
            "AND h.estado = 'ACTIVO' AND h.tutoria.estado = 'ACTIVA'")
    List<Horario> findVigentesDeTutorado(String matricula);

    /** Horarios vigentes de un día de la semana (sesiones de hoy, recordatorios). */
    @Query("SELECT h FROM Horario h WHERE h.dia = :dia AND h.estado = 'ACTIVO' AND h.tutoria.estado = 'ACTIVA'")
    List<Horario> findVigentesPorDia(String dia);
}
