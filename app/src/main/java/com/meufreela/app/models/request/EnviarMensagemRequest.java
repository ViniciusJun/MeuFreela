package com.meufreela.app.models.request;

public class EnviarMensagemRequest {
    private final String conteudo;

    public EnviarMensagemRequest(String conteudo) {
        this.conteudo = conteudo;
    }
    public String getConteudo() { return conteudo; }
}