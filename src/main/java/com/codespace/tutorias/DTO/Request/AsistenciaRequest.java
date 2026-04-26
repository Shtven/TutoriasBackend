package com.codespace.tutorias.DTO.Request;

public class AsistenciaRequest {
    private int idTutoria;

    public AsistenciaRequest() {}

    public AsistenciaRequest(int idTutoria) {
        this.idTutoria = idTutoria;
    }

    public int getIdTutoria() {
        return idTutoria;
    }
}
