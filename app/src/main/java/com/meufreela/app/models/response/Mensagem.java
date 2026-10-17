package com.meufreela.app.models.response;

public class Mensagem {
    private String id;
    private String solicitacaoId;
    private String remetenteId;
    private String remetenteNome;
    private String conteudo;
    private String enviadaEm;

    public String getId() { return id; }
    public String getSolicitacaoId() { return solicitacaoId; }
    public String getRemetenteId() { return remetenteId; }
    public String getRemetenteNome() { return remetenteNome; }
    public String getConteudo() { return conteudo; }
    public String getEnviadaEm() { return enviadaEm; }
}