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
