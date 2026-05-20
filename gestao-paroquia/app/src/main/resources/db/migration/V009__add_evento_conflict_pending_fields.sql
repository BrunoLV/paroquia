ALTER TABLE calendario.eventos
    ADD COLUMN IF NOT EXISTS conflict_state VARCHAR(32);

ALTER TABLE calendario.eventos
    ADD COLUMN IF NOT EXISTS conflict_reason VARCHAR(2000);

CREATE INDEX IF NOT EXISTS idx_eventos_conflict_state
    ON calendario.eventos (conflict_state);
