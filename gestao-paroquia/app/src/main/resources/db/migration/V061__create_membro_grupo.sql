DROP TABLE IF EXISTS calendario.membros_organizacoes;
DROP TABLE IF EXISTS calendario.membros_organizacao;
DROP TABLE IF EXISTS calendario.membros;

CREATE TABLE IF NOT EXISTS calendario.membro_grupo (
    id UUID PRIMARY KEY,
    fiel_id UUID NOT NULL,
    organizacao_id UUID NOT NULL,
    tipo_organizacao VARCHAR(32) NOT NULL,
    papel VARCHAR(64) NOT NULL,
    igreja_vinculo_id UUID,
    data_entrada DATE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    criado_por VARCHAR(255),
    atualizado_por VARCHAR(255),
    CONSTRAINT fk_membro_grupo_fiel FOREIGN KEY (fiel_id) REFERENCES calendario.fieis (id)
);

CREATE INDEX IF NOT EXISTS idx_membro_grupo_fiel_ativo ON calendario.membro_grupo (fiel_id, ativo);
CREATE INDEX IF NOT EXISTS idx_membro_grupo_organizacao_ativo ON calendario.membro_grupo (organizacao_id, ativo);
CREATE INDEX IF NOT EXISTS idx_membro_grupo_igreja_vinculo_ativo
    ON calendario.membro_grupo (igreja_vinculo_id, ativo);
