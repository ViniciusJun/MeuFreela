package com.meufreela.api.dto;

import com.meufreela.api.entity.CategoriaServico;
import com.meufreela.api.entity.Servico;

import java.time.LocalDateTime;

public record ServicoResumo(
        String id,
        String freelancerId,
        String freelancerNome,
        boolean freelancerVerificado,
        Double freelancerAvaliacao,
        String titulo,
        String descricao,
        CategoriaServico categoria,
        Double preco,
        boolean ativo,
        LocalDateTime criadoEm
) {
    public static ServicoResumo from(Servico s) {
        return new ServicoResumo(
                s.getId(),
                s.getFreelancer().getId(),
                s.getFreelancer().getNome(),
                s.getFreelancer().isVerificado(),
                s.getFreelancer().getAvaliacaoMedia(),
                s.getTitulo(),
                s.getDescricao(),
                s.getCategoria(),
                s.getPreco(),
                s.isAtivo(),
                s.getCriadoEm()
        );
    }
}