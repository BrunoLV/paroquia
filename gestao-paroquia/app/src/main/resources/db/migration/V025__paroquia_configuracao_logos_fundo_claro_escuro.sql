/**
 * MIGRAÇÃO EVOLUTIVA: V025__paroquia_configuracao_logos_fundo_claro_escuro.sql
 * CONTEXTO: Suporte a logotipos binarizados para fundo claro e escuro e desacoplamento do YAML.
 * REGRA: Os dados gerais residem na Matriz (unidade territorial) e Diocese (governo eclesiástico).
 * A tabela paroquia_configuracao armazena estritamente a identidade visual (cores e logotipos binarizados).
 */

-- 1. Expansão de colunas de logotipos binarizados e tipos MIME
ALTER TABLE calendario.paroquia_configuracao
    ADD COLUMN IF NOT EXISTS logo_fundo_claro_bytes bytea,
    ADD COLUMN IF NOT EXISTS logo_fundo_claro_tipo_mime character varying(64) DEFAULT 'image/png',
    ADD COLUMN IF NOT EXISTS logo_fundo_escuro_bytes bytea,
    ADD COLUMN IF NOT EXISTS logo_fundo_escuro_tipo_mime character varying(64) DEFAULT 'image/png';

-- 2. Descontinuação das colunas redundantes (tornadas anuláveis para evitar duplicação)
ALTER TABLE calendario.paroquia_configuracao
    ALTER COLUMN nome DROP NOT NULL,
    ALTER COLUMN diocese DROP NOT NULL,
    ALTER COLUMN endereco DROP NOT NULL;

-- 3. Migração de dados legados: preserva logo_bytes existente em logo_fundo_claro_bytes
UPDATE calendario.paroquia_configuracao
SET logo_fundo_claro_bytes = logo_bytes
WHERE logo_fundo_claro_bytes IS NULL AND logo_bytes IS NOT NULL;

-- 4. Garantia de registro inicial padrão de identidade visual
INSERT INTO calendario.paroquia_configuracao (
    id,
    cor_primaria_hex,
    cor_secundaria_hex,
    version,
    criado_em_utc,
    atualizado_em_utc,
    criado_por,
    atualizado_por
) VALUES (
    '00000000-0000-0000-0000-000000000001'::uuid,
    '#1A237E',
    '#808080',
    0,
    NOW(),
    NOW(),
    'sistema',
    'sistema'
) ON CONFLICT (id) DO NOTHING;

-- 5. Comentários DDL de documentação viva
COMMENT ON COLUMN calendario.paroquia_configuracao.logo_fundo_claro_bytes IS 'Bytes binarizados do logotipo oficial da paróquia para aplicação sobre fundos claros.';
COMMENT ON COLUMN calendario.paroquia_configuracao.logo_fundo_claro_tipo_mime IS 'Tipo MIME do logotipo para fundo claro (ex.: image/png).';
COMMENT ON COLUMN calendario.paroquia_configuracao.logo_fundo_escuro_bytes IS 'Bytes binarizados do logotipo oficial da paróquia para aplicação sobre fundos escuros.';
COMMENT ON COLUMN calendario.paroquia_configuracao.logo_fundo_escuro_tipo_mime IS 'Tipo MIME do logotipo para fundo escuro (ex.: image/png).';
