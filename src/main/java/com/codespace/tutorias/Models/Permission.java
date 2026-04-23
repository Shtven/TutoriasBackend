package com.codespace.tutorias.Models;

public class Permission {

    private int idPermission;
    private boolean crudTutorias;
    private boolean inscribirTutorias;
    private boolean verTutorias;
    private int idRol;

    public Permission() {}

    public Permission(int idPermission, boolean crudTutorias, boolean inscribirTutorias, boolean verTutorias, int idRol) {
        this.idPermission = idPermission;
        this.crudTutorias = crudTutorias;
        this.inscribirTutorias = inscribirTutorias;
        this.verTutorias = verTutorias;
        this.idRol = idRol;
    }

    public int getIdPermission() {
        return idPermission;
    }

    public void setIdPermission(int idPermission) {
        this.idPermission = idPermission;
    }

    public boolean isCrudTutorias() {
        return crudTutorias;
    }

    public void setCrudTutorias(boolean crudTutorias) {
        this.crudTutorias = crudTutorias;
    }

    public boolean isInscribirTutorias() {
        return inscribirTutorias;
    }

    public void setInscribirTutorias(boolean inscribirTutorias) {
        this.inscribirTutorias = inscribirTutorias;
    }

    public boolean isVerTutorias() {
        return verTutorias;
    }

    public void setVerTutorias(boolean verTutorias) {
        this.verTutorias = verTutorias;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

}
