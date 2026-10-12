package com.codespace.tutorias.Repositories;

import com.codespace.tutorias.Models.Recordatorio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface RecordatorioRepository extends JpaRepository<Recordatorio, Integer> {

    boolean existsByInscripcion_IdInscripcionAndHorario_IdHorarioAndFecha(
            int idInscripcion, int idHorario, LocalDate fecha);
}
