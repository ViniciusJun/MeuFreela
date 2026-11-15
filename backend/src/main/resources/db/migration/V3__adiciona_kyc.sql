-- Adiciona o status KYC no usuário
ALTER TABLE usuarios ADD COLUMN status_kyc VARCHAR(20) NOT NULL DEFAULT 'NAO_ENVIADO';

-- Cria a tabela de histórico de envios
CREATE TABLE solicitacoes_kyc (
    id VARCHAR(255) PRIMARY KEY,
    usuario_id VARCHAR(255) NOT NULL,
    documento_url VARCHAR(500) NOT NULL,
    selfie_url VARCHAR(500) NOT NULL,
    status VARCHAR(20) NOT NULL,
    motivo_rejeicao VARCHAR(500),
    data_envio TIMESTAMP NOT NULL,
    data_analise TIMESTAMP,
    CONSTRAINT fk_solicitacoes_kyc_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);