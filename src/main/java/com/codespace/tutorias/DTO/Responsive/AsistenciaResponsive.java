package com.codespace.tutorias.DTO.Responsive;

public class AsistenciaResponsive {
    private int idAsistencia;
    private Integer matricula;
    private String nombre;
    private Boolean asistio;
    private Integer calificacion;

    public AsistenciaResponsive() {}

    public AsistenciaResponsive(int idAsistencia, Integer matricula, String nombre, Boolean asistio, Integer calificacion) {
        this.idAsistencia = idAsistencia;
        this.matricula = matricula;
        this.nombre = nombre;
        this.asistio = asistio;
        this.calificacion = calificacion;
    }

    public int getIdAsistencia() {
        return idAsistencia;
    }

    public void setIdAsistencia(int idAsistencia) {
        this.idAsistencia = idAsistencia;
    }

    public Integer getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(Integer calificacion) {
        this.calificacion = calificacion;
    }

    public Integer getMatricula() {
        return matricula;
    }

    public void setMatricula(Integer matricula) {
        this.matricula = matricula;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getAsistio() {
        return asistio;
    }

    public void setAsistio(Boolean asistio) {
        this.asistio = asistio;
    }
}
