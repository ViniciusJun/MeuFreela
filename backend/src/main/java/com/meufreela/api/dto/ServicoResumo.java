package com.meufreela.api.dto;

import com.meufreela.api.entity.CategoriaServico;
import com.meufreela.api.entity.Servico;
import com.meufreela.api.entity.Usuario;      // ← import que faltava

import java.time.LocalDateTime;

public record ServicoResumo(
        String id,
        String freelancerId,
        String freelancerNome,
        String freelancerFotoUrl,
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
        Usuario f = s.getFreelancer();     // ← variável declarada
        String foto = f.getFotoUrl() != null ? f.getFotoUrl() : "";

        return new ServicoResumo(
                s.getId(),
                f.getId(),
                f.getNome(),
                foto,                       // ← vírgula
                f.isVerificado(),           // ← trocado de s.getFreelancer() para f
                f.getAvaliacaoMedia(),
                s.getTitulo(),
                s.getDescricao(),
                s.getCategoria(),
                s.getPreco(),
                s.isAtivo(),
                s.getCriadoEm()
        );
    }
}