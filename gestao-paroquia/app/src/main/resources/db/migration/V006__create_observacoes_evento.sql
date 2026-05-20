CREATE TABLE IF NOT EXISTS calendario.observacoes_evento (
    id UUID PRIMARY KEY,
    evento_id UUID NOT NULL,
    usuario_id UUID NOT NULL,
    tipo VARCHAR(64) NOT NULL,
    conteudo VARCHAR(4000) NOT NULL,
    criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);
