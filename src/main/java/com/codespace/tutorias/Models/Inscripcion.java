package com.codespace.tutorias.Models;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

/** Inscripción de un tutorado a una tutoría (incluye todos sus horarios activos). */
@Entity
public class Inscripcion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idInscripcion;

    @ManyToOne
    @JoinColumn(name = "id_tutoria")
    private Tutoria tutoria;

    @ManyToOne
    @JoinColumn(name = "matricula_tutorado")
    private Usuario tutorado;

    private OffsetDateTime fechaInscripcion;

    /** ACTIVA | BAJA | BAJA_AUTOMATICA. */
    private String estado;

    private OffsetDateTime fechaBaja;

    public Inscripcion() {}

    public int getIdInscripcion() {
        return idInscripcion;
    }

    public void setIdInscripcion(int idInscripcion) {
        this.idInscripcion = idInscripcion;
    }

    public Tutoria getTutoria() {
        return tutoria;
    }

    public void setTutoria(Tutoria tutoria) {
        this.tutoria = tutoria;
    }

    public Usuario getTutorado() {
        return tutorado;
    }

    public void setTutorado(Usuario tutorado) {
        this.tutorado = tutorado;
    }

    public OffsetDateTime getFechaInscripcion() {
        return fechaInscripcion;
    }

    public void setFechaInscripcion(OffsetDateTime fechaInscripcion) {
        this.fechaInscripcion = fechaInscripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public OffsetDateTime getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(OffsetDateTime fechaBaja) {
        this.fechaBaja = fechaBaja;
    }
}
