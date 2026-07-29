CREATE TABLE IF NOT EXISTS calendario.cpc_decisoes (
    id UUID PRIMARY KEY,
    titulo VARCHAR(160) NOT NULL,
    pauta_descricao VARCHAR(4000) NOT NULL,
    data_reuniao DATE NOT NULL,
    comunidade_id UUID NOT NULL,
    conselho_tipo VARCHAR(32) NOT NULL DEFAULT 'CPC',
    status_homologacao VARCHAR(32) NOT NULL DEFAULT 'PENDENTE_HOMOLOGACAO',
    solicitante_id VARCHAR(128) NOT NULL,
    solicitante_nome VARCHAR(160),
    homologador_id VARCHAR(128),
    motivo_veto VARCHAR(2000),
    criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    decidido_em_utc TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_cpc_decisoes_comunidade ON calendario.cpc_decisoes(comunidade_id);
CREATE INDEX IF NOT EXISTS idx_cpc_decisoes_status ON calendario.cpc_decisoes(status_homologacao);
