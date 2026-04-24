package com.codespace.tutorias.DTO.Mapping;

import com.codespace.tutorias.DTO.Request.HorarioRequest;
import com.codespace.tutorias.Models.Horario;
import com.codespace.tutorias.Models.Materia;
import com.codespace.tutorias.Models.Usuario;
import com.codespace.tutorias.Repositories.HorarioRepository;
import com.codespace.tutorias.Repositories.MateriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HorarioMapping {

    public Horario toEntity(HorarioRequest request, Usuario usuario, Materia materia) {
        Horario entity = new Horario();
        entity.setTutor(usuario);
        entity.setMateria(materia);
        entity.setDia(request.getDia());
        entity.setHoraInicio(request.getHoraInicio());
        entity.setHoraFin(request.getHoraFin());

        return  entity;
    }

}
