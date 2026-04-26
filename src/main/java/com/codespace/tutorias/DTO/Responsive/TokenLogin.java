package com.codespace.tutorias.DTO.Responsive;

public class TokenLogin {
    private String token;
    private String rol;

    public TokenLogin() {
    }

    public TokenLogin(String token, String rol) {
        this.token = token;
        this.rol = rol;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
