package com.codespace.tutorias.Services;

import com.codespace.tutorias.DTO.Mapping.TutoriaMapping;
import com.codespace.tutorias.DTO.Request.ActualizarTutoriaRequest;
import com.codespace.tutorias.DTO.Request.TutoriaRequest;
import com.codespace.tutorias.DTO.Responsive.TutoriaResponsive;
import com.codespace.tutorias.Exceptions.BusinessException;
import com.codespace.tutorias.Helpers.DateHelper;
import com.codespace.tutorias.Helpers.EstadosTutoria;
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
            throw new BusinessException("El horario no existe");
        }

        if (!materia.isPresent()) {
            throw new BusinessException("La materia no existe");
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

    public List<TutoriaResponsive> obtenerTutoriasTutorado() {
        List<Tutoria> tutorias = tutoriaRepository.findAllProgramadas();
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
            throw new BusinessException("La tutoría no existe");
        }

        TutoriaResponsive tutoriaResponse = tutoriaMapping.toDTO(tutoria.get(), tutoria.get().getHorario());

        return tutoriaResponse;
    }

    public void actualizarTutoria(int idTutoria, ActualizarTutoriaRequest request, String matricula) {
        Horario horario = horarioRepository.findById(request.getIdHorario())
                .orElseThrow(() -> new BusinessException("El horario no existe"));
        Tutoria tutoriaExistente = tutoriaRepository.findById(idTutoria)
                .orElseThrow(() -> new BusinessException("La tutoría no existe"));

        if (!tutoriaExistente.getHorario().getTutor().getMatricula().equals(matricula)) {
            throw new BusinessException("Solo el tutor dueño puede modificar esta tutoría.");
        }

        Tutoria tutoriaActualizada = tutoriaMapping.update(request, tutoriaExistente.getMateria(), horario, tutoriaExistente.getAsistencias());
        tutoriaActualizada.setIdTutoria(idTutoria);

        tutoriaRepository.save(tutoriaActualizada);

    }

    public void completarTutoria(int id, String matricula){
        Tutoria tutoria = tutoriaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("La tutoría no existe"));

        if (!tutoria.getHorario().getTutor().getMatricula().equals(matricula)) {
            throw new BusinessException("Solo el tutor dueño puede completar esta tutoría.");
        }

        if (!DateHelper.yaComenzo(tutoria.getFecha(), tutoria.getHorario().getHoraInicio())) {
            throw new BusinessException("No puedes marcar como completada una tutoría que aún no ha iniciado.");
        }

        if (EstadosTutoria.COMPLETADA.equals(tutoria.getEstado())) {
            throw new BusinessException("La tutoría ya está marcada como completada.");
        }

        if (EstadosTutoria.CANCELADA.equals(tutoria.getEstado())) {
            throw new BusinessException("No puedes completar una tutoría cancelada.");
        }

        tutoria.setEstado(EstadosTutoria.COMPLETADA);

        tutoriaRepository.save(tutoria);
    }

    public void eliminarTutoria(int idTutoria, String matricula){
        Tutoria tutoria = tutoriaRepository.findById(idTutoria)
                .orElseThrow(() -> new BusinessException("La tutoría no existe"));

        if (!tutoria.getHorario().getTutor().getMatricula().equals(matricula)) {
            throw new BusinessException("Solo el tutor dueño puede cancelar esta tutoría.");
        }

        if (DateHelper.menosDe15Min(tutoria.getFecha(), tutoria.getHorario().getHoraInicio())) {
            throw new BusinessException("No puedes cancelar con menos de 15 minutos de anticipación");
        }

        emailService.enviarCorreoCancelacion(tutoria);

        tutoria.setEstado(EstadosTutoria.CANCELADA);

        tutoriaRepository.save(tutoria);
    }
}
