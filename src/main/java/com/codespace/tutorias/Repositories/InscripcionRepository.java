package com.codespace.tutorias.Repositories;

import com.codespace.tutorias.Models.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {

    Optional<Inscripcion> findByTutoria_IdTutoriaAndTutorado_MatriculaAndEstado(
            int idTutoria, String matricula, String estado);

    boolean existsByTutoria_IdTutoriaAndTutorado_MatriculaAndEstado(
            int idTutoria, String matricula, String estado);

    /** Inscripción más reciente de un tutorado en una tutoría (cualquier estado). */
    Optional<Inscripcion> findFirstByTutoria_IdTutoriaAndTutorado_MatriculaOrderByFechaInscripcionDesc(
            int idTutoria, String matricula);

    List<Inscripcion> findByTutoria_IdTutoriaAndEstadoOrderByTutorado_NombreAscTutorado_ApellidosAsc(
            int idTutoria, String estado);

    List<Inscripcion> findByTutoria_IdTutoria(int idTutoria);

    List<Inscripcion> findByTutorado_MatriculaOrderByFechaInscripcionDesc(String matricula);

    long countByTutoria_IdTutoriaAndEstado(int idTutoria, String estado);

    long countByEstado(String estado);

    /** Inscripciones ACTIVA en tutorías ACTIVA (tarjeta del admin). */
    @Query("SELECT COUNT(i) FROM Inscripcion i WHERE i.estado = 'ACTIVA' AND i.tutoria.estado = 'ACTIVA'")
    long contarActivasEnTutoriasActivas();

    long countByEstadoAndFechaBajaGreaterThanEqual(String estado, OffsetDateTime desde);

    /** [idTutoria, inscripciones ACTIVA] para varias tutorías a la vez. */
    @Query("SELECT i.tutoria.idTutoria, COUNT(i) FROM Inscripcion i " +
            "WHERE i.estado = 'ACTIVA' AND i.tutoria.idTutoria IN :ids GROUP BY i.tutoria.idTutoria")
    List<Object[]> contarActivasPorTutoria(Collection<Integer> ids);

    /** idTutoria de las tutorías donde el tutorado tuvo baja automática (no puede reinscribirse). */
    @Query("SELECT DISTINCT i.tutoria.idTutoria FROM Inscripcion i " +
            "WHERE i.tutorado.matricula = :matricula AND i.estado = 'BAJA_AUTOMATICA'")
    List<Integer> findTutoriasConBajaAutomatica(String matricula);
}
