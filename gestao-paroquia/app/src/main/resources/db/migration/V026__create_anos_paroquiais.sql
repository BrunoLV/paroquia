CREATE TABLE IF NOT EXISTS calendario.anos_paroquiais (
    ano INTEGER PRIMARY KEY,
    status VARCHAR(32) NOT NULL,
    data_fechamento_utc TIMESTAMP WITH TIME ZONE,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Seed current and next year in PLANEJAMENTO status (H2 compatible)
INSERT INTO calendario.anos_paroquiais (ano, status)
SELECT 2026, 'PLANEJAMENTO'
WHERE NOT EXISTS (SELECT 1 FROM calendario.anos_paroquiais WHERE ano = 2026);

INSERT INTO calendario.anos_paroquiais (ano, status)
SELECT 2027, 'PLANEJAMENTO'
WHERE NOT EXISTS (SELECT 1 FROM calendario.anos_paroquiais WHERE ano = 2027);
