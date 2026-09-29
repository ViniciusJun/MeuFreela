package com.meufreela.app.models.response;

import com.google.gson.annotations.SerializedName;

public class Solicitacao {
    private String id;
    private String clienteId;
    private String clienteNome;
    private String freelancerId;
    private String freelancerNome;
    private String descricao;
    private String endereco;

    @SerializedName("dataDesejada")
    private String dataDesejada;

    private Integer duracaoHoras;
    private Double valorTotal;
    private String status;
    private String criadoEm;

    // getters
    public String getId() { return id; }
    public String getClienteId() { return clienteId; }
    public String getClienteNome() { return clienteNome; }
    public String getFreelancerId() { return freelancerId; }
    public String getFreelancerNome() { return freelancerNome; }
    public String getDescricao() { return descricao; }
    public String getEndereco() { return endereco; }
    public String getDataDesejada() { return dataDesejada; }
    public Integer getDuracaoHoras() { return duracaoHoras; }
    public Double getValorTotal() { return valorTotal; }
    public String getStatus() { return status; }
    public String getCriadoEm() { return criadoEm; }
}