CREATE TABLE IF NOT EXISTS calendario.papel_global (
    id UUID PRIMARY KEY,
    fiel_id UUID NOT NULL,
    papel VARCHAR(32) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    criado_por VARCHAR(255),
    atualizado_por VARCHAR(255),
    CONSTRAINT fk_papel_global_fiel FOREIGN KEY (fiel_id) REFERENCES calendario.fieis (id),
    CONSTRAINT uk_papel_global_fiel_papel UNIQUE (fiel_id, papel)
);

CREATE INDEX IF NOT EXISTS idx_papel_global_fiel_ativo ON calendario.papel_global (fiel_id, ativo);
