CREATE TABLE IF NOT EXISTS calendario.solicitacoes_certidao_sacramental (
    id UUID PRIMARY KEY,
    fiel_id UUID NOT NULL REFERENCES calendario.fieis(id),
    tipo VARCHAR(32) NOT NULL,
    status VARCHAR(16) NOT NULL,
    solicitado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    decidido_em_utc TIMESTAMP WITH TIME ZONE NULL,
    decidido_por_usuario_id UUID NULL REFERENCES calendario.usuarios(id),
    observacao_decisao VARCHAR(500) NULL
);
CREATE INDEX IF NOT EXISTS idx_solicitacao_certidao_fiel ON calendario.solicitacoes_certidao_sacramental (fiel_id, solicitado_em_utc DESC);
CREATE INDEX IF NOT EXISTS idx_solicitacao_certidao_status ON calendario.solicitacoes_certidao_sacramental (status, solicitado_em_utc);
