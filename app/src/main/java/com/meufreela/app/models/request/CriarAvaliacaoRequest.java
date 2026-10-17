package com.meufreela.app.models.request;

public class CriarAvaliacaoRequest {
    private final String solicitacaoId;
    private final int nota;
    private final String comentario;

    public CriarAvaliacaoRequest(String solicitacaoId, int nota, String comentario) {
        this.solicitacaoId = solicitacaoId;
        this.nota = nota;
        this.comentario = comentario;
    }
}