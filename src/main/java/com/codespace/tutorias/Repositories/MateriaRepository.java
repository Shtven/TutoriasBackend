package com.codespace.tutorias.Repositories;

import com.codespace.tutorias.Models.Materia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MateriaRepository extends JpaRepository<Materia, Integer> {

    Optional<Materia> findByIdentificador(int identificador);

    List<Materia> findAllByOrderByMateriaAsc();
}
