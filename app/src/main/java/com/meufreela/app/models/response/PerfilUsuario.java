package com.meufreela.app.models.response;

import java.util.List;

public class PerfilUsuario {
    private String id;
    private String nome;
    private String email;
    private String tipo;
    private String fotoUrl;
    private boolean verificado;
    private List<String> papeis;

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }
    public String getTipo() { return tipo; }
    public boolean isVerificado() { return verificado; }
    public List<String> getPapeis() { return papeis; }
    public String getFotoUrl() { return fotoUrl; }
    public boolean temPapel(String papel) {
        return papeis != null && papeis.contains(papel);
    }
}