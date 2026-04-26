package com.codespace.tutorias.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
public class Asistencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idAsistencia;
    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "id_tutoria")
    private Tutoria tutoria;
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "matricula")
    private Usuario usuario;

    private boolean asistio;


    public Asistencia() {}

    public Asistencia(int idAsistencia, Tutoria tutoria, Usuario usuario) {
        this.idAsistencia = idAsistencia;
        this.tutoria = tutoria;
        this.usuario = usuario;
    }

    public int getIdAsistencia() {
        return idAsistencia;
    }

    public void setIdAsistencia(int idAsistencia) {
        this.idAsistencia = idAsistencia;
    }

    public Tutoria getTutoria() {
        return tutoria;
    }

    public void setTutoria(Tutoria tutoria) {
        this.tutoria = tutoria;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public boolean isAsistio() {
        return asistio;
    }

    public void setAsistio(boolean asistio) {
        this.asistio = asistio;
    }
}
