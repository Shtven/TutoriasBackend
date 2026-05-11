package com.codespace.tutorias.Services;

import com.codespace.tutorias.DTO.Mapping.MateriaMapping;
import com.codespace.tutorias.DTO.Request.MateriaRequest;
import com.codespace.tutorias.Exceptions.BusinessException;
import com.codespace.tutorias.Models.Materia;
import com.codespace.tutorias.Repositories.MateriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MateriaService {

    @Autowired
    private MateriaRepository materiaRepository;
    @Autowired
    private MateriaMapping materiaMapping;

    public void createMateria(MateriaRequest request) {
        Materia materia = materiaRepository.findByNrc(request.getNrc())
                .orElseThrow(() -> new BusinessException("La materia ya existe"));

        materiaRepository.save(materiaMapping.toEntity(request));
    }

    public Materia getMateria(int nrc) {
        return materiaRepository.findByNrc(nrc)
                .orElseThrow(() -> new BusinessException("La materia no existe"));
    }

    public List<Materia> getMaterias() {
        return materiaRepository.findAll();
    }

    public void deleteMateria(int nrc) {
        Materia materia = materiaRepository.findByNrc(nrc)
                .orElseThrow(() -> new BusinessException("La materia no existe"));

        materiaRepository.delete(materia);
    }

    public void updateMateria(int nrc, MateriaRequest request) {
        Materia materia = materiaRepository.findByNrc(nrc)
                .orElseThrow(() -> new BusinessException("La materia no existe"));

        materia.setMateria(request.getNombre());
        materia.setNrc(request.getNrc());

        materiaRepository.save(materia);
    }
}
