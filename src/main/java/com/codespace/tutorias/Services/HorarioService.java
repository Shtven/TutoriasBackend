package com.codespace.tutorias.Services;

import com.codespace.tutorias.DTO.Mapping.HorarioMapping;
import com.codespace.tutorias.DTO.Request.HorarioRequest;
import com.codespace.tutorias.DTO.Responsive.HorarioResponsive;
import com.codespace.tutorias.Helpers.DateHelper;
import com.codespace.tutorias.Models.Horario;
import com.codespace.tutorias.Models.Materia;
import com.codespace.tutorias.Models.Usuario;
import com.codespace.tutorias.Repositories.HorarioRepository;
import com.codespace.tutorias.Repositories.MateriaRepository;
import com.codespace.tutorias.Repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HorarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private HorarioRepository horarioRepository;
    @Autowired
    private HorarioMapping  horarioMapping;

    public void crearTutoria(HorarioRequest request, String matricula) {
        Optional<Usuario> usuario = usuarioRepository.findById(matricula);
        if (!usuario.isPresent()) {
            throw new RuntimeException("La matricula no existe");
        }

        for(Horario h: horarioRepository.findByMatricula(matricula)){
            if(request.getDia().equals(h.getDia()) &&
                    DateHelper.haySolapamiento(h.getHoraInicio(), h.getHoraFin(),
                            request.getHoraInicio(), request.getHoraFin())){
                throw new RuntimeException("Ya tienes un horario con estos datos.");
            }
        }

        Horario newHorario = horarioMapping.toEntity(request, usuario.get());

        horarioRepository.save(newHorario);
    }

    public List<HorarioResponsive> listarHorarios(String matricula) {
        return horarioRepository.findByMatricula(matricula).stream().map(horarioMapping::toDTO).toList();
    }

    public HorarioResponsive listarHorario(int idHorario) {
        Optional<Horario> horario = horarioRepository.findById(idHorario);

        if (!horario.isPresent()) {
            throw new RuntimeException("El horario no existe");
        }

        return horarioMapping.toDTO(horario.get());
    }

    public void eliminarHorario(int idHorario) {
        Optional<Horario> horario = horarioRepository.findById(idHorario);

        if (!horario.isPresent()) {
            throw new RuntimeException("El horario no existe");
        }

        horarioRepository.deleteById(idHorario);
    }

    public void modificarHorario(int idHorario, HorarioRequest request, String matricula) {
        Optional<Usuario> usuario = usuarioRepository.findById(matricula);
        Optional<Horario> horario = horarioRepository.findById(idHorario);

        if (!usuario.isPresent()) {
            throw new RuntimeException("La matricula no existe");
        }

        if (!horario.isPresent()) {
            throw new RuntimeException("El horario no existe");
        }

        for(Horario h: horarioRepository.findByMatricula(matricula)){
            if(horario.get().getDia().equals(h.getDia()) &&
                    DateHelper.haySolapamiento(h.getHoraInicio(), h.getHoraFin(),
                            horario.get().getHoraInicio(), horario.get().getHoraFin())){
                throw new RuntimeException("Ya tienes un horario con estos datos.");
            }
        }

        Horario updatedHorario = horarioMapping.toEntity(request, usuario.get());
        updatedHorario.setIdHorario(idHorario);

        horarioRepository.save(updatedHorario);
    }
}
