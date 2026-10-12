package com.codespace.tutorias.Repositories;

import com.codespace.tutorias.Models.Aula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AulaRepository extends JpaRepository<Aula, Aula.AulaId> {

    List<Aula> findAllByOrderByEdificioAscAulaAsc();
}
