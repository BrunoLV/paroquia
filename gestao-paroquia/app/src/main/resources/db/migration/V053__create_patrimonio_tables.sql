CREATE TABLE IF NOT EXISTS calendario.ativos_patrimoniais (
    id UUID PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    numero_tombamento VARCHAR(32) NOT NULL,
    categoria VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'DISPONIVEL',
    reservavel BOOLEAN NOT NULL DEFAULT FALSE,
    organizacao_id UUID NOT NULL,
    data_aquisicao DATE,
    valor_estimado NUMERIC(12,2),
    criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_ativos_patrimoniais_numero_tombamento UNIQUE (numero_tombamento)
);

CREATE INDEX IF NOT EXISTS idx_ativos_patrimoniais_organizacao ON calendario.ativos_patrimoniais(organizacao_id);
CREATE INDEX IF NOT EXISTS idx_ativos_patrimoniais_status ON calendario.ativos_patrimoniais(status);

CREATE TABLE IF NOT EXISTS calendario.observacoes_ativo_patrimonial (
    id UUID PRIMARY KEY,
    ativo_patrimonial_id UUID NOT NULL,
    usuario_id UUID NOT NULL,
    tipo VARCHAR(64) NOT NULL,
    conteudo VARCHAR(4000) NOT NULL,
    criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    removida BOOLEAN NOT NULL DEFAULT FALSE,
    removida_em_utc TIMESTAMP WITH TIME ZONE,
    removida_por_usuario_id UUID,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_observacoes_ativo_patrimonial_ativo ON calendario.observacoes_ativo_patrimonial(ativo_patrimonial_id);

CREATE TABLE IF NOT EXISTS calendario.patrimonio_numeracao_anual (
    ano INTEGER PRIMARY KEY,
    ultimo_numero INTEGER NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0
);
