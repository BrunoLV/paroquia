-- Exceção de retenção: qualificação indispensável ao registro e à certidão sacramental.
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS nome_pai_certidao VARCHAR(255) NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS nome_mae_certidao VARCHAR(255) NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS testemunhas_matrimonio TEXT NULL;
