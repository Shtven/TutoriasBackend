package com.codespace.tutorias.Models;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/** Registro de recordatorios enviados; evita duplicados (RN-17). */
@Entity
public class Recordatorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idRecordatorio;

    @ManyToOne
    @JoinColumn(name = "id_inscripcion")
    private Inscripcion inscripcion;

    @ManyToOne
    @JoinColumn(name = "id_horario")
    private Horario horario;

    private LocalDate fecha;

    private OffsetDateTime fechaEnvio;

    public Recordatorio() {}

    public Recordatorio(Inscripcion inscripcion, Horario horario, LocalDate fecha, OffsetDateTime fechaEnvio) {
        this.inscripcion = inscripcion;
        this.horario = horario;
        this.fecha = fecha;
        this.fechaEnvio = fechaEnvio;
    }

    public int getIdRecordatorio() {
        return idRecordatorio;
    }

    public Inscripcion getInscripcion() {
        return inscripcion;
    }

    public Horario getHorario() {
        return horario;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public OffsetDateTime getFechaEnvio() {
        return fechaEnvio;
    }
}
