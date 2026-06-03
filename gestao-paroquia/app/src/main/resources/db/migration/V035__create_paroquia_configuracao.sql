CREATE TABLE IF NOT EXISTS calendario.paroquia_configuracao (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    diocese VARCHAR(255) NOT NULL,
    endereco VARCHAR(500) NOT NULL,
    logo_bytes BYTEA,
    cor_primaria_hex VARCHAR(7) NOT NULL,
    cor_secundaria_hex VARCHAR(7) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);
