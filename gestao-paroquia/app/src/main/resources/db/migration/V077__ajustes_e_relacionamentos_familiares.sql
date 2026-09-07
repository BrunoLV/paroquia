CREATE TABLE calendario.solicitacoes_ajuste_cadastral (
    id UUID PRIMARY KEY, fiel_id UUID NOT NULL REFERENCES calendario.fieis(id), usuario_solicitante_id UUID NOT NULL REFERENCES calendario.usuarios(id),
    email VARCHAR(512), celular VARCHAR(255), telefone_fixo VARCHAR(255), endereco_logradouro VARCHAR(512), endereco_numero VARCHAR(255), endereco_complemento VARCHAR(255), endereco_bairro VARCHAR(255), endereco_cep VARCHAR(255), endereco_cidade VARCHAR(255), endereco_estado VARCHAR(2),
    status VARCHAR(16) NOT NULL, justificativa_analise VARCHAR(500), analisado_por_usuario_id UUID REFERENCES calendario.usuarios(id), analisado_em_utc TIMESTAMP WITH TIME ZONE, criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_ajuste_cadastral_pendente ON calendario.solicitacoes_ajuste_cadastral(status, criado_em_utc);

CREATE TABLE calendario.fiel_relacionamentos_familiares (
    id UUID PRIMARY KEY, par_id UUID NOT NULL, fiel_origem_id UUID NOT NULL REFERENCES calendario.fieis(id), fiel_destino_id UUID NOT NULL REFERENCES calendario.fieis(id), tipo_relacao VARCHAR(16) NOT NULL, criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL, deleted_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_relacao_familiar_ativa ON calendario.fiel_relacionamentos_familiares(fiel_origem_id, fiel_destino_id, tipo_relacao, deleted_at);
