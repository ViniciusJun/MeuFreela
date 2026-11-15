package com.meufreela.app.models.response;

public class SolicitacaoKycResponse {
    private String status; // NAO_ENVIADO, EM_ANALISE, APROVADO, REJEITADO
    private String dataEnvio;
    private String motivoRejeicao;

    public String getStatus() { return status; }
    public String getDataEnvio() { return dataEnvio; }
    public String getMotivoRejeicao() { return motivoRejeicao; }
}