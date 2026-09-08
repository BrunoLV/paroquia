-- V081: Adequação da tabela calendario.locais com endereço estruturado, soft delete e colunas de auditoria

ALTER TABLE calendario.locais ADD COLUMN IF NOT EXISTS cep VARCHAR(9);
ALTER TABLE calendario.locais ADD COLUMN IF NOT EXISTS logradouro VARCHAR(150);
ALTER TABLE calendario.locais ADD COLUMN IF NOT EXISTS numero VARCHAR(20);
ALTER TABLE calendario.locais ADD COLUMN IF NOT EXISTS complemento VARCHAR(100);
ALTER TABLE calendario.locais ADD COLUMN IF NOT EXISTS bairro VARCHAR(100);
ALTER TABLE calendario.locais ADD COLUMN IF NOT EXISTS cidade VARCHAR(100);
ALTER TABLE calendario.locais ADD COLUMN IF NOT EXISTS uf VARCHAR(2);
ALTER TABLE calendario.locais ADD COLUMN IF NOT EXISTS descricao TEXT;
ALTER TABLE calendario.locais ADD COLUMN IF NOT EXISTS capacidade_maxima INTEGER;
ALTER TABLE calendario.locais ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE calendario.locais ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.locais ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();

-- Migração de compatibilidade de dados legados
UPDATE calendario.locais
SET logradouro = SUBSTRING(endereco FROM 1 FOR 150)
WHERE logradouro IS NULL AND endereco IS NOT NULL;

UPDATE calendario.locais
SET descricao = caracteristicas
WHERE descricao IS NULL AND caracteristicas IS NOT NULL;

UPDATE calendario.locais
SET capacidade_maxima = capacidade
WHERE capacidade_maxima IS NULL AND capacidade IS NOT NULL;

-- Índices para otimização de consultas ativas e por organização/igreja
CREATE INDEX IF NOT EXISTS idx_locais_grupo_paroquial_deleted
    ON calendario.locais (grupo_paroquial_id, deleted_at);

CREATE INDEX IF NOT EXISTS idx_locais_ativo_deleted
    ON calendario.locais (ativo, deleted_at);

-- Metadados conceituais DDL
COMMENT ON TABLE calendario.locais IS 'Dependências físicas e salas vinculadas a paróquia ou comunidades para celebrações e eventos.';
COMMENT ON COLUMN calendario.locais.cep IS 'Código de Endereçamento Postal formatado (00000-000).';
COMMENT ON COLUMN calendario.locais.logradouro IS 'Nome da rua, avenida ou praça do local físico.';
COMMENT ON COLUMN calendario.locais.numero IS 'Número predial do local físico.';
COMMENT ON COLUMN calendario.locais.complemento IS 'Complemento predial ou de localização.';
COMMENT ON COLUMN calendario.locais.bairro IS 'Bairro do local físico.';
COMMENT ON COLUMN calendario.locais.cidade IS 'Município do local físico.';
COMMENT ON COLUMN calendario.locais.uf IS 'Unidade Federativa (sigla de 2 caracteres).';
COMMENT ON COLUMN calendario.locais.descricao IS 'Descrição detalhada de uso, equipamentos disponíveis ou restrições.';
COMMENT ON COLUMN calendario.locais.capacidade_maxima IS 'Lotação máxima de pessoas permitida no espaço físico.';
COMMENT ON COLUMN calendario.locais.deleted_at IS 'Data/hora de exclusão lógica (soft delete). Registros excluídos não aparecem em listagens gerais.';
COMMENT ON COLUMN calendario.locais.created_at IS 'Data/hora de criação do registro no sistema.';
COMMENT ON COLUMN calendario.locais.updated_at IS 'Data/hora da última alteração no registro.';
