package com.meufreela.api.entity;

public enum StatusSolicitacao {
    PENDENTE,       // aguardando resposta do freelancer
    ACEITA,         // freelancer aceitou
    RECUSADA,       // freelancer recusou
    EM_ANDAMENTO,   // serviço começou
    CONCLUIDA,      // serviço finalizado (aguarda pagamento)
    CANCELADA,      // cliente cancelou
    PAGA            // pagamento liberado (escrow liberado)
}