package com.codespace.tutorias.Services;

import com.codespace.tutorias.DTO.Mapping.TutoriaMapping;
import com.codespace.tutorias.DTO.Request.TutoriaRequest;
import com.codespace.tutorias.DTO.Responsive.TutoriaResponsive;
import com.codespace.tutorias.Models.Horario;
import com.codespace.tutorias.Models.Materia;
import com.codespace.tutorias.Models.Tutoria;
import com.codespace.tutorias.Repositories.HorarioRepository;
import com.codespace.tutorias.Repositories.MateriaRepository;
import com.codespace.tutorias.Repositories.TutoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Service
public class TutoriaService {

    @Autowired
    private HorarioRepository horarioRepository;
    @Autowired
    private MateriaRepository materiaRepository;
    @Autowired
    private TutoriaRepository tutoriaRepository;
    @Autowired
    private TutoriaMapping tutoriaMapping;
    @Autowired
    private EmailService emailService;

    public void crearTutoria(TutoriaRequest request) {
        Optional<Horario> horario = horarioRepository.findById(request.getIdHorario());
        Optional<Materia> materia = materiaRepository.findByNrc(request.getNrc());

        if (!horario.isPresent()) {
            throw new RuntimeException("El horario no existe");
        }

        if (!materia.isPresent()) {
            throw new RuntimeException("La materia no existe");
        }

        Tutoria tutoria = tutoriaMapping.toEntity(request, materia.get(), horario.get());

        tutoriaRepository.save(tutoria);
    }

    public List<TutoriaResponsive> obtenerTutorias(String matricula) {
        List<Tutoria> tutorias = tutoriaRepository.findAllByMatricula(matricula);
        List<TutoriaResponsive> tutoriasResponse = new ArrayList<>();
        for(Tutoria t: tutorias){
            TutoriaResponsive tutoria = tutoriaMapping.toDTO(t, t.getHorario());
            tutoriasResponse.add(tutoria);
        }

        return tutoriasResponse;
    }

    public TutoriaResponsive obtenerTutoriaPorId(int idTutoria) {
        Optional<Tutoria> tutoria = tutoriaRepository.findById(idTutoria);

        if (!tutoria.isPresent()) {
            throw new RuntimeException("La tutoría no existe");
        }

        TutoriaResponsive tutoriaResponse = tutoriaMapping.toDTO(tutoria.get(), tutoria.get().getHorario());

        return tutoriaResponse;
    }

    public void actualizarTutoria(int idTutoria, TutoriaRequest request) {
        Optional<Tutoria> tutoria = tutoriaRepository.findById(idTutoria);
        Optional<Horario> horario = horarioRepository.findById(request.getIdHorario());
        Optional<Materia> materia = materiaRepository.findByNrc(request.getNrc());

        if (!horario.isPresent()) {
            throw new RuntimeException("El horario no existe");
        }

        if (!materia.isPresent()) {
            throw new RuntimeException("La materia no existe");
        }

        if (!tutoria.isPresent()) {
            throw new RuntimeException("El horario no existe");
        }

        Tutoria tutoriaActualizada = tutoriaMapping.toEntity(request, materia.get(), horario.get());
        tutoriaActualizada.setIdTutoria(idTutoria);

        tutoriaRepository.save(tutoriaActualizada);

    }

    public void eliminarTutoria(int idTutoria){
        Tutoria tutoria = tutoriaRepository.findById(idTutoria)
                .orElseThrow(() -> new RuntimeException("La tutoría no existe"));

        emailService.enviarCorreoCancelacion(tutoria);

        tutoria.setEstado("CANCELADA");

        tutoriaRepository.save(tutoria);
    }
}
