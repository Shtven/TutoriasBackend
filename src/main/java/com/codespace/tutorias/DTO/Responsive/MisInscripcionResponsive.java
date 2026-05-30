package com.codespace.tutorias.DTO.Responsive;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO para los endpoints orientados al tutorado:
 *   - GET /asistencia/mis-inscripciones
 *   - GET /asistencia/historial
 *
 * Contiene los datos necesarios para que el dashboard del tutorado pueda
 * renderizar cada inscripcion (materia, horario, ubicacion, tutor) y
 * realizar acciones sobre ella (cancelar via idAsistencia).
 */
public class MisInscripcionResponsive {

    private int idAsistencia;
    private int idTutoria;
    private LocalDate fecha;
    private String materia;
    private String dia;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private int edificio;
    private int aula;
    private String tutor;
    private String estado;
    private Boolean asistio;
    private Integer calificacion;

    public MisInscripcionResponsive() {}

    public int getIdAsistencia() {
        return idAsistencia;
    }

    public void setIdAsistencia(int idAsistencia) {
        this.idAsistencia = idAsistencia;
    }

    public int getIdTutoria() {
        return idTutoria;
    }

    public void setIdTutoria(int idTutoria) {
        this.idTutoria = idTutoria;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
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

    public String getTutor() {
        return tutor;
    }

    public void setTutor(String tutor) {
        this.tutor = tutor;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Boolean getAsistio() {
        return asistio;
    }

    public void setAsistio(Boolean asistio) {
        this.asistio = asistio;
    }

    public Integer getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(Integer calificacion) {
        this.calificacion = calificacion;
    }
}
