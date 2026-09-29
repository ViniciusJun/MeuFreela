package com.meufreela.app.models.request;

public class CriarSolicitacaoRequest {
    private final String freelancerId;
    private final String descricao;
    private final String endereco;
    private final String dataDesejada; // ISO: "2026-10-15T14:00:00"
    private final int duracaoHoras;

    public CriarSolicitacaoRequest(String freelancerId, String descricao,
                                   String endereco, String dataDesejada, int duracaoHoras) {
        this.freelancerId = freelancerId;
        this.descricao = descricao;
        this.endereco = endereco;
        this.dataDesejada = dataDesejada;
        this.duracaoHoras = duracaoHoras;
    }
}