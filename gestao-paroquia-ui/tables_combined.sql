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

CREATE TABLE IF NOT EXISTS calendario.eventos (
    id UUID PRIMARY KEY,
    titulo VARCHAR(160) NOT NULL,
    descricao VARCHAR(4000),
    organizacao_responsavel_id UUID NOT NULL,
    inicio_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    fim_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(32) NOT NULL,
    cancelado_motivo VARCHAR(2000),
    adicionado_extra_justificativa VARCHAR(4000),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_eventos_organizacao
        FOREIGN KEY (organizacao_responsavel_id)
        REFERENCES calendario.organizacoes (id)
);
CREATE TABLE IF NOT EXISTS calendario.projetos_eventos (
    id UUID PRIMARY KEY,
    nome VARCHAR(160) NOT NULL,
    descricao VARCHAR(2000),
    status VARCHAR(32) NOT NULL DEFAULT 'ATIVO',
    version BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE IF NOT EXISTS calendario.eventos_recorrencia (
    id UUID PRIMARY KEY,
    evento_base_id UUID NOT NULL,
    frequencia VARCHAR(32) NOT NULL,
    intervalo INTEGER NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_eventos_recorrencia_evento
        FOREIGN KEY (evento_base_id)
        REFERENCES calendario.eventos (id)
        ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS calendario.observacoes_evento (
    id UUID PRIMARY KEY,
    evento_id UUID NOT NULL,
    usuario_id UUID NOT NULL,
    tipo VARCHAR(64) NOT NULL,
    conteudo VARCHAR(4000) NOT NULL,
    criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);
CREATE TABLE IF NOT EXISTS calendario.aprovacoes (
    id UUID PRIMARY KEY,
    evento_id UUID NOT NULL,
    tipo_solicitacao VARCHAR(64) NOT NULL,
    aprovador_papel VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    decidido_em_utc TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_aprovacoes_evento_id
    ON calendario.aprovacoes (evento_id);

CREATE INDEX IF NOT EXISTS idx_aprovacoes_status
    ON calendario.aprovacoes (status);
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
CREATE TABLE IF NOT EXISTS calendario.auditoria_operacoes (
    id UUID PRIMARY KEY,
    organizacao_id UUID NOT NULL,
    evento_id UUID,
    recurso_tipo VARCHAR(64) NOT NULL,
    recurso_id VARCHAR(255) NOT NULL,
    acao VARCHAR(64) NOT NULL,
    resultado VARCHAR(32) NOT NULL,
    ator VARCHAR(255) NOT NULL,
    ator_usuario_id UUID,
    correlation_id VARCHAR(128) NOT NULL,
    detalhes_auditaveis_json VARCHAR(4000) NOT NULL,
    ocorrido_em_utc TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_auditoria_operacoes_org_periodo
    ON calendario.auditoria_operacoes (organizacao_id, ocorrido_em_utc, id);

CREATE INDEX IF NOT EXISTS idx_auditoria_operacoes_evento_periodo
    ON calendario.auditoria_operacoes (evento_id, ocorrido_em_utc, id);

CREATE INDEX IF NOT EXISTS idx_auditoria_operacoes_correlation
    ON calendario.auditoria_operacoes (correlation_id, ocorrido_em_utc, id);
CREATE TABLE IF NOT EXISTS calendario.anos_paroquiais (
    ano INTEGER PRIMARY KEY,
    status VARCHAR(32) NOT NULL,
    data_fechamento_utc TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Seed current and next year in PLANEJAMENTO status (H2 compatible)
INSERT INTO calendario.anos_paroquiais (ano, status)
SELECT 2026, 'PLANEJAMENTO'
WHERE NOT EXISTS (SELECT 1 FROM calendario.anos_paroquiais WHERE ano = 2026);

INSERT INTO calendario.anos_paroquiais (ano, status)
SELECT 2027, 'PLANEJAMENTO'
WHERE NOT EXISTS (SELECT 1 FROM calendario.anos_paroquiais WHERE ano = 2027);
CREATE TABLE IF NOT EXISTS calendario.membros (
    id UUID PRIMARY KEY,
    nome_completo VARCHAR(255) NOT NULL,
    data_nascimento DATE,
    email VARCHAR(255),
    telefone VARCHAR(20),
    endereco VARCHAR(500),
    usuario_id UUID,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_batismo DATE,
    local_batismo VARCHAR(255),
    data_crisma DATE,
    data_matrimonio DATE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_membros_usuario FOREIGN KEY (usuario_id) REFERENCES calendario.usuarios(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_membros_nome ON calendario.membros (nome_completo);
CREATE INDEX IF NOT EXISTS idx_membros_ativo ON calendario.membros (ativo);
