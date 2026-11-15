-- ============================================================
-- V1 — Schema inicial do MeuFreela
-- ============================================================
-- Este arquivo documenta o estado inicial do banco.
-- Em bancos NOVOS, Flyway executa esta migration do zero.
-- Em bancos EXISTENTES (populados por Hibernate antes do Flyway),
--   "baseline-on-migrate=true" marca a V1 como já aplicada —
--   o SQL NÃO é reexecutado, só registrado no histórico.
-- ============================================================

-- ----- USUARIOS -----
CREATE TABLE usuarios (
    id                  VARCHAR(255)    PRIMARY KEY,
    fcm_token           VARCHAR(500),
    nome                VARCHAR(255)    NOT NULL,
    email               VARCHAR(255)    NOT NULL UNIQUE,
    senha               VARCHAR(255)    NOT NULL,
    cpf                 VARCHAR(11)     NOT NULL,
    telefone            VARCHAR(11)     NOT NULL,
    tipo                VARCHAR(20)     NOT NULL,
    verificado          BOOLEAN         NOT NULL DEFAULT FALSE,
    categoria           VARCHAR(30),
    descricao           VARCHAR(500),
    preco_hora          DOUBLE PRECISION,
    avaliacao_media     DOUBLE PRECISION DEFAULT 0.0,
    total_servicos      INTEGER         DEFAULT 0,
    latitude            DOUBLE PRECISION,
    longitude           DOUBLE PRECISION,
    criado_em           TIMESTAMP       NOT NULL
);

-- ----- USUARIO_PAPEIS (Set<TipoUsuario>) -----
CREATE TABLE usuario_papeis (
    usuario_id          VARCHAR(255)    NOT NULL,
    papel               VARCHAR(20)     NOT NULL,
    CONSTRAINT fk_usuario_papeis_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT pk_usuario_papeis
        PRIMARY KEY (usuario_id, papel)
);

-- ----- SERVICOS -----
CREATE TABLE servicos (
    id                  VARCHAR(255)    PRIMARY KEY,
    freelancer_id       VARCHAR(255)    NOT NULL,
    titulo              VARCHAR(120)    NOT NULL,
    descricao           VARCHAR(1000)   NOT NULL,
    categoria           VARCHAR(30)     NOT NULL,
    preco               DOUBLE PRECISION NOT NULL,
    ativo               BOOLEAN         NOT NULL DEFAULT TRUE,
    criado_em           TIMESTAMP       NOT NULL,
    CONSTRAINT fk_servicos_freelancer
        FOREIGN KEY (freelancer_id) REFERENCES usuarios(id)
);

-- ----- SOLICITACOES -----
CREATE TABLE solicitacoes (
    id                  VARCHAR(255)    PRIMARY KEY,
    cliente_id          VARCHAR(255)    NOT NULL,
    freelancer_id       VARCHAR(255)    NOT NULL,
    descricao           VARCHAR(1000)   NOT NULL,
    endereco            VARCHAR(300)    NOT NULL,
    data_desejada       TIMESTAMP       NOT NULL,
    duracao_horas       INTEGER         NOT NULL,
    valor_total         DOUBLE PRECISION,
    status              VARCHAR(20)     NOT NULL,
    criado_em           TIMESTAMP       NOT NULL,
    atualizado_em       TIMESTAMP,
    CONSTRAINT fk_solicitacoes_cliente
        FOREIGN KEY (cliente_id) REFERENCES usuarios(id),
    CONSTRAINT fk_solicitacoes_freelancer
        FOREIGN KEY (freelancer_id) REFERENCES usuarios(id)
);

-- ----- AVALIACOES -----
CREATE TABLE avaliacoes (
    id                  VARCHAR(255)    PRIMARY KEY,
    solicitacao_id      VARCHAR(255)    NOT NULL,
    autor_id            VARCHAR(255)    NOT NULL,
    avaliado_id         VARCHAR(255)    NOT NULL,
    nota                INTEGER         NOT NULL,
    comentario          VARCHAR(2000),
    criado_em           TIMESTAMP       NOT NULL,
    CONSTRAINT fk_avaliacoes_solicitacao
        FOREIGN KEY (solicitacao_id) REFERENCES solicitacoes(id),
    CONSTRAINT fk_avaliacoes_autor
        FOREIGN KEY (autor_id) REFERENCES usuarios(id),
    CONSTRAINT fk_avaliacoes_avaliado
        FOREIGN KEY (avaliado_id) REFERENCES usuarios(id)
);

-- ----- MENSAGENS -----
CREATE TABLE mensagens (
    id                  VARCHAR(255)    PRIMARY KEY,
    solicitacao_id      VARCHAR(255)    NOT NULL,
    remetente_id        VARCHAR(255)    NOT NULL,
    conteudo            VARCHAR(2000)   NOT NULL,
    enviada_em          TIMESTAMP       NOT NULL,
    CONSTRAINT fk_mensagens_solicitacao
        FOREIGN KEY (solicitacao_id) REFERENCES solicitacoes(id),
    CONSTRAINT fk_mensagens_remetente
        FOREIGN KEY (remetente_id) REFERENCES usuarios(id)
);

-- ----- INDICES (performance) -----
-- Email e CPF já têm unique constraint implícita
-- (criados pela UNIQUE em usuarios).
-- Índices para queries frequentes:
CREATE INDEX idx_servicos_freelancer      ON servicos(freelancer_id);
CREATE INDEX idx_servicos_categoria_ativo ON servicos(categoria, ativo);
CREATE INDEX idx_solicitacoes_cliente     ON solicitacoes(cliente_id, criado_em DESC);
CREATE INDEX idx_solicitacoes_freelancer  ON solicitacoes(freelancer_id, criado_em DESC);
CREATE INDEX idx_mensagens_solicitacao    ON mensagens(solicitacao_id, enviada_em ASC);
CREATE INDEX idx_avaliacoes_avaliado      ON avaliacoes(avaliado_id);