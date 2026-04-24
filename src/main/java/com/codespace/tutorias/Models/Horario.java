package com.codespace.tutorias.Models;

import jakarta.persistence.*;

import java.sql.Time;

@Entity
public class Horario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idHorario;

    @ManyToOne
    @JoinColumn(name = "matricula")
    private Usuario tutor;

    @ManyToOne
    @JoinColumn(name = "id_Materia")
    private Materia materia;

    private String dia;

    private Time horaInicio;

    private Time horaFin;

    public Horario() {}

    public Horario(int idHorario, Usuario tutor, Materia materia, String dia, Time horaInicio, Time horaFin) {
        this.idHorario = idHorario;
        this.tutor = tutor;
        this.materia = materia;
        this.dia = dia;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public int getIdHorario() {
        return idHorario;
    }

    public void setIdHorario(int idHorario) {
        this.idHorario = idHorario;
    }

    public Usuario getTutor() {
        return tutor;
    }

    public void setTutor(Usuario tutor) {
        this.tutor = tutor;
    }

    public Materia getMateria() {
        return materia;
    }

    public void setMateria(Materia materia) {
        this.materia = materia;
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public Time getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(Time horaInicio) {
        this.horaInicio = horaInicio;
    }

    public Time getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(Time horaFin) {
        this.horaFin = horaFin;
    }

}
