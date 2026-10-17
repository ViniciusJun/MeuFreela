package com.meufreela.api.dto;

import com.meufreela.api.entity.Avaliacao;

import java.time.LocalDateTime;

public record AvaliacaoResumo(
        String id,
        String autorId,
        String autorNome,
        String avaliadoId,
        String avaliadoNome,
        Integer nota,
        String comentario,
        LocalDateTime criadoEm
) {
    public static AvaliacaoResumo from(Avaliacao a) {
        return new AvaliacaoResumo(
                a.getId(),
                a.getAutor().getId(),
                a.getAutor().getNome(),
                a.getAvaliado().getId(),
                a.getAvaliado().getNome(),
                a.getNota(),
                a.getComentario(),
                a.getCriadoEm()
        );
    }
}