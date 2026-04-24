package com.codespace.tutorias.Models;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idRol;

    private String rol;

    @OneToMany(mappedBy = "rol")
    private List<Permisos> permisos;

    public Rol() {}

    public Rol(int idRol, String rol) {
        this.idRol = idRol;
        this.rol = rol;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public List<Permisos> getPermisos() {
        return permisos;
    }

    public void setPermisos(List<Permisos> permisos) {
        this.permisos = permisos;
    }
}