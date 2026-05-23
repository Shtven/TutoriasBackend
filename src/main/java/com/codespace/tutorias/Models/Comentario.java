package com.codespace.tutorias.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "comentarios")
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comentario")
    private int idComentario;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "matricula_usuario")
    private Usuario usuario;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "id_tutoria")
    private Tutoria tutoria;

    @Column(name = "comentario", length = 255, nullable = false)
    private String comentario;

    public Comentario() {}

    public Comentario(Usuario usuario, Tutoria tutoria, String comentario) {
        this.usuario = usuario;
        this.tutoria = tutoria;
        this.comentario = comentario;
    }

    public int getIdComentario() {
        return idComentario;
    }

    public void setIdComentario(int idComentario) {
        this.idComentario = idComentario;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
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
}
