-- O agregado Fiel deixa de ser repositório sacramental; este conteúdo pertencerá a módulo próprio.
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS pais_origem VARCHAR(3);
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS documento_estrangeiro_tipo VARCHAR(16);
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS documento_estrangeiro_numero VARCHAR(255);
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS celular VARCHAR(255);
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS is_whatsapp BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS unidade_territorial_id UUID;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS is_dizimista BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE calendario.fieis SET pais_origem = COALESCE(pais_origem, 'BRA');
UPDATE calendario.fieis SET celular = COALESCE(celular, celular_whatsapp, telefone);
UPDATE calendario.fieis SET is_whatsapp = COALESCE(is_whatsapp, FALSE);
UPDATE calendario.fieis SET unidade_territorial_id = COALESCE(unidade_territorial_id,
    (SELECT id FROM calendario.unidade_territorial WHERE tipo = 'MATRIZ' AND ativo = TRUE ORDER BY nome LIMIT 1));

ALTER TABLE calendario.fieis ALTER COLUMN telefone DROP NOT NULL;
ALTER TABLE calendario.fieis ALTER COLUMN vocacao DROP NOT NULL;

ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS batizado;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS primeira_comunhao;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS crismado;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS casado_igreja_catolica;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS data_batismo;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS paroquia_batismo;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS celebrante_batismo;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS livro_batismo;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS folha_batismo;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS termo_batismo;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS padrinhos_batismo;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS data_primeira_comunhao;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS paroquia_primeira_comunhao;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS turma_catequese;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS data_crisma;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS paroquia_crisma;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS celebrante_crisma;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS padrinho_crisma;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS data_matrimonio;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS paroquia_matrimonio;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS numero_processo_matrimonial;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS certidao_matrimonio;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS nome_certidao;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS nome_pai_certidao;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS nome_mae_certidao;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS testemunhas_matrimonio;

DROP TABLE IF EXISTS calendario.solicitacoes_certidao_sacramental;
CREATE INDEX IF NOT EXISTS idx_fieis_unidade_territorial ON calendario.fieis(unidade_territorial_id);
