package com.codespace.tutorias.DTO.Mapping;

import com.codespace.tutorias.DTO.Request.MateriaRequest;
import com.codespace.tutorias.Models.Materia;
import org.springframework.stereotype.Component;

@Component
public class MateriaMapping {

    public Materia toEntity(MateriaRequest request) {
        Materia entity = new Materia();
        entity.setMateria(request.getNombre());
        entity.setNrc(request.getNrc());
        return entity;
    }
}

