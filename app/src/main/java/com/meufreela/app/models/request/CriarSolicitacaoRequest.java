package com.meufreela.app.models.request;

public class CriarSolicitacaoRequest {
    private final String freelancerId;
    private final String servicoId;
    private final String descricao;
    private final String endereco;
    private final String dataDesejada;
    private final int duracaoHoras;

    public CriarSolicitacaoRequest(String freelancerId, String servicoId,
                                   String descricao, String endereco,
                                   String dataDesejada, int duracaoHoras) {
        this.freelancerId = freelancerId;
        this.servicoId = servicoId;
        this.descricao = descricao;
        this.endereco = endereco;
        this.dataDesejada = dataDesejada;
        this.duracaoHoras = duracaoHoras;
    }
}