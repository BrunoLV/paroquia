CREATE TABLE IF NOT EXISTS calendario.eventos_envolvidos (
    evento_id UUID NOT NULL,
    organizacao_id UUID NOT NULL,
    papel_participacao VARCHAR(64),
    PRIMARY KEY (evento_id, organizacao_id),
    CONSTRAINT fk_eventos_envolvidos_evento
        FOREIGN KEY (evento_id)
        REFERENCES calendario.eventos (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_eventos_envolvidos_organizacao
        FOREIGN KEY (organizacao_id)
        REFERENCES calendario.organizacoes (id)
);
