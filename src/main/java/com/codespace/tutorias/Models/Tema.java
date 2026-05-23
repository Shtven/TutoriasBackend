package com.codespace.tutorias.Models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "temas")
public class Tema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tema")
    private int idTema;

    @Column(name = "tema", nullable = false)
    private String tema;

    @JsonBackReference
    @ManyToOne
    @JoinColumn(name = "id_tutoria")
    private Tutoria tutoria;

    public Tema() {}

    public Tema(String tema, Tutoria tutoria) {
        this.tema = tema;
        this.tutoria = tutoria;
    }

    public int getIdTema() {
        return idTema;
    }

    public void setIdTema(int idTema) {
        this.idTema = idTema;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public Tutoria getTutoria() {
        return tutoria;
    }

    public void setTutoria(Tutoria tutoria) {
        this.tutoria = tutoria;
    }
}
