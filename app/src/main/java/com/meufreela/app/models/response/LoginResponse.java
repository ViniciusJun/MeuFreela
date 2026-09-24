package com.meufreela.app.models.response;

public class LoginResponse {
    private String token;
    private String refreshToken;
    private Usuario usuario;

    // getters
    public String getToken() { return token; }
    public String getRefreshToken() { return refreshToken; }
    public Usuario getUsuario() { return usuario; }

    public static class Usuario {
        private String id;
        private String nome;
        private String email;
        private String tipo; // "CLIENTE" ou "FREELANCER"
        private boolean verificado;

        // getters...
        public String getId() { return id; }
        public String getNome() { return nome; }
        public String getEmail() { return email; }
        public String getTipo() { return tipo; }
        public boolean isVerificado() { return verificado; }
    }
}