package com.meufreela.api.dto;

import com.meufreela.api.entity.Solicitacao;

import java.time.LocalDateTime;

public record SolicitacaoResumo(
        String id,
        String clienteId,
        String clienteNome,
        String freelancerId,
        String freelancerNome,
        String descricao,
        String endereco,
        LocalDateTime dataDesejada,
        Integer duracaoHoras,
        Double valorTotal,
        String status,
        LocalDateTime criadoEm
) {
    public static SolicitacaoResumo from(Solicitacao s) {
        return new SolicitacaoResumo(
                s.getId(),
                s.getCliente().getId(),
                s.getCliente().getNome(),
                s.getFreelancer().getId(),
                s.getFreelancer().getNome(),
                s.getDescricao(),
                s.getEndereco(),
                s.getDataDesejada(),
                s.getDuracaoHoras(),
                s.getValorTotal(),
                s.getStatus().name(),
                s.getCriadoEm()
        );
    }
}