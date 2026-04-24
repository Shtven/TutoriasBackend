package com.codespace.tutorias.Models;

import jakarta.persistence.*;

@Entity
public class Permisos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idPermiso;
    private boolean crudTutoria;
    private boolean inscribirTutoria;
    private boolean verTutoria;
    @ManyToOne
    @JoinColumn(name= "id_Rol")
    private Rol rol;

    public Permisos() {}

    public Permisos(int idPermiso, boolean crudTutoria, boolean inscribirTutoria, boolean verTutoria, Rol rol) {
        this.idPermiso = idPermiso;
        this.crudTutoria = crudTutoria;
        this.inscribirTutoria = inscribirTutoria;
        this.verTutoria = verTutoria;
        this.rol = rol;
    }

    public int getIdPermiso() {
        return idPermiso;
    }

    public void setIdPermiso(int idPermiso) {
        this.idPermiso = idPermiso;
    }

    public boolean isCrudTutoria() {
        return crudTutoria;
    }

    public void setCrudTutoria(boolean crudTutoria) {
        this.crudTutoria = crudTutoria;
    }

    public boolean isInscribirTutoria() {
        return inscribirTutoria;
    }

    public void setInscribirTutoria(boolean inscribirTutoria) {
        this.inscribirTutoria = inscribirTutoria;
    }

    public boolean isVerTutoria() {
        return verTutoria;
    }

    public void setVerTutoria(boolean verTutoria) {
        this.verTutoria = verTutoria;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

}
