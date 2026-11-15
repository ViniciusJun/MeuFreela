-- Tabela de Pagamentos (Escrow)
CREATE TABLE pagamentos (
    id VARCHAR(255) PRIMARY KEY,
    solicitacao_id VARCHAR(255) NOT NULL UNIQUE,
    valor DOUBLE PRECISION NOT NULL,
    status VARCHAR(20) NOT NULL,
    gateway_transaction_id VARCHAR(100),
    criado_em TIMESTAMP NOT NULL,
    liberado_em TIMESTAMP,
    CONSTRAINT fk_pagamentos_solicitacao FOREIGN KEY (solicitacao_id) REFERENCES solicitacoes(id) ON DELETE CASCADE
);

-- Índice para buscas rápidas
CREATE INDEX idx_pagamentos_solicitacao ON pagamentos(solicitacao_id);