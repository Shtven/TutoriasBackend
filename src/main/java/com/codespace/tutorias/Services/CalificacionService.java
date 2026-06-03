package com.codespace.tutorias.Services;

import com.codespace.tutorias.DTO.Request.CalificacionRequest;
import com.codespace.tutorias.DTO.Responsive.PromedioTutorResponsive;
import com.codespace.tutorias.Exceptions.BusinessException;
import com.codespace.tutorias.Helpers.EstadosTutoria;
import com.codespace.tutorias.Models.Asistencia;
import com.codespace.tutorias.Models.Usuario;
import com.codespace.tutorias.Repositories.AsistenciaRepository;
import com.codespace.tutorias.Repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CalificacionService {

    private static final int MIN_CALIFICACION = 1;
    private static final int MAX_CALIFICACION = 5;

    @Autowired
    private AsistenciaRepository asistenciaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    public void calificarTutoria(CalificacionRequest request, Integer matricula) {

        if (request.getCalificacion() < MIN_CALIFICACION || request.getCalificacion() > MAX_CALIFICACION) {
            throw new BusinessException("La calificación debe estar entre 1 y 5.");
        }

        Asistencia asistencia = asistenciaRepository.findById(request.getIdAsistencia())
                .orElseThrow(() -> new BusinessException("La inscripción no existe"));

        if (!asistencia.getUsuario().getMatricula().equals(matricula)) {
            throw new BusinessException("Solo puedes calificar tus propias tutorías.");
        }

        if (!EstadosTutoria.COMPLETADA.equals(asistencia.getTutoria().getEstado())) {
            throw new BusinessException("Solo puedes calificar tutorías completadas.");
        }

        if (asistencia.getCalificacion() != null) {
            throw new BusinessException("Ya calificaste esta tutoría.");
        }

        asistencia.setCalificacion(request.getCalificacion());
        asistenciaRepository.save(asistencia);
    }

    public PromedioTutorResponsive obtenerPromedioTutor(Integer matricula) {

        Usuario tutor = usuarioRepository.findById(matricula)
                .orElseThrow(() -> new BusinessException("El tutor no existe"));

        Double promedio = asistenciaRepository.calcularPromedioTutor(matricula);
        long total = asistenciaRepository.contarCalificacionesTutor(matricula);

        return new PromedioTutorResponsive(
                tutor.getMatricula(),
                tutor.getNombre() + " " + tutor.getApellidoP() + " " + tutor.getApellidoM(),
                promedio,
                total
        );
    }
}
