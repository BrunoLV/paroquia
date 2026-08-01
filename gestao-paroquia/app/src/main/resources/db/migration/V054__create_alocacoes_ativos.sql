CREATE TABLE IF NOT EXISTS calendario.alocacoes_ativos (
    id UUID PRIMARY KEY,
    ativo_id UUID NOT NULL,
    tipo VARCHAR(32) NOT NULL,
    evento_id UUID,
    organizacao_origem_id UUID NOT NULL,
    organizacao_destino_id UUID,
    emprestimo BOOLEAN NOT NULL DEFAULT FALSE,
    data_inicio DATE NOT NULL,
    data_fim DATE NOT NULL,
    status VARCHAR(32) NOT NULL,
    solicitante_id UUID,
    justificativa VARCHAR(4000),
    aprovacao_id UUID,
    impactada_por_atraso BOOLEAN NOT NULL DEFAULT FALSE,
    alerta_atraso_emitido_em TIMESTAMP WITH TIME ZONE,
    motivo_cancelamento VARCHAR(4000),
    criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    devolvido_em_utc TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_alocacoes_ativos_ativo FOREIGN KEY (ativo_id) REFERENCES calendario.ativos_patrimoniais(id)
);

CREATE INDEX IF NOT EXISTS idx_alocacoes_ativos_ativo_status ON calendario.alocacoes_ativos(ativo_id, status);
CREATE INDEX IF NOT EXISTS idx_alocacoes_ativos_evento ON calendario.alocacoes_ativos(evento_id);
CREATE INDEX IF NOT EXISTS idx_alocacoes_ativos_aprovacao ON calendario.alocacoes_ativos(aprovacao_id);
