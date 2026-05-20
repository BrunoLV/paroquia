ALTER TABLE calendario.eventos
    ADD COLUMN projeto_id UUID;

ALTER TABLE calendario.eventos
    ADD CONSTRAINT fk_eventos_projeto
    FOREIGN KEY (projeto_id)
    REFERENCES calendario.projetos_eventos (id);

CREATE INDEX IF NOT EXISTS idx_eventos_projeto_id
    ON calendario.eventos (projeto_id);
