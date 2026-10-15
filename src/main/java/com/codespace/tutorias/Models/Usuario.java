package com.codespace.tutorias.Models;

import jakarta.persistence.*;

@Entity
public class Usuario {

    /** Con prefijo S: "S23014587". Los admins pueden tener otro formato (asignados por BD). */
    @Id
    private String matricula;

    private String nombre;
    private String apellidos;
    private String correo;

    @ManyToOne
    @JoinColumn(name = "id_rol")
    private Rol rol;

    /**
     * LOCAL = entra con contraseña. MICROSOFT | GOOGLE = cuenta del login
     * anterior. Null = pre-registro (el signup lo reclama).
     */
    private String proveedor;

    /** Claim 'sub' del proveedor (solo MICROSOFT | GOOGLE). */
    private String idExterno;

    /** BCrypt. Nunca sale en una respuesta ni en un log. */
    private String pwdHash;

    public Usuario() {}

    public String nombreCompleto() {
        return nombre + " " + apellidos;
    }

    /** Sin contraseña todavía: pre-registro o cuenta de un proveedor externo. */
    public boolean esReclamable() {
        return pwdHash == null;
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

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public String getIdExterno() {
        return idExterno;
    }

    public void setIdExterno(String idExterno) {
        this.idExterno = idExterno;
    }

    public String getPwdHash() {
        return pwdHash;
    }

    public void setPwdHash(String pwdHash) {
        this.pwdHash = pwdHash;
    }
}
