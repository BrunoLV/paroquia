ALTER TABLE calendario.eventos ADD COLUMN recorrencia_id UUID;

ALTER TABLE calendario.eventos
    ADD CONSTRAINT fk_eventos_recorrencia
    FOREIGN KEY (recorrencia_id)
    REFERENCES calendario.eventos_recorrencia (id);

CREATE INDEX IF NOT EXISTS idx_eventos_recorrencia_id
    ON calendario.eventos (recorrencia_id);
