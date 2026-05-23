package com.codespace.tutorias.Services;

import com.codespace.tutorias.DTO.Mapping.TemaMapping;
import com.codespace.tutorias.DTO.Request.TemaRequest;
import com.codespace.tutorias.DTO.Responsive.TemaResponsive;
import com.codespace.tutorias.Exceptions.BusinessException;
import com.codespace.tutorias.Models.Tema;
import com.codespace.tutorias.Models.Tutoria;
import com.codespace.tutorias.Repositories.TemaRepository;
import com.codespace.tutorias.Repositories.TutoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemaService {

    @Autowired
    private TemaRepository temaRepository;
    @Autowired
    private TutoriaRepository tutoriaRepository;
    @Autowired
    private TemaMapping temaMapping;

    public void crearTema(TemaRequest request, String matricula) {

        if (request.getTema() == null || request.getTema().trim().isEmpty()) {
            throw new BusinessException("El tema no puede estar vacío.");
        }

        Tutoria tutoria = tutoriaRepository.findById(request.getIdTutoria())
                .orElseThrow(() -> new BusinessException("La tutoría no existe"));

        if (!"PROGRAMADA".equals(tutoria.getEstado())) {
            throw new BusinessException("Solo puedes agregar temas a tutorías programadas.");
        }

        if (!tutoria.getHorario().getTutor().getMatricula().equals(matricula)) {
            throw new BusinessException("Solo el tutor dueño de la sesión puede agregar temas.");
        }

        temaRepository.save(temaMapping.toEntity(request.getTema().trim(), tutoria));
    }

    public List<TemaResponsive> listarPorTutoria(int idTutoria) {

        if (!tutoriaRepository.existsById(idTutoria)) {
            throw new BusinessException("La tutoría no existe");
        }

        return temaRepository.findByTutoriaIdTutoria(idTutoria)
                .stream().map(temaMapping::toDTO).toList();
    }

    public void eliminarTema(int idTema, String matricula) {

        Tema tema = temaRepository.findById(idTema)
                .orElseThrow(() -> new BusinessException("El tema no existe"));

        Tutoria tutoria = tema.getTutoria();

        if (!"PROGRAMADA".equals(tutoria.getEstado())) {
            throw new BusinessException("Solo puedes eliminar temas de tutorías programadas.");
        }

        if (!tutoria.getHorario().getTutor().getMatricula().equals(matricula)) {
            throw new BusinessException("Solo el tutor dueño de la sesión puede eliminar temas.");
        }

        temaRepository.delete(tema);
    }
}
