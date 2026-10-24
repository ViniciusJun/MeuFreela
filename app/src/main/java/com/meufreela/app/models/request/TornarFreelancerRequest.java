package com.meufreela.app.models.request;

public class TornarFreelancerRequest {
    private final String categoria;
    private final String descricao;
    private final double precoHora;

    public TornarFreelancerRequest(String categoria, String descricao, double precoHora) {
        this.categoria = categoria;
        this.descricao = descricao;
        this.precoHora = precoHora;
    }
}