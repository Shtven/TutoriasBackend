package com.codespace.tutorias.DTO.Mapping;

import com.codespace.tutorias.DTO.Request.TutoriaRequest;
import com.codespace.tutorias.DTO.Responsive.TutoriaResponsive;
import com.codespace.tutorias.Models.Asistencia;
import com.codespace.tutorias.Models.Horario;
import com.codespace.tutorias.Models.Materia;
import com.codespace.tutorias.Models.Tutoria;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TutoriaMapping {

    public Tutoria toEntity(TutoriaRequest request, Materia materia, Horario horario) {
        Tutoria entity = new Tutoria();
        List<Asistencia> asistencias = new ArrayList<>();
        entity.setAula(request.getAula());
        entity.setMateria(materia);
        entity.setHorario(horario);
        entity.setEdificio(request.getEdificio());
        entity.setAsistencias(asistencias);
        entity.setFecha(request.getFecha());
        entity.setEstado("Programada");
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

        return dto;
    }
}
