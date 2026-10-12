package com.codespace.tutorias.Services;

import com.codespace.tutorias.DTO.Responsive.EdificioResponsive;
import com.codespace.tutorias.Exceptions.BusinessException;
import com.codespace.tutorias.Models.Aula;
import com.codespace.tutorias.Repositories.AulaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Catálogo de lugares (tabla aula). */
@Service
public class CatalogoService {

    @Autowired
    private AulaRepository aulaRepository;

    @Transactional(readOnly = true)
    public List<EdificioResponsive> edificios() {
        Map<Integer, List<Integer>> porEdificio = new LinkedHashMap<>();
        for (Aula aula : aulaRepository.findAllByOrderByEdificioAscAulaAsc()) {
            porEdificio.computeIfAbsent(aula.getEdificio(), e -> new ArrayList<>()).add(aula.getAula());
        }
        return porEdificio.entrySet().stream()
                .map(e -> new EdificioResponsive(e.getKey(), e.getValue()))
                .toList();
    }

    public void validarAula(int edificio, int aula) {
        if (!aulaRepository.existsById(new Aula.AulaId(edificio, aula))) {
            throw new BusinessException("El aula seleccionada no existe.");
        }
    }
}
