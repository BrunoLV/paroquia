CREATE TABLE calendario.notificacoes (
    id UUID PRIMARY KEY, usuario_id UUID NOT NULL REFERENCES calendario.usuarios(id), tipo VARCHAR(64) NOT NULL,
    mensagem VARCHAR(500) NOT NULL, criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL, lida_em_utc TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_notificacoes_usuario ON calendario.notificacoes(usuario_id, lida_em_utc, criado_em_utc DESC);
