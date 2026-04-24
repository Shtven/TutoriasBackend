package com.codespace.tutorias.Repositories;

import com.codespace.tutorias.Models.Materia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface MateriaRepository extends JpaRepository<Materia, Integer> {

    @Query("SELECT m from Materia m WHERE m.nrc=:nrc")
    Optional<Materia> findByNrc(int nrc);

}
