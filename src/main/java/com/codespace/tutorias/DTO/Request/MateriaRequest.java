package com.codespace.tutorias.DTO.Request;

public class MateriaRequest {

    private String nombre;
    private int nrc;

    public MateriaRequest() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getNrc() {
        return nrc;
    }

    public void setNrc(int nrc) {
        this.nrc = nrc;
    }
}
