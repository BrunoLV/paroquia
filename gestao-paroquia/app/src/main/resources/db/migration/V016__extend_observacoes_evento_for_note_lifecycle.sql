ALTER TABLE calendario.observacoes_evento
    ADD COLUMN IF NOT EXISTS removida BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE calendario.observacoes_evento
    ADD COLUMN IF NOT EXISTS removida_em_utc TIMESTAMP WITH TIME ZONE;

ALTER TABLE calendario.observacoes_evento
    ADD COLUMN IF NOT EXISTS removida_por_usuario_id UUID;

CREATE TABLE IF NOT EXISTS calendario.observacoes_nota_revisoes (
    id UUID PRIMARY KEY,
    observacao_id UUID NOT NULL REFERENCES calendario.observacoes_evento(id) ON DELETE CASCADE,
    conteudo_anterior VARCHAR(4000) NOT NULL,
    conteudo_novo VARCHAR(4000) NOT NULL,
    revisado_por_usuario_id UUID NOT NULL,
    revisado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_observacoes_evento_evento_removida_criado
    ON calendario.observacoes_evento (evento_id, removida, criado_em_utc, id);

CREATE INDEX IF NOT EXISTS idx_observacoes_evento_evento_usuario_removida
    ON calendario.observacoes_evento (evento_id, usuario_id, removida, criado_em_utc, id);

CREATE INDEX IF NOT EXISTS idx_observacoes_nota_revisoes_observacao
    ON calendario.observacoes_nota_revisoes (observacao_id, revisado_em_utc, id);
