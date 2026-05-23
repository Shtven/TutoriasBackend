package com.codespace.tutorias.DTO.Responsive;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class TutoriaResponsive {
    private int id;
    private LocalDate fecha;
    private String nombreTutor;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String materia;
    private int edificio;
    private int aula;
    private String estado;
    private List<TemaResponsive> temas;


    public TutoriaResponsive() {
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getNombreTutor() {
        return nombreTutor;
    }

    public void setNombreTutor(String nombreTutor) {
        this.nombreTutor = nombreTutor;
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

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public int getEdificio() {
        return edificio;
    }

    public void setEdificio(int edificio) {
        this.edificio = edificio;
    }

    public int getAula() {
        return aula;
    }

    public void setAula(int aula) {
        this.aula = aula;
    }

    public List<TemaResponsive> getTemas() {
        return temas;
    }

    public void setTemas(List<TemaResponsive> temas) {
        this.temas = temas;
    }
}
