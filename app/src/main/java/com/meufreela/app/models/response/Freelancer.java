package com.meufreela.app.models.response;

import com.google.gson.annotations.SerializedName;

public class Freelancer {
    private String id;
    private String nome;
    private String categoria;
    private String descricao;

    @SerializedName("precoHora")
    private Double precoHora;

    @SerializedName("avaliacaoMedia")
    private Double avaliacaoMedia;

    @SerializedName("totalServicos")
    private Integer totalServicos;

    private boolean verificado;

    // getters
    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getCategoria() { return categoria; }
    public String getDescricao() { return descricao; }
    public Double getPrecoHora() { return precoHora; }
    public Double getAvaliacaoMedia() { return avaliacaoMedia; }
    public Integer getTotalServicos() { return totalServicos; }
    public boolean isVerificado() { return verificado; }
}