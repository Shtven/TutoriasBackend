package com.codespace.tutorias.DTO.Mapping;

import com.codespace.tutorias.DTO.Request.ActualizarTutoriaRequest;
import com.codespace.tutorias.DTO.Request.TutoriaRequest;
import com.codespace.tutorias.Helpers.EstadosTutoria;
import com.codespace.tutorias.DTO.Responsive.TemaResponsive;
import com.codespace.tutorias.DTO.Responsive.TutoriaResponsive;
import com.codespace.tutorias.Models.Asistencia;
import com.codespace.tutorias.Models.Horario;
import com.codespace.tutorias.Models.Materia;
import com.codespace.tutorias.Models.Tema;
import com.codespace.tutorias.Models.Tutoria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TutoriaMapping {

    @Autowired
    private TemaMapping temaMapping;

    public Tutoria toEntity(TutoriaRequest request, Materia materia, Horario horario) {
        Tutoria entity = new Tutoria();
        List<Asistencia> asistencias = new ArrayList<>();
        entity.setAula(request.getAula());
        entity.setMateria(materia);
        entity.setHorario(horario);
        entity.setEdificio(request.getEdificio());
        entity.setAsistencias(asistencias);
        entity.setFecha(request.getFecha());
        entity.setEstado(EstadosTutoria.PROGRAMADA);

        List<Tema> temas = new ArrayList<>();
        if (request.getTemas() != null) {
            for (String texto : request.getTemas()) {
                if (texto != null && !texto.trim().isEmpty()) {
                    temas.add(temaMapping.toEntity(texto.trim(), entity));
                }
            }
        }
        entity.setTemas(temas);
        return entity;
    }

    public TutoriaResponsive toDTO(Tutoria entity, Horario horario) {
        TutoriaResponsive dto = new TutoriaResponsive();
        dto.setId(entity.getIdTutoria());
        dto.setFecha(entity.getFecha());
        dto.setNombreTutor(horario.getTutor().getNombre() +
                " " + horario.getTutor().getApellidoP() +
                " " + horario.getTutor().getApellidoM());
        dto.setHoraInicio(horario.getHoraInicio());
        dto.setHoraFin(horario.getHoraFin());
        dto.setMateria(entity.getMateria().getMateria());
        dto.setAula(entity.getAula());
        dto.setEdificio(entity.getEdificio());
        dto.setEstado(entity.getEstado());

        List<TemaResponsive> temasDTO = new ArrayList<>();
        if (entity.getTemas() != null) {
            for (Tema t : entity.getTemas()) {
                temasDTO.add(temaMapping.toDTO(t));
            }
        }
        dto.setTemas(temasDTO);

        return dto;
    }

    public Tutoria update(ActualizarTutoriaRequest request, Materia materia, Horario horario, List<Asistencia> asistencias) {
        Tutoria entity = new Tutoria();
        entity.setAula(request.getAula());
        entity.setMateria(materia);
        entity.setHorario(horario);
        entity.setEdificio(request.getEdificio());
        entity.setAsistencias(asistencias);
        entity.setFecha(request.getFecha());
        entity.setEstado(EstadosTutoria.PROGRAMADA);
        return entity;
    }
}
