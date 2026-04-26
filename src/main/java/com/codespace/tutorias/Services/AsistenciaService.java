package com.codespace.tutorias.Services;

import com.codespace.tutorias.DTO.Mapping.AsistenciaMapping;
import com.codespace.tutorias.DTO.Request.AsistenciaRequest;
import com.codespace.tutorias.DTO.Responsive.AsistenciaResponsive;
import com.codespace.tutorias.Helpers.DateHelper;
import com.codespace.tutorias.Models.Asistencia;
import com.codespace.tutorias.Models.Horario;
import com.codespace.tutorias.Models.Tutoria;
import com.codespace.tutorias.Models.Usuario;
import com.codespace.tutorias.Repositories.AsistenciaRepository;
import com.codespace.tutorias.Repositories.TutoriaRepository;
import com.codespace.tutorias.Repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AsistenciaService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private AsistenciaRepository asistenciaRepository;
    @Autowired
    private TutoriaRepository tutoriaRepository;
    @Autowired
    private AsistenciaMapping asistenciaMapping;

    public void crearAsistencia(AsistenciaRequest request, String matricula) {

        Usuario usuario = usuarioRepository.findById(matricula)
                .orElseThrow(() -> new RuntimeException("El usuario no existe"));

        Tutoria tutoria = tutoriaRepository.findById(request.getIdTutoria())
                .orElseThrow(() -> new RuntimeException("La tutoría no existe"));

        if (DateHelper.yaComenzo(tutoria.getFecha(), tutoria.getHorario().getHoraInicio())) {
            throw new RuntimeException("La tutoría ya ha comenzado, ya no puedes inscribirte.");
        }

        if (!"PROGRAMADA".equals(tutoria.getEstado())) {
            throw new RuntimeException("La tutoría no está disponible");
        }

        for (Asistencia a : asistenciaRepository.findByUsuarioMatricula(matricula)) {

            Tutoria existente = a.getTutoria();

            if (existente.getHorario().getDia().equals(tutoria.getHorario().getDia()) &&
                    DateHelper.haySolapamiento(
                            existente.getHorario().getHoraInicio(),
                            existente.getHorario().getHoraFin(),
                            tutoria.getHorario().getHoraInicio(),
                            tutoria.getHorario().getHoraFin()
                    )) {
                throw new RuntimeException("Ya estás inscrito en otra tutoría con el mismo horario.");
            }
        }

        long inscritos = asistenciaRepository.countByTutoriaIdTutoria(request.getIdTutoria());

        if (inscritos >= 5) {
            throw new RuntimeException("La tutoría está llena.");
        }

        if (asistenciaRepository.findByMatriculaTutoria(matricula, request.getIdTutoria()).isPresent()) {
            throw new RuntimeException("Ya existe una asistencia para esta tutoría");
        }

        asistenciaRepository.save(asistenciaMapping.toEntity(tutoria, usuario));
    }

    public void eliminarAsistencia(int idAsistencia, String matricula) {

        Asistencia asistencia = asistenciaRepository.findById(idAsistencia)
                .orElseThrow(() -> new RuntimeException("La asistencia no existe"));

        if (!asistencia.getUsuario().getMatricula().equals(matricula)) {
            throw new RuntimeException("No puedes eliminar esta asistencia");
        }

        asistenciaRepository.delete(asistencia);
    }

    public List<AsistenciaResponsive> listarAsistencias(int idTutoria) {
        return asistenciaRepository.findByTutoriaIdTutoria(idTutoria)
                .stream().map(asistenciaMapping::toDTO).toList();
    }

    public AsistenciaResponsive listarAsistencia(int idAsistencia) {
        Asistencia asistencia = asistenciaRepository.findById(idAsistencia)
                .orElseThrow(()-> new RuntimeException("La asistencia no existe"));

        return asistenciaMapping.toDTO(asistencia);
    }

    public void actualizarAsistencia(boolean asistio, int idAsistencia) {
        Asistencia asistencia = asistenciaRepository.findById(idAsistencia)
                .orElseThrow(()-> new RuntimeException("La asistencia no existe"));

        asistencia.setAsistio(asistio);

        asistenciaRepository.save(asistencia);
    }


}
