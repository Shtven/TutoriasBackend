package com.codespace.tutorias.DTO.Request;

public class CalificacionRequest {

    private int idAsistencia;
    private int calificacion;

    public CalificacionRequest() {}

    public CalificacionRequest(int idAsistencia, int calificacion) {
        this.idAsistencia = idAsistencia;
        this.calificacion = calificacion;
    }

    public int getIdAsistencia() {
        return idAsistencia;
    }

    public void setIdAsistencia(int idAsistencia) {
        this.idAsistencia = idAsistencia;
    }

    public int getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(int calificacion) {
        this.calificacion = calificacion;
    }
}
