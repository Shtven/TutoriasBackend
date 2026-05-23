package com.codespace.tutorias.DTO.Mapping;

import com.codespace.tutorias.DTO.Responsive.ComentarioResponsive;
import com.codespace.tutorias.Models.Comentario;
import com.codespace.tutorias.Models.Tutoria;
import com.codespace.tutorias.Models.Usuario;
import org.springframework.stereotype.Component;

@Component
public class ComentarioMapping {

    public Comentario toEntity(Tutoria tutoria, Usuario usuario, String texto) {
        Comentario comentario = new Comentario();
        comentario.setTutoria(tutoria);
        comentario.setUsuario(usuario);
        comentario.setComentario(texto);
        return comentario;
    }

    public ComentarioResponsive toDTO(Comentario comentario) {
        ComentarioResponsive dto = new ComentarioResponsive();
        dto.setIdComentario(comentario.getIdComentario());
        dto.setIdTutoria(comentario.getTutoria().getIdTutoria());
        dto.setMatricula(comentario.getUsuario().getMatricula());
        dto.setNombre(comentario.getUsuario().getNombre()
                + " " + comentario.getUsuario().getApellidoP()
                + " " + comentario.getUsuario().getApellidoM());
        dto.setComentario(comentario.getComentario());
        return dto;
    }
}
