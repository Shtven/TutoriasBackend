package com.codespace.tutorias.Services;

import com.codespace.tutorias.DTO.Mapping.TutoriaMapping;
import com.codespace.tutorias.DTO.Responsive.TemaResponsive;
import com.codespace.tutorias.Exceptions.BusinessException;
import com.codespace.tutorias.Helpers.EstadosHorario;
import com.codespace.tutorias.Helpers.EstadosTutoria;
import com.codespace.tutorias.Helpers.Reglas;
import com.codespace.tutorias.Helpers.Roles;
import com.codespace.tutorias.Models.Horario;
import com.codespace.tutorias.Models.Tema;
import com.codespace.tutorias.Repositories.HorarioRepository;
import com.codespace.tutorias.Repositories.TemaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Temas por horario; editables aunque haya inscritos (RN-15). */
@Service
public class TemaService {

    @Autowired
    private HorarioRepository horarioRepository;

    @Autowired
    private TemaRepository temaRepository;

    @Autowired
    private TutoriaService tutoriaService;

    @Autowired
    private TutoriaMapping mapping;

    @Transactional
    public TemaResponsive agregar(int idHorario, String matricula, String texto) {
        Horario horario = horarioRepository.findById(idHorario)
                .orElseThrow(() -> new BusinessException("Horario no encontrado."));
        validarEditable(horario, matricula);

        String tema = texto.trim();
        if (tema.isEmpty()) {
            throw new BusinessException("El tema es obligatorio.");
        }
        if (tema.length() > Reglas.MAX_CARACTERES_TEMA) {
            throw new BusinessException("El tema no puede exceder " + Reglas.MAX_CARACTERES_TEMA + " caracteres.");
        }
        if (temaRepository.countByHorario_IdHorario(idHorario) >= Reglas.MAX_TEMAS_POR_HORARIO) {
            throw new BusinessException("Máximo " + Reglas.MAX_TEMAS_POR_HORARIO + " temas por horario.");
        }
        if (temaRepository.existsByHorario_IdHorarioAndTemaIgnoreCase(idHorario, tema)) {
            throw new BusinessException("El tema ya existe en este horario.");
        }

        Tema nuevo = new Tema(tema, horario);
        horario.getTemas().add(nuevo);
        temaRepository.save(nuevo);
        return mapping.toDTO(nuevo);
    }

    @Transactional
    public void eliminar(int idTema, String matricula) {
        Tema tema = temaRepository.findById(idTema)
                .orElseThrow(() -> new BusinessException("Tema no encontrado."));
        Horario horario = tema.getHorario();
        validarEditable(horario, matricula);
        horario.getTemas().remove(tema);
    }

    private void validarEditable(Horario horario, String matricula) {
        tutoriaService.verificarDueno(horario.getTutoria(), matricula, Roles.TUTOR);
        if (!EstadosTutoria.ACTIVA.equals(horario.getTutoria().getEstado())
                || !EstadosHorario.ACTIVO.equals(horario.getEstado())) {
            throw new BusinessException("El horario no está activo.");
        }
    }
}
