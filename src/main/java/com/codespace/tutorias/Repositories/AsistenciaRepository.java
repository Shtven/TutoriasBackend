package com.codespace.tutorias.Repositories;

import com.codespace.tutorias.Models.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Integer> {

    List<Asistencia> findBySesion_IdSesion(int idSesion);

    List<Asistencia> findBySesion_IdSesionIn(Collection<Integer> idsSesion);

    List<Asistencia> findByInscripcion_IdInscripcion(int idInscripcion);

    List<Asistencia> findBySesion_Horario_Tutoria_IdTutoria(int idTutoria);

    /** Inasistencias que cuentan (RN-10): asistio=false en sesiones COMPLETADA. */
    @Query("SELECT COUNT(a) FROM Asistencia a WHERE a.inscripcion.idInscripcion = :idInscripcion " +
            "AND a.asistio = false AND a.sesion.fechaListaCerrada IS NOT NULL")
    long contarInasistencias(int idInscripcion);

    /** [idInscripcion, inasistencias] para varias inscripciones a la vez. */
    @Query("SELECT a.inscripcion.idInscripcion, COUNT(a) FROM Asistencia a " +
            "WHERE a.inscripcion.idInscripcion IN :ids AND a.asistio = false " +
            "AND a.sesion.fechaListaCerrada IS NOT NULL GROUP BY a.inscripcion.idInscripcion")
    List<Object[]> contarInasistenciasPorInscripcion(Collection<Integer> ids);

    /** [idTutoria, marcas asistio=true, marcas totales] en sesiones COMPLETADA (RN-14). */
    @Query("SELECT a.sesion.horario.tutoria.idTutoria, " +
            "SUM(CASE WHEN a.asistio = true THEN 1 ELSE 0 END), COUNT(a) " +
            "FROM Asistencia a WHERE a.sesion.fechaListaCerrada IS NOT NULL " +
            "AND a.sesion.horario.tutoria.idTutoria IN :ids GROUP BY a.sesion.horario.tutoria.idTutoria")
    List<Object[]> contarMarcasPorTutoria(Collection<Integer> ids);
}
