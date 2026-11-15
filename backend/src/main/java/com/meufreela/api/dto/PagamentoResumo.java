package com.meufreela.api.dto;

import com.meufreela.api.entity.Pagamento;
import com.meufreela.api.entity.StatusPagamento;

import java.time.LocalDateTime;

public record PagamentoResumo(
        String id,
        Double valor,
        StatusPagamento status,
        String gatewayTransactionId,
        LocalDateTime criadoEm,
        LocalDateTime liberadoEm
) {
    public static PagamentoResumo from(Pagamento p) {
        return new PagamentoResumo(
                p.getId(),
                p.getValor(),
                p.getStatus(),
                p.getGatewayTransactionId(),
                p.getCriadoEm(),
                p.getLiberadoEm()
        );
    }
}