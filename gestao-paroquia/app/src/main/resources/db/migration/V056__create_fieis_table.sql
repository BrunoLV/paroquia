CREATE TABLE IF NOT EXISTS calendario.fieis (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    cpf VARCHAR(255) NOT NULL,
    cpf_hash CHAR(64) NOT NULL,
    data_nascimento VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    telefone VARCHAR(255) NOT NULL,
    endereco VARCHAR(1000) NOT NULL,
    vocacao VARCHAR(32) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    criado_por VARCHAR(255) NOT NULL,
    atualizado_por VARCHAR(255) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_fieis_cpf_hash UNIQUE (cpf_hash)
);

CREATE INDEX IF NOT EXISTS idx_fieis_ativo ON calendario.fieis (ativo);
