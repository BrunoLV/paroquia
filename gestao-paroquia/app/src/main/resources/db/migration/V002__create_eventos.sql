CREATE TABLE IF NOT EXISTS calendario.eventos (
    id UUID PRIMARY KEY,
    titulo VARCHAR(160) NOT NULL,
    descricao VARCHAR(4000),
    organizacao_responsavel_id UUID NOT NULL,
    inicio_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    fim_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(32) NOT NULL,
    cancelado_motivo VARCHAR(2000),
    adicionado_extra_justificativa VARCHAR(4000),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_eventos_organizacao
        FOREIGN KEY (organizacao_responsavel_id)
        REFERENCES calendario.organizacoes (id)
);
