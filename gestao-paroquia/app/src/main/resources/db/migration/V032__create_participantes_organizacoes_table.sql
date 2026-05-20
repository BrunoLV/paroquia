CREATE TABLE IF NOT EXISTS calendario.membros_organizacoes (
    id UUID PRIMARY KEY,
    membro_id UUID NOT NULL,
    organizacao_id UUID NOT NULL,
    data_inicio DATE NOT NULL,
    data_fim DATE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_part_membro FOREIGN KEY (membro_id) REFERENCES calendario.membros(id) ON DELETE CASCADE,
    CONSTRAINT fk_part_organizacao FOREIGN KEY (organizacao_id) REFERENCES calendario.organizacoes(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_membros_org_membro ON calendario.membros_organizacoes (membro_id);
CREATE INDEX IF NOT EXISTS idx_membros_org_organizacao ON calendario.membros_organizacoes (organizacao_id);
