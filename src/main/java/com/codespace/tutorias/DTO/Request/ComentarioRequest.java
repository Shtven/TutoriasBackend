package com.codespace.tutorias.DTO.Request;

public class ComentarioRequest {

    private int idTutoria;
    private String comentario;

    public ComentarioRequest() {}

    public ComentarioRequest(int idTutoria, String comentario) {
        this.idTutoria = idTutoria;
        this.comentario = comentario;
    }

    public int getIdTutoria() {
        return idTutoria;
    }

    public void setIdTutoria(int idTutoria) {
        this.idTutoria = idTutoria;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
