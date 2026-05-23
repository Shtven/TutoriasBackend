package com.codespace.tutorias.DTO.Mapping;

import com.codespace.tutorias.DTO.Responsive.TemaResponsive;
import com.codespace.tutorias.Models.Tema;
import com.codespace.tutorias.Models.Tutoria;
import org.springframework.stereotype.Component;

@Component
public class TemaMapping {

    public Tema toEntity(String texto, Tutoria tutoria) {
        Tema tema = new Tema();
        tema.setTema(texto);
        tema.setTutoria(tutoria);
        return tema;
    }

    public TemaResponsive toDTO(Tema tema) {
        TemaResponsive dto = new TemaResponsive();
        dto.setIdTema(tema.getIdTema());
        dto.setTema(tema.getTema());
        return dto;
    }
}
