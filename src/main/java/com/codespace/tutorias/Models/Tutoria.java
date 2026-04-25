package com.codespace.tutorias.Models;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Tutoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idTutoria;
    @ManyToOne
    @JoinColumn(name = "id_horario")
    private Horario horario;
    @ManyToOne
    @JoinColumn(name = "matricula")
    private Usuario tutorado;
    @ManyToOne
    @JoinColumn(name = "id_Materia")
    private Materia materia;
    private int edificio;
    private int aula;
    private String estado;
    private LocalDate fecha;

    public Tutoria() {
    }

    public int getIdTutoria() {
        return idTutoria;
    }

    public void setIdTutoria(int idTutoria) {
        this.idTutoria = idTutoria;
    }

    public Horario getHorario() {
        return horario;
    }

    public void setHorario(Horario horario) {
        this.horario = horario;
    }

    public Usuario getTutorado() {
        return tutorado;
    }

    public void setTutorado(Usuario tutorado) {
        this.tutorado = tutorado;
    }

    public Materia getMateria() {
        return materia;
    }

    public void setMateria(Materia materia) {
        this.materia = materia;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
}
