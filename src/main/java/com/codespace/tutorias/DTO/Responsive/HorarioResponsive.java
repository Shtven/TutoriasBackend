package com.codespace.tutorias.DTO.Responsive;

import java.sql.Time;

public class HorarioResponsive {
    private int id;
    private String dia;
    private Time horaInicio;
    private Time horaFin;

    public HorarioResponsive() {
    }

    public HorarioResponsive(int id, String dia, Time horaInicio, Time horaFin) {
        this.id = id;
        this.dia = dia;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
