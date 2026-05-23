package com.codespace.tutorias.DTO.Responsive;

public class ComentarioResponsive {

    private int idComentario;
    private int idTutoria;
    private String matricula;
    private String nombre;
    private String comentario;

    public ComentarioResponsive() {}

    public ComentarioResponsive(int idComentario, int idTutoria, String matricula, String nombre, String comentario) {
        this.idComentario = idComentario;
        this.idTutoria = idTutoria;
        this.matricula = matricula;
        this.nombre = nombre;
        this.comentario = comentario;
    }

    public int getIdComentario() {
        return idComentario;
    }

    public void setIdComentario(int idComentario) {
        this.idComentario = idComentario;
    }

    public int getIdTutoria() {
        return idTutoria;
    }

    public void setIdTutoria(int idTutoria) {
        this.idTutoria = idTutoria;
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

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
