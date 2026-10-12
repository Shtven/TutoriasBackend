package com.codespace.tutorias.Models;

import jakarta.persistence.*;

@Entity
@Table(name = "temas")
public class Tema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idTema;

    private String tema;

    @ManyToOne
    @JoinColumn(name = "id_horario")
    private Horario horario;

    public Tema() {}

    public Tema(String tema, Horario horario) {
        this.tema = tema;
        this.horario = horario;
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

    public Horario getHorario() {
        return horario;
    }

    public void setHorario(Horario horario) {
        this.horario = horario;
    }
}
