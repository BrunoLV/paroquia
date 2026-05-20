CREATE TABLE IF NOT EXISTS calendario.eventos_recorrencia (
    id UUID PRIMARY KEY,
    evento_base_id UUID NOT NULL,
    frequencia VARCHAR(32) NOT NULL,
    intervalo INTEGER NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_eventos_recorrencia_evento
        FOREIGN KEY (evento_base_id)
        REFERENCES calendario.eventos (id)
        ON DELETE CASCADE
);
