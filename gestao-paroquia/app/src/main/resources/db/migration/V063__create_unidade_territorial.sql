CREATE TABLE IF NOT EXISTS calendario.unidade_territorial (
    id UUID PRIMARY KEY,
    nome VARCHAR(255) NOT NULL UNIQUE,
    tipo VARCHAR(32) NOT NULL,
    contato VARCHAR(255),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    parent_id UUID,
    version BIGINT NOT NULL DEFAULT 0
);

-- Migra as linhas territoriais (CLERO/MATRIZ/COMUNIDADE) de organizacoes, preservando os IDs originais
-- para não quebrar referências existentes (membro_grupo.organizacao_id, eventos.organizacao_responsavel_id
-- etc. continuam apontando para o mesmo UUID, agora em unidade_territorial).
INSERT INTO calendario.unidade_territorial (id, nome, tipo, contato, ativo, parent_id, version)
SELECT id, nome, tipo, contato, ativo, parent_id, version
FROM calendario.organizacoes
WHERE tipo IN ('CLERO', 'MATRIZ', 'COMUNIDADE')
ON CONFLICT (id) DO NOTHING;
