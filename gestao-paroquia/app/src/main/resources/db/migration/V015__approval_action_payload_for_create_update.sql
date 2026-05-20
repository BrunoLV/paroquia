ALTER TABLE calendario.aprovacoes
    ALTER COLUMN evento_id DROP NOT NULL;

CREATE INDEX IF NOT EXISTS idx_aprovacoes_tipo_status_criado
    ON calendario.aprovacoes (tipo_solicitacao, status, criado_em_utc);
