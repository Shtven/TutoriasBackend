package com.codespace.tutorias.Repositories;

import com.codespace.tutorias.Models.Sesion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SesionRepository extends JpaRepository<Sesion, Integer> {

    Optional<Sesion> findByHorario_IdHorarioAndFecha(int idHorario, LocalDate fecha);

    List<Sesion> findByHorario_IdHorarioInAndFechaBetween(Collection<Integer> idsHorario, LocalDate desde, LocalDate hasta);

    List<Sesion> findByHorario_Tutoria_IdTutoriaOrderByFechaAscHoraInicioAsc(int idTutoria);

    @Query("SELECT COUNT(s) > 0 FROM Sesion s WHERE s.horario.tutoria.idTutoria = :idTutoria " +
            "AND s.fechaListaAbierta IS NOT NULL AND s.fechaListaCerrada IS NULL")
    boolean existeListaAbierta(int idTutoria);

    /** Pases de lista abiertos de un tutor (cualquier fecha). */
    @Query("SELECT s FROM Sesion s WHERE s.horario.tutoria.tutor.matricula = :matricula " +
            "AND s.fechaListaAbierta IS NOT NULL AND s.fechaListaCerrada IS NULL " +
            "ORDER BY s.fecha, s.horaInicio")
    List<Sesion> findListasAbiertasDeTutor(String matricula);

    /** Listas abiertas a las que aún no se les envió el aviso de cierre (RN-9). */
    @Query("SELECT s FROM Sesion s WHERE s.fechaListaAbierta IS NOT NULL " +
            "AND s.fechaListaCerrada IS NULL AND s.fechaAvisoCierre IS NULL")
    List<Sesion> findListasAbiertasSinAviso();
}
