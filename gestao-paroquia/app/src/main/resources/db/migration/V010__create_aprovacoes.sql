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
