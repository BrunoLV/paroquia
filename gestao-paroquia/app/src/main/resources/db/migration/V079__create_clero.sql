CREATE TABLE calendario.clerigos (
    id UUID PRIMARY KEY,
    nome_civil VARCHAR(2048) NOT NULL,
    nome_eclesiastico VARCHAR(255) NOT NULL,
    data_nascimento VARCHAR(2048) NOT NULL,
    pais_origem VARCHAR(3) NOT NULL,
    cpf VARCHAR(2048),
    rg VARCHAR(2048),
    documento_estrangeiro VARCHAR(2048),
    email VARCHAR(2048) NOT NULL,
    celular VARCHAR(2048) NOT NULL,
    telefone_fixo VARCHAR(2048),
    whatsapp BOOLEAN NOT NULL DEFAULT FALSE,
    endereco VARCHAR(4096),
    grau_ordem VARCHAR(32) NOT NULL,
    diocese_incardinacao VARCHAR(255) NOT NULL,
    data_ordenacao_diaconal DATE,
    data_ordenacao_presbiteral DATE,
    data_ordenacao_episcopal DATE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    deleted_at TIMESTAMP WITH TIME ZONE,
    criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    criado_por VARCHAR(255) NOT NULL,
    atualizado_por VARCHAR(255) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);
CREATE INDEX idx_clerigos_ativos ON calendario.clerigos(ativo, nome_eclesiastico);

CREATE TABLE calendario.encargos_clericais (
    id UUID PRIMARY KEY,
    clerigo_id UUID NOT NULL REFERENCES calendario.clerigos(id),
    unidade_territorial_id UUID NOT NULL REFERENCES calendario.unidade_territorial(id),
    funcao VARCHAR(32) NOT NULL,
    atual BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    encerrado_em_utc TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_encargo_clerical_funcao_atual ON calendario.encargos_clericais(funcao, atual);

ALTER TABLE calendario.usuarios ADD COLUMN IF NOT EXISTS clerigo_id UUID;
ALTER TABLE calendario.usuarios ALTER COLUMN fiel_id DROP NOT NULL;
ALTER TABLE calendario.usuarios ADD CONSTRAINT fk_usuarios_clerigo
    FOREIGN KEY (clerigo_id) REFERENCES calendario.clerigos(id);
ALTER TABLE calendario.usuarios ADD CONSTRAINT ck_usuario_pessoa_exclusiva
    CHECK ((fiel_id IS NULL AND clerigo_id IS NOT NULL) OR (fiel_id IS NOT NULL AND clerigo_id IS NULL));

CREATE TABLE calendario.relacionamentos_familiares (
    id UUID PRIMARY KEY,
    par_id UUID NOT NULL,
    pessoa_origem_id UUID NOT NULL,
    tipo_pessoa_origem VARCHAR(16) NOT NULL DEFAULT 'FIEL',
    pessoa_destino_id UUID NOT NULL,
    tipo_pessoa_destino VARCHAR(16) NOT NULL DEFAULT 'FIEL',
    tipo_relacao VARCHAR(16) NOT NULL,
    criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);
CREATE INDEX idx_relacao_familiar_polimorfica ON calendario.relacionamentos_familiares(pessoa_origem_id, tipo_pessoa_origem, deleted_at);

CREATE TABLE calendario.solicitacoes_ajuste_clerical (
    id UUID PRIMARY KEY,
    usuario_solicitante_id UUID NOT NULL REFERENCES calendario.usuarios(id),
    clerigo_id UUID NOT NULL REFERENCES calendario.clerigos(id),
    email VARCHAR(2048) NOT NULL,
    celular VARCHAR(2048) NOT NULL,
    telefone_fixo VARCHAR(2048),
    endereco VARCHAR(4096) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDENTE',
    justificativa_analise VARCHAR(2048),
    analisado_por_usuario_id UUID REFERENCES calendario.usuarios(id),
    analisado_em_utc TIMESTAMP WITH TIME ZONE,
    criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL
);
CREATE INDEX idx_solicitacoes_ajuste_clerical_pendentes ON calendario.solicitacoes_ajuste_clerical(status, criado_em_utc);

CREATE TABLE calendario.migracoes_clero_pendentes (
    fiel_id UUID PRIMARY KEY REFERENCES calendario.fieis(id),
    papel_origem VARCHAR(64) NOT NULL,
    criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO calendario.migracoes_clero_pendentes (fiel_id, papel_origem)
SELECT DISTINCT pg.fiel_id, pg.papel
FROM calendario.papel_global pg
WHERE pg.ativo = TRUE
  AND pg.papel IN ('paroco', 'vigario')
  AND NOT EXISTS (
      SELECT 1 FROM calendario.migracoes_clero_pendentes mcp WHERE mcp.fiel_id = pg.fiel_id
  );
