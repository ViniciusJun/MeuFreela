package com.meufreela.app.models.response;

public class Servico {
    private String id;
    private String freelancerId;
    private String freelancerFotoUrl;
    private String freelancerNome;
    private boolean freelancerVerificado;
    private Double freelancerAvaliacao;
    private String titulo;
    private String descricao;
    private String categoria;
    private Double preco;
    private boolean ativo;
    private String criadoEm;

    public String getId() { return id; }
    public String getFreelancerId() { return freelancerId; }
    public String getFreelancerFotoUrl() { return freelancerFotoUrl; }
    public String getFreelancerNome() { return freelancerNome; }
    public boolean isFreelancerVerificado() { return freelancerVerificado; }
    public Double getFreelancerAvaliacao() { return freelancerAvaliacao; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getCategoria() { return categoria; }
    public Double getPreco() { return preco; }
    public boolean isAtivo() { return ativo; }
    public String getCriadoEm() { return criadoEm; }
}