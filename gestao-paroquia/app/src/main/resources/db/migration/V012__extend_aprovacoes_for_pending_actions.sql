ALTER TABLE calendario.aprovacoes
    ADD COLUMN IF NOT EXISTS executado_em_utc TIMESTAMP WITH TIME ZONE;

ALTER TABLE calendario.aprovacoes
    ADD COLUMN IF NOT EXISTS solicitante_id VARCHAR(255);

ALTER TABLE calendario.aprovacoes
    ADD COLUMN IF NOT EXISTS solicitante_papel VARCHAR(64);

ALTER TABLE calendario.aprovacoes
    ADD COLUMN IF NOT EXISTS solicitante_tipo_organizacao VARCHAR(64);

ALTER TABLE calendario.aprovacoes
    ADD COLUMN IF NOT EXISTS aprovador_id VARCHAR(255);

ALTER TABLE calendario.aprovacoes
    ADD COLUMN IF NOT EXISTS motivo_cancelamento_snapshot VARCHAR(2000);

ALTER TABLE calendario.aprovacoes
    ADD COLUMN IF NOT EXISTS decision_observacao VARCHAR(2000);

ALTER TABLE calendario.aprovacoes
    ADD COLUMN IF NOT EXISTS action_payload_json VARCHAR(4000);

ALTER TABLE calendario.aprovacoes
    ADD COLUMN IF NOT EXISTS correlation_id VARCHAR(128);

CREATE INDEX IF NOT EXISTS idx_aprovacoes_evento_tipo_status
    ON calendario.aprovacoes (evento_id, tipo_solicitacao, status);

CREATE INDEX IF NOT EXISTS idx_aprovacoes_correlation_id
    ON calendario.aprovacoes (correlation_id);
