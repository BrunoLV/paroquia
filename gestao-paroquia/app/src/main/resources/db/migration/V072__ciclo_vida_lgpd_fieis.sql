-- Retenção: a desativação preserva o registro; depois de cinco anos a aplicação anonimiza a PII.
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS desativado_em_utc TIMESTAMP WITH TIME ZONE NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS dados_cadastrais_eliminados BOOLEAN NOT NULL DEFAULT FALSE;

-- Os campos abaixo passam a aceitar NULL exclusivamente após a anonimização LGPD.
ALTER TABLE calendario.fieis ALTER COLUMN nome DROP NOT NULL;
ALTER TABLE calendario.fieis ALTER COLUMN cpf DROP NOT NULL;
ALTER TABLE calendario.fieis ALTER COLUMN cpf_hash DROP NOT NULL;
ALTER TABLE calendario.fieis ALTER COLUMN data_nascimento DROP NOT NULL;
ALTER TABLE calendario.fieis ALTER COLUMN email DROP NOT NULL;
ALTER TABLE calendario.fieis ALTER COLUMN telefone DROP NOT NULL;
ALTER TABLE calendario.fiel_dependentes ALTER COLUMN nome DROP NOT NULL;

CREATE INDEX IF NOT EXISTS idx_fieis_retencao_lgpd
    ON calendario.fieis (ativo, dados_cadastrais_eliminados, desativado_em_utc);
