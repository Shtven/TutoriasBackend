package com.codespace.tutorias.Models;

import jakarta.persistence.*;

/** Marca de un inscrito en una sesión. "Sin marca" = no existe la fila. */
@Entity
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idAsistencia;

    @ManyToOne
    @JoinColumn(name = "id_sesion")
    private Sesion sesion;

    @ManyToOne
    @JoinColumn(name = "id_inscripcion")
    private Inscripcion inscripcion;

    private boolean asistio;

    public Asistencia() {}

    public Asistencia(Sesion sesion, Inscripcion inscripcion, boolean asistio) {
        this.sesion = sesion;
        this.inscripcion = inscripcion;
        this.asistio = asistio;
    }

    public int getIdAsistencia() {
        return idAsistencia;
    }

    public void setIdAsistencia(int idAsistencia) {
        this.idAsistencia = idAsistencia;
    }

    public Sesion getSesion() {
        return sesion;
    }

    public void setSesion(Sesion sesion) {
        this.sesion = sesion;
    }

    public Inscripcion getInscripcion() {
        return inscripcion;
    }

    public void setInscripcion(Inscripcion inscripcion) {
        this.inscripcion = inscripcion;
    }

    public boolean isAsistio() {
        return asistio;
    }

    public void setAsistio(boolean asistio) {
        this.asistio = asistio;
    }
}
