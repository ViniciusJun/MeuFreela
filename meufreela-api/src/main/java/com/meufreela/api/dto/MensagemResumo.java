package com.meufreela.api.dto;

import com.meufreela.api.entity.Mensagem;

import java.time.LocalDateTime;

public record MensagemResumo(
        String id,
        String solicitacaoId,
        String remetenteId,
        String remetenteNome,
        String conteudo,
        LocalDateTime enviadaEm
) {
    public static MensagemResumo from(Mensagem m) {
        return new MensagemResumo(
                m.getId(),
                m.getSolicitacao().getId(),
                m.getRemetente().getId(),
                m.getRemetente().getNome(),
                m.getConteudo(),
                m.getEnviadaEm()
        );
    }
}