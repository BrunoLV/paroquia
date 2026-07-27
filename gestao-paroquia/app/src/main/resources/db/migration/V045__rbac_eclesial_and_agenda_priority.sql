-- Migration V045: RBAC Eclesial e Validação da Agenda Comunitária (Níveis de Prioridade 1-4)
ALTER TABLE calendario.eventos ADD COLUMN IF NOT EXISTS nivel_prioridade INT DEFAULT 4 NOT NULL;
ALTER TABLE calendario.eventos ADD COLUMN IF NOT EXISTS autorizado_pelo_cpp BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE calendario.eventos ADD COLUMN IF NOT EXISTS setor_pastoral VARCHAR(64);

ALTER TABLE calendario.organizacoes ADD COLUMN IF NOT EXISTS setor_pastoral VARCHAR(64);

CREATE INDEX IF NOT EXISTS idx_eventos_nivel_prioridade ON calendario.eventos (nivel_prioridade);
