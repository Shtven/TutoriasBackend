package com.codespace.tutorias.DTO.Responsive;

public class TemaResponsive {

    private int idTema;
    private String tema;

    public TemaResponsive() {}

    public TemaResponsive(int idTema, String tema) {
        this.idTema = idTema;
        this.tema = tema;
    }

    public int getIdTema() {
        return idTema;
    }

    public void setIdTema(int idTema) {
        this.idTema = idTema;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }
}
