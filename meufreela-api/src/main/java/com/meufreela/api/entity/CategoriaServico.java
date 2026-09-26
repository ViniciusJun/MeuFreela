package com.meufreela.api.entity;

public enum CategoriaServico {
    REFORMAS_REPAROS("Reformas e Reparos"),
    SERVICOS_DOMESTICOS("Serviços Domésticos"),
    TECNOLOGIA("Tecnologia"),
    EVENTOS("Eventos"),
    BELEZA("Beleza");

    private final String descricao;

    CategoriaServico(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() { return descricao; }
}