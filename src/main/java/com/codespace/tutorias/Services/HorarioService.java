package com.codespace.tutorias.Services;

import com.codespace.tutorias.DTO.Mapping.HorarioMapping;
import com.codespace.tutorias.DTO.Request.HorarioRequest;
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
    private MateriaRepository materiaRepository;
    @Autowired
    private HorarioRepository horarioRepository;
    @Autowired
    private HorarioMapping  horarioMapping;

//    public void crearTutoria(HorarioRequest request, String matricula) {
//        Optional<Usuario> usuario = usuarioRepository.findById(matricula);
//        Optional<Materia> materia = materiaRepository.findByNrc(request.getNrc());
//
//        if (!usuario.isPresent()) {
//            throw new RuntimeException("La matricula no existe");
//        }
//
//        if (!materia.isPresent()) {
//            throw new RuntimeException("La materia no existe");
//        }
//
//        Horario newHorario = horarioMapping.toEntity(request, usuario.get(), materia.get());
//
//        horarioRepository.save(newHorario);
//    }
//
//    public void modificarTutoria(HorarioRequest request, String matricula) {
//        Optional<Usuario> usuario = usuarioRepository.findById(matricula);
//        Optional<Materia> materia = materiaRepository.findByNrc(request.getNrc());
//        if (!usuario.isPresent()) {
//            throw new RuntimeException("La matricula no existe");
//        }
//
//        if (!materia.isPresent()) {
//            throw new RuntimeException("La materia no existe");
//        }
//
//
//    }

    public List<HorariosMostrarDTO> listarHorariosPublicos() {
        return horarioRepository.findAll()
                .stream()
                .map(horarioMapping::convertirAPublica)
                .toList();
    }

    public Optional<HorariosMostrarDTO> buscarHorarioPublico(int id) {
        return horarioRepository.findById(id)
                .map(horarioMapping::convertirAPublica);
    }

    public HorariosDTO crearHorario(CrearHorarioDTO dto, String matricula) {
        Horario horario = horarioMapping.convertirANuevaEntidad(dto, matricula);

        List<Horario> horariosTutor = findByTutor(matricula);

        for(Horario h: horariosTutor){
            if(horario.getDia().equals(h.getDia()) &&
                    DateHelper.haySolapamiento(h.getHoraInicio(), h.getHoraFin(),
                            horario.getHoraInicio(), horario.getHoraFin())){
                throw new BusinessException("Ya tienes un horario con estos datos.");
            }
        }
        return horarioMapping.convertirADTO(horarioRepository.save(horario));
    }

    public Optional<Horario> actualizarHorario(int id, HorariosDTO dto) {
        return horarioRepository.findById(id)
                .map(existing -> {
                    var entidad = horarioMapping.convertirAEntidad(dto);
                    entidad.setIdHorario(id);
                    return horarioRepository.save(entidad);
                });
    }

    public void eliminarHorario(int id) {
        horarioRepository.deleteById(id);
    }


    private List<Horario> findByTutor(String matricula){
        return horarioRepository.findByTutor(matricula);
    }

    public List<HorariosMostrarDTO> misHorarios(String matricula){
        return horarioRepository.findByTutor(matricula)
                .stream()
                .map(horarioMapping::convertirAPublica)
                .toList();

    }
}
