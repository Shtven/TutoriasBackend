package com.codespace.tutorias.Models;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "comentarios")
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idComentario;

    @ManyToOne
    @JoinColumn(name = "matricula_tutorado")
    private Usuario tutorado;

    @ManyToOne
    @JoinColumn(name = "id_tutoria")
    private Tutoria tutoria;

    private String comentario;

    private OffsetDateTime fechaComentario;

    public Comentario() {}

    public int getIdComentario() {
        return idComentario;
    }

    public void setIdComentario(int idComentario) {
        this.idComentario = idComentario;
    }

    public Usuario getTutorado() {
        return tutorado;
    }

    public void setTutorado(Usuario tutorado) {
        this.tutorado = tutorado;
    }

    public Tutoria getTutoria() {
        return tutoria;
    }

    public void setTutoria(Tutoria tutoria) {
        this.tutoria = tutoria;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public OffsetDateTime getFechaComentario() {
        return fechaComentario;
    }

    public void setFechaComentario(OffsetDateTime fechaComentario) {
        this.fechaComentario = fechaComentario;
    }
}
