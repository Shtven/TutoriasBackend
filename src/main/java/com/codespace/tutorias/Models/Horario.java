package com.codespace.tutorias.Models;

import jakarta.persistence.*;

import java.time.LocalTime;

@Entity
public class Horario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idHorario;

    @ManyToOne
    @JoinColumn(name = "matricula_tutor")
    private Usuario tutor;

    private String dia;

    private LocalTime horaInicio;

    private LocalTime horaFin;

    public Horario() {}

    public Horario(int idHorario, Usuario tutor, String dia, LocalTime horaInicio, LocalTime horaFin) {
        this.idHorario = idHorario;
        this.tutor = tutor;
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

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }
}
