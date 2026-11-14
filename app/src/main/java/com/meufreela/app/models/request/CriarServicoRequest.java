package com.meufreela.app.models.request;

public class CriarServicoRequest {
    private final String titulo;
    private final String descricao;
    private final String categoria;
    private final double preco;

    public CriarServicoRequest(String titulo, String descricao, String categoria, double preco) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.categoria = categoria;
        this.preco = preco;
    }
}