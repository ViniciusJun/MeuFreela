package com.meufreela.api.dto;

import com.meufreela.api.entity.CategoriaServico;
import com.meufreela.api.entity.Usuario;

public record FreelancerResumo(
        String id,
        String nome,
        String fotoUrl,
        CategoriaServico categoria,
        String descricao,
        Double precoHora,
        Double avaliacaoMedia,
        Integer totalServicos,
        boolean verificado
) {
    public static FreelancerResumo from(Usuario u) {
        return new FreelancerResumo(
                u.getId(),
                u.getNome(),
                u.getFotoUrl() != null ? u.getFotoUrl() : "",
                u.getCategoria(),
                u.getDescricao(),
                u.getPrecoHora(),
                u.getAvaliacaoMedia(),
                u.getTotalServicos(),
                u.isVerificado()
        );
    }
}