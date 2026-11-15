package com.meufreela.api.entity;

public enum StatusPagamento {
    PENDENTE,   // Aguardando ação do cliente
    RETIDO,     // Pago pelo cliente, em custódia da plataforma (Escrow)
    LIBERADO,   // Liberado para o freelancer após aprovação
    CANCELADO   // Estornado ou cancelado
}