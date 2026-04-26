package com.codespace.tutorias.DTO.Mapping;

import com.codespace.tutorias.DTO.Responsive.AsistenciaResponsive;
import com.codespace.tutorias.Models.Asistencia;
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
        dto.setMatricula(asistencia.getUsuario().getMatricula());
        dto.setNombre(asistencia.getUsuario().getNombre()
                + " " + asistencia.getUsuario().getApellidoP()
                + " " + asistencia.getUsuario().getApellidoM());
        dto.setAsistio(asistencia.isAsistio());

        return dto;
    }
}
