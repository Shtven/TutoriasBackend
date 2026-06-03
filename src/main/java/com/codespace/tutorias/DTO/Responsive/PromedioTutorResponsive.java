package com.codespace.tutorias.DTO.Responsive;

public class PromedioTutorResponsive {

    private Integer matricula;
    private String nombre;
    private Double promedio;
    private long totalCalificaciones;

    public PromedioTutorResponsive() {}

    public PromedioTutorResponsive(Integer matricula, String nombre, Double promedio, long totalCalificaciones) {
        this.matricula = matricula;
        this.nombre = nombre;
        this.promedio = promedio;
        this.totalCalificaciones = totalCalificaciones;
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

    public Double getPromedio() {
        return promedio;
    }

    public void setPromedio(Double promedio) {
        this.promedio = promedio;
    }

    public long getTotalCalificaciones() {
        return totalCalificaciones;
    }

    public void setTotalCalificaciones(long totalCalificaciones) {
        this.totalCalificaciones = totalCalificaciones;
    }
}
