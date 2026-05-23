package com.codespace.tutorias.DTO.Request;

public class TemaRequest {

    private int idTutoria;
    private String tema;

    public TemaRequest() {}

    public TemaRequest(int idTutoria, String tema) {
        this.idTutoria = idTutoria;
        this.tema = tema;
    }

    public int getIdTutoria() {
        return idTutoria;
    }

    public void setIdTutoria(int idTutoria) {
        this.idTutoria = idTutoria;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }
}
