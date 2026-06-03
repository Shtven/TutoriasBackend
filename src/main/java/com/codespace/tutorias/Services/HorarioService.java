package com.codespace.tutorias.Services;

import com.codespace.tutorias.DTO.Mapping.HorarioMapping;
import com.codespace.tutorias.DTO.Request.HorarioRequest;
import com.codespace.tutorias.DTO.Responsive.HorarioResponsive;
import com.codespace.tutorias.Exceptions.BusinessException;
import com.codespace.tutorias.Helpers.DateHelper;
import com.codespace.tutorias.Helpers.Dia;
import com.codespace.tutorias.Models.Horario;
import com.codespace.tutorias.Models.Materia;
import com.codespace.tutorias.Models.Usuario;
import com.codespace.tutorias.Repositories.HorarioRepository;
import com.codespace.tutorias.Repositories.MateriaRepository;
import com.codespace.tutorias.Repositories.TutoriaRepository;
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
    private TutoriaRepository tutoriaRepository;
    @Autowired
    private HorarioMapping  horarioMapping;

    public void crearHorario(HorarioRequest request, Integer matricula) {
        Optional<Usuario> usuario = usuarioRepository.findById(matricula);
        if (!usuario.isPresent()) {
            throw new BusinessException("La matricula no existe");
        }

        // Normaliza el dia recibido (uppercase + sin acentos + validacion).
        // Se reescribe en el request para que el mapper persista la forma canonica.
        String diaNormalizado = Dia.normalizar(request.getDia());
        request.setDia(diaNormalizado);

        for(Horario h: horarioRepository.findByMatricula(matricula)){
            // Comparacion defensiva: normaliza tambien el lado almacenado por si
            // hay registros legacy con capitalizacion distinta.
            String diaExistente = Dia.normalizar(h.getDia());
            if(diaNormalizado.equals(diaExistente) &&
                    DateHelper.haySolapamiento(h.getHoraInicio(), h.getHoraFin(),
                            request.getHoraInicio(), request.getHoraFin())){
                throw new BusinessException("Ya tienes un horario con estos datos.");
            }
        }

        Horario newHorario = horarioMapping.toEntity(request, usuario.get());

        horarioRepository.save(newHorario);
    }

    public List<HorarioResponsive> listarHorarios(Integer matricula) {
        return horarioRepository.findByMatricula(matricula).stream().map(horarioMapping::toDTO).toList();
    }

    public HorarioResponsive listarHorario(int idHorario) {
        Optional<Horario> horario = horarioRepository.findById(idHorario);

        if (!horario.isPresent()) {
            throw new BusinessException("El horario no existe");
        }

        return horarioMapping.toDTO(horario.get());
    }

    public void eliminarHorario(int idHorario, Integer matricula) {
        Horario horario = horarioRepository.findById(idHorario)
                .orElseThrow(() -> new BusinessException("El horario no existe"));

        if (!horario.getTutor().getMatricula().equals(matricula)) {
            throw new BusinessException("Solo el tutor dueño puede eliminar este horario.");
        }

        if (tutoriaRepository.existsByHorario_IdHorario(idHorario)) {
            throw new BusinessException("No puedes eliminar este horario porque tiene tutorías asociadas.");
        }

        horarioRepository.deleteById(idHorario);
    }

    public void modificarHorario(int idHorario, HorarioRequest request, Integer matricula) {
        Optional<Usuario> usuario = usuarioRepository.findById(matricula);
        Optional<Horario> horario = horarioRepository.findById(idHorario);

        if (!usuario.isPresent()) {
            throw new BusinessException("La matricula no existe");
        }

        if (!horario.isPresent()) {
            throw new BusinessException("El horario no existe");
        }

        if (!horario.get().getTutor().getMatricula().equals(matricula)) {
            throw new BusinessException("Solo el tutor dueño puede modificar este horario.");
        }

        String diaNormalizado = Dia.normalizar(request.getDia());
        request.setDia(diaNormalizado);

        for(Horario h: horarioRepository.findByMatricula(matricula)){
            if (h.getIdHorario() == idHorario) continue;
            String diaExistente = Dia.normalizar(h.getDia());
            if(diaNormalizado.equals(diaExistente) && DateHelper.haySolapamiento(h.getHoraInicio(), h.getHoraFin(),
                    request.getHoraInicio(), request.getHoraFin())){
                 throw new BusinessException("Ya tienes un horario con estos datos.");
             }
        }

        Horario updatedHorario = horarioMapping.toEntity(request, usuario.get());
        updatedHorario.setIdHorario(idHorario);

        horarioRepository.save(updatedHorario);
    }
}
