package com.meufreela.app.models.request;

public class LoginRequest {
    private final String email;
    private final String senha;

    public LoginRequest(String email, String senha) {
        this.email = email;
        this.senha = senha;
    }
    // getters...
}
