package com.codespace.tutorias.DTO.Mapping;

import com.codespace.tutorias.DTO.Responsive.*;
import com.codespace.tutorias.Helpers.Reglas;
import com.codespace.tutorias.Models.Horario;
import com.codespace.tutorias.Models.Materia;
import com.codespace.tutorias.Models.Tema;
import com.codespace.tutorias.Models.Tutoria;
import com.codespace.tutorias.Services.AgendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TutoriaMapping {

    @Autowired
    private UsuarioMapping usuarioMapping;

    public MateriaResponsive toDTO(Materia materia) {
        return new MateriaResponsive(materia.getIdMateria(), materia.getMateria(), materia.getIdentificador());
    }

    public TemaResponsive toDTO(Tema tema) {
        return new TemaResponsive(tema.getIdTema(), tema.getTema());
    }

    public HorarioResponsive toDTO(Horario horario, boolean conTemas) {
        List<TemaResponsive> temas = conTemas
                ? horario.getTemas().stream().map(this::toDTO).toList()
                : null;
        return new HorarioResponsive(horario.getIdHorario(), horario.getDia(),
                horario.getHoraInicio(), horario.getHoraFin(), horario.getEstado(), temas);
    }

    public CupoResponsive cupo(long ocupados) {
        return new CupoResponsive(ocupados, Reglas.CUPO_MAXIMO, Math.max(0, Reglas.CUPO_MAXIMO - ocupados));
    }

    /** Resumen con los horarios ACTIVO (con o sin temas). */
    public TutoriaResumenResponsive toResumen(Tutoria tutoria, long ocupados, boolean conTemas) {
        return new TutoriaResumenResponsive(
                tutoria.getIdTutoria(),
                toDTO(tutoria.getMateria()),
                usuarioMapping.toResumen(tutoria.getTutor()),
                tutoria.getEdificio(),
                tutoria.getAula(),
                tutoria.getEstado(),
                AgendaService.activos(tutoria).stream().map(h -> toDTO(h, conTemas)).toList(),
                cupo(ocupados));
    }

    public ChoqueResponsive toDTO(AgendaService.Choque choque) {
        Horario existente = choque.existente();
        return new ChoqueResponsive(
                choque.candidato().getDia(),
                choque.candidato().getHoraInicio(),
                choque.candidato().getHoraFin(),
                new ChoqueConResponsive(
                        existente.getTutoria().getIdTutoria(),
                        toDTO(existente.getTutoria().getMateria()),
                        existente.getHoraInicio(),
                        existente.getHoraFin()));
    }

    /** Próxima sesión con los campos de la vista de detalle. */
    public ProximaSesionResponsive proxima(AgendaService.Ocurrencia ocurrencia) {
        Horario h = ocurrencia.horario();
        return new ProximaSesionResponsive(null, null, h.getIdHorario(), ocurrencia.fecha(), h.getDia(),
                h.getHoraInicio(), h.getHoraFin(), null, null, null,
                h.getHoraInicio().minusHours(Reglas.HORAS_RECORDATORIO), null);
    }
}
