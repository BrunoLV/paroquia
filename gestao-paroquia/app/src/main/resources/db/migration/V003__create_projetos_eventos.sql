CREATE TABLE IF NOT EXISTS calendario.projetos_eventos (
    id UUID PRIMARY KEY,
    nome VARCHAR(160) NOT NULL,
    descricao VARCHAR(2000),
    status VARCHAR(32) NOT NULL DEFAULT 'ATIVO',
    version BIGINT NOT NULL DEFAULT 0
);
