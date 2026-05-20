CREATE SCHEMA IF NOT EXISTS calendario;

CREATE TABLE IF NOT EXISTS calendario.organizacoes (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL
);

-- Foundational organizations
INSERT INTO calendario.organizacoes (id, nome)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'Paróquia Nossa Senhora de Fátima'),
    ('00000000-0000-0000-0000-0000000000aa', 'Pastoral da Comunicação'),
    ('00000000-0000-0000-0000-0000000000bb', 'Pastoral da Juventude'),
    ('00000000-0000-0000-0000-0000000000cc', 'Conselho Pastoral Paroquial'),
    ('00000000-0000-0000-0000-0000000000dd', 'Clero Paroquial');

CREATE TABLE IF NOT EXISTS calendario.locais (
    id UUID PRIMARY KEY,
    nome VARCHAR(160) NOT NULL,
    tipo VARCHAR(32) NOT NULL,
    capacidade INTEGER,
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS calendario.categorias (
    id UUID PRIMARY KEY,
    nome VARCHAR(160) NOT NULL,
    descricao VARCHAR(2000),
    ativo BOOLEAN NOT NULL DEFAULT TRUE
);

