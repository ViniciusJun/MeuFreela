-- ============================================================
-- V2 — Adiciona tabela de fotos para os serviços
-- ============================================================

CREATE TABLE servico_fotos (
    servico_id  VARCHAR(255)    NOT NULL,
    foto_url    VARCHAR(500)    NOT NULL,
    CONSTRAINT fk_servico_fotos_servico
        FOREIGN KEY (servico_id) REFERENCES servicos(id) ON DELETE CASCADE
);

-- Índice para melhorar a performance de busca de fotos por serviço
CREATE INDEX idx_servico_fotos_servico ON servico_fotos(servico_id);