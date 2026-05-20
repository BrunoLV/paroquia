ALTER TABLE calendario.projetos_eventos ADD COLUMN organizacao_responsavel_id UUID;
ALTER TABLE calendario.projetos_eventos ADD COLUMN inicio_utc TIMESTAMP WITH TIME ZONE;
ALTER TABLE calendario.projetos_eventos ADD COLUMN fim_utc TIMESTAMP WITH TIME ZONE;

-- Migration of existing data is not strictly required as the table is likely empty or can have nulls temporarily
-- But for production readiness, we keep it nullable and will make it NOT NULL after data alignment if needed.
