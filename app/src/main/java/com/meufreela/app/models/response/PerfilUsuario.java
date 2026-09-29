package com.meufreela.app.models.response;

public class PerfilUsuario {
    private String id;
    private String nome;
    private String email;
    private String tipo;
    private boolean verificado;

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getTipo() { return tipo; }
    public boolean isVerificado() { return verificado; }
}