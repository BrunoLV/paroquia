ALTER TABLE calendario.usuarios ADD COLUMN IF NOT EXISTS fiel_id UUID;
ALTER TABLE calendario.usuarios ADD CONSTRAINT fk_usuarios_fiel FOREIGN KEY (fiel_id) REFERENCES calendario.fieis(id);
ALTER TABLE calendario.usuarios ADD COLUMN IF NOT EXISTS criado_em_utc TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE calendario.usuarios ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE calendario.usuarios ADD COLUMN IF NOT EXISTS criado_por VARCHAR(255);
ALTER TABLE calendario.usuarios ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(255);

UPDATE calendario.usuarios
    SET criado_em_utc = CURRENT_TIMESTAMP
    WHERE criado_em_utc IS NULL;

UPDATE calendario.usuarios
    SET atualizado_em_utc = CURRENT_TIMESTAMP
    WHERE atualizado_em_utc IS NULL;

ALTER TABLE calendario.usuarios ALTER COLUMN criado_em_utc SET NOT NULL;
ALTER TABLE calendario.usuarios ALTER COLUMN atualizado_em_utc SET NOT NULL;
