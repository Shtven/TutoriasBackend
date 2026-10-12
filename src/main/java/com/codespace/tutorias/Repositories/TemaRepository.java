package com.codespace.tutorias.Repositories;

import com.codespace.tutorias.Models.Tema;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemaRepository extends JpaRepository<Tema, Integer> {

    long countByHorario_IdHorario(int idHorario);

    boolean existsByHorario_IdHorarioAndTemaIgnoreCase(int idHorario, String tema);
}
