package com.meufreela.api.dto;

import com.meufreela.api.entity.CategoriaServico;
import com.meufreela.api.entity.Servico;
import com.meufreela.api.entity.Usuario;

import java.time.LocalDateTime;
import java.util.List;

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
        List<String> fotos,
        LocalDateTime criadoEm
) {
    public static ServicoResumo from(Servico s) {
        Usuario f = s.getFreelancer();
        String fotoFreelancer = f.getFotoUrl() != null ? f.getFotoUrl() : "";
        List<String> fotos = s.getFotos() != null ? s.getFotos() : List.of();

        return new ServicoResumo(
                s.getId(),
                f.getId(),
                f.getNome(),
                fotoFreelancer,
                f.isVerificado(),
                f.getAvaliacaoMedia(),
                s.getTitulo(),
                s.getDescricao(),
                s.getCategoria(),
                s.getPreco(),
                s.isAtivo(),
                fotos,
                s.getCriadoEm()
        );
    }
}