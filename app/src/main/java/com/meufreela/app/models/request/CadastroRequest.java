package com.meufreela.app.models.request;

public class CadastroRequest {
    private final String nome;
    private final String email;
    private final String senha;
    private final String telefone;
    private final String cpf;
    private final String tipo; // "CLIENTE" ou "FREELANCER"

    public CadastroRequest(String nome, String email, String senha,
                           String telefone, String cpf, String tipo) {
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.telefone = telefone;
        this.cpf = cpf;
        this.tipo = tipo;
    }
    // getters...
}