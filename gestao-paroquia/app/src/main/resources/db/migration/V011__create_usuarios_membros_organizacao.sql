CREATE SCHEMA IF NOT EXISTS calendario;

CREATE TABLE IF NOT EXISTS calendario.usuarios (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS calendario.membros_organizacao (
    id UUID PRIMARY KEY,
    usuario_id UUID NOT NULL,
    organizacao_id UUID NOT NULL,
    tipo_organizacao VARCHAR(32) NOT NULL,
    papel VARCHAR(64) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_membros_organizacao_usuario
        FOREIGN KEY (usuario_id) REFERENCES calendario.usuarios (id)
        ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_membros_organizacao_usuario_ativo
    ON calendario.membros_organizacao (usuario_id, ativo);
