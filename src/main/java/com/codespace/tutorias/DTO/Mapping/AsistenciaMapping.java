package com.codespace.tutorias.DTO.Mapping;

import com.codespace.tutorias.DTO.Responsive.AsistenciaResponsive;
import com.codespace.tutorias.DTO.Responsive.MisInscripcionResponsive;
import com.codespace.tutorias.Models.Asistencia;
import com.codespace.tutorias.Models.Horario;
import com.codespace.tutorias.Models.Tutoria;
import com.codespace.tutorias.Models.Usuario;
import org.springframework.stereotype.Component;

@Component
public class AsistenciaMapping {

    public Asistencia toEntity(Tutoria tutoria, Usuario usuario) {
        Asistencia asistencia = new Asistencia();
        asistencia.setTutoria(tutoria);
        asistencia.setUsuario(usuario);
        asistencia.setAsistio(false);

        return asistencia;
    }


    public AsistenciaResponsive toDTO(Asistencia asistencia) {
        AsistenciaResponsive dto = new AsistenciaResponsive();
        dto.setIdAsistencia(asistencia.getIdAsistencia());
        dto.setMatricula(asistencia.getUsuario().getMatricula());
        dto.setNombre(asistencia.getUsuario().getNombre()
                + " " + asistencia.getUsuario().getApellidoP()
                + " " + asistencia.getUsuario().getApellidoM());
        dto.setAsistio(asistencia.isAsistio());
        dto.setCalificacion(asistencia.getCalificacion());

        return dto;
    }

    /**
     * Mapeo orientado al tutorado: incluye los datos de la sesion de tutoria
     * necesarios para renderizar el dashboard de "mis inscripciones" /
     * "historial" (materia, horario, ubicacion, tutor).
     */
    public MisInscripcionResponsive toMisInscripcionDTO(Asistencia asistencia) {
        Tutoria tutoria = asistencia.getTutoria();
        Horario horario = tutoria.getHorario();
        Usuario tutor = horario.getTutor();

        MisInscripcionResponsive dto = new MisInscripcionResponsive();
        dto.setIdAsistencia(asistencia.getIdAsistencia());
        dto.setIdTutoria(tutoria.getIdTutoria());
        dto.setFecha(tutoria.getFecha());
        dto.setMateria(tutoria.getMateria().getMateria());
        dto.setDia(horario.getDia());
        dto.setHoraInicio(horario.getHoraInicio());
        dto.setHoraFin(horario.getHoraFin());
        dto.setEdificio(tutoria.getEdificio());
        dto.setAula(tutoria.getAula());
        dto.setTutor(tutor.getNombre() + " " + tutor.getApellidoP() + " " + tutor.getApellidoM());
        dto.setEstado(tutoria.getEstado());
        dto.setAsistio(asistencia.isAsistio());
        dto.setCalificacion(asistencia.getCalificacion());

        return dto;
    }
}
