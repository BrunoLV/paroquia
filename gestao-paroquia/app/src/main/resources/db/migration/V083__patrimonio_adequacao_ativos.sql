-- V083: Adequação do agregado Ativo Patrimonial à especificação revisada.
--   * Renomeia colunas para a linguagem ubíqua definitiva (codigo_tombamento, valor_aquisicao, is_reservavel).
--   * Introduz descrição editável, autoria de cadastro e exclusão lógica (soft delete).
--   * Converge o enum de status para DISPONIVEL | RESERVADO | EM_MANUTENCAO (descontinua ALOCADO e BAIXADO).
--   * Torna data e valor de aquisição obrigatórios, com backfill conservador das linhas legadas.

ALTER TABLE calendario.ativos_patrimoniais RENAME COLUMN numero_tombamento TO codigo_tombamento;
ALTER TABLE calendario.ativos_patrimoniais RENAME COLUMN valor_estimado TO valor_aquisicao;
ALTER TABLE calendario.ativos_patrimoniais RENAME COLUMN reservavel TO is_reservavel;

ALTER TABLE calendario.ativos_patrimoniais ADD COLUMN IF NOT EXISTS descricao TEXT;
ALTER TABLE calendario.ativos_patrimoniais ADD COLUMN IF NOT EXISTS cadastrado_por_id UUID;
ALTER TABLE calendario.ativos_patrimoniais ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE calendario.ativos_patrimoniais ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITH TIME ZONE;

-- Backfill idempotente das linhas legadas antes de aplicar as restrições NOT NULL / CHECK.
UPDATE calendario.ativos_patrimoniais SET data_aquisicao = CAST(criado_em_utc AS DATE) WHERE data_aquisicao IS NULL;
UPDATE calendario.ativos_patrimoniais SET valor_aquisicao = 0 WHERE valor_aquisicao IS NULL;
UPDATE calendario.ativos_patrimoniais SET updated_at = criado_em_utc WHERE updated_at IS NULL;
UPDATE calendario.ativos_patrimoniais SET status = 'RESERVADO' WHERE status = 'ALOCADO';
UPDATE calendario.ativos_patrimoniais SET deleted_at = NOW(), status = 'DISPONIVEL' WHERE status = 'BAIXADO';

ALTER TABLE calendario.ativos_patrimoniais ALTER COLUMN data_aquisicao SET NOT NULL;
ALTER TABLE calendario.ativos_patrimoniais ALTER COLUMN valor_aquisicao SET NOT NULL;
ALTER TABLE calendario.ativos_patrimoniais ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE calendario.ativos_patrimoniais
    ADD CONSTRAINT chk_ativos_patrimoniais_status
    CHECK (status IN ('DISPONIVEL', 'RESERVADO', 'EM_MANUTENCAO'));

ALTER TABLE calendario.ativos_patrimoniais
    ADD CONSTRAINT chk_ativos_patrimoniais_valor_aquisicao
    CHECK (valor_aquisicao >= 0);

CREATE INDEX IF NOT EXISTS idx_ativos_patrimoniais_grupo_deleted
    ON calendario.ativos_patrimoniais (grupo_paroquial_id, deleted_at);

COMMENT ON COLUMN calendario.ativos_patrimoniais.codigo_tombamento IS 'Código patrimonial único gerado pelo sistema no cadastro (padrão PAT-AAAA-NNNN); imutável.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.valor_aquisicao IS 'Valor financeiro de compra ou avaliação do bem no ato da aquisição; imutável e não negativo.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.is_reservavel IS 'Define se o bem é elegível para reserva no módulo de Reservas; imutável após o cadastro.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.descricao IS 'Detalhamento técnico e estado físico do bem; único campo editável após o cadastro.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.cadastrado_por_id IS 'Usuário autor do cadastro (auditoria); nulo apenas para linhas anteriores à V083.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.deleted_at IS 'Instante da exclusão lógica (soft delete); nulo enquanto o bem está incorporado ao patrimônio.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.updated_at IS 'Instante da última alteração do registro.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.status IS 'Estado corrente do bem: DISPONIVEL, RESERVADO (reserva em vigor) ou EM_MANUTENCAO.';
