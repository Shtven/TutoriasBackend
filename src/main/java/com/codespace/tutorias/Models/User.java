package com.codespace.tutorias.Models;

import jakarta.persistence.Id;

import java.time.LocalDateTime;

public class User {
    @Id
    private String matricula;

    private String nombre;
    private String apellidoP;
    private String apellidoM;
    private String correo;
    private String pwd;
    private int rol;

    public User(){ }

    public User(String matricula, String nombre, String correo, String pwd, int rol) {
        this.matricula = matricula;
        this.nombre = nombre;
        this.correo = correo;
        this.pwd = pwd;
        this.rol = rol;
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

    public String getApellidoP() {
        return apellidoP;
    }

    public void setApellidoP(String apellidoP) {
        this.apellidoP = apellidoP;
    }

    public String getApellidoM() {
        return apellidoM;
    }

    public void setApellidoM(String apellidoM) {
        this.apellidoM = apellidoM;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public int getRol() {
        return rol;
    }

    public void setRol(int rol) {
        this.rol = rol;
    }
}
