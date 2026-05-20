CREATE INDEX IF NOT EXISTS idx_eventos_inicio_fim ON calendario.eventos (inicio_utc, fim_utc);
CREATE INDEX IF NOT EXISTS idx_eventos_status ON calendario.eventos (status);
