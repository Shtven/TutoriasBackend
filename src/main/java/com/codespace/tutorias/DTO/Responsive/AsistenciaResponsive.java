package com.codespace.tutorias.DTO.Responsive;

public class AsistenciaResponsive {
    private String matricula;
    private String nombre;
    private Boolean asistio;

    public AsistenciaResponsive() {}

    public AsistenciaResponsive(String matricula, String nombre, Boolean asistio) {
        this.matricula = matricula;
        this.nombre = nombre;
        this.asistio = asistio;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
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
