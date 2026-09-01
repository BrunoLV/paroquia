-- V070__garantir_colunas_reestruturacao_fiel.sql
-- Adição idempotente de todas as colunas do Agregado Fiel (Endereço Estruturado, Vida Sacramental, LGPD e Dependentes).

-- 1. Endereço Estruturado
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS endereco_logradouro VARCHAR(500) NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS endereco_numero VARCHAR(255) NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS endereco_complemento VARCHAR(255) NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS endereco_bairro VARCHAR(255) NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS endereco_cep VARCHAR(255) NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS endereco_cidade VARCHAR(255) NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS endereco_estado VARCHAR(2) NULL;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS endereco;

-- 2. Identificação Pessoal & Contato
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS rg VARCHAR(255) NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS sexo VARCHAR(20) NULL DEFAULT 'NAO_INFORMADO';
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS profissao VARCHAR(100) NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS telefone_fixo VARCHAR(255) NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS celular_whatsapp VARCHAR(255) NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS preferencia_canal_comunicacao VARCHAR(20) NULL DEFAULT 'WHATSAPP';

-- 3. Núcleo Familiar
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS estado_civil VARCHAR(30) NULL DEFAULT 'SOLTEIRO';
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS conjuge_id UUID NULL;

-- 4. Vida Sacramental Detalhada
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS data_batismo VARCHAR(255) NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS paroquia_batismo VARCHAR(255) NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS celebrante_batismo VARCHAR(255) NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS livro_batismo VARCHAR(50) NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS folha_batismo VARCHAR(50) NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS termo_batismo VARCHAR(50) NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS padrinhos_batismo TEXT NULL;

ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS data_primeira_comunhao VARCHAR(255) NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS paroquia_primeira_comunhao VARCHAR(255) NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS turma_catequese VARCHAR(100) NULL;

ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS data_crisma VARCHAR(255) NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS paroquia_crisma VARCHAR(255) NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS celebrante_crisma VARCHAR(255) NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS padrinho_crisma VARCHAR(255) NULL;

ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS data_matrimonio VARCHAR(255) NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS paroquia_matrimonio VARCHAR(255) NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS numero_processo_matrimonial VARCHAR(50) NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS certidao_matrimonio VARCHAR(50) NULL;

-- 5. Habilidades e Voluntariado
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS habilidades_voluntariado TEXT NULL;

-- 6. Gestão de Consentimento e LGPD
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS termo_consentimento_aceito BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS data_hora_consentimento_utc TIMESTAMP WITH TIME ZONE NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS ip_consentimento VARCHAR(255) NULL;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS versao_termo_consentimento VARCHAR(20) NULL DEFAULT 'v1.0';
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS uso_imagem_autorizado BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS permitir_divulgacao_aniversario BOOLEAN NOT NULL DEFAULT TRUE;

-- 7. Tabela de Dependentes / Filhos do Fiel
CREATE TABLE IF NOT EXISTS calendario.fiel_dependentes (
    id UUID PRIMARY KEY,
    fiel_id UUID NOT NULL REFERENCES calendario.fieis(id) ON DELETE CASCADE,
    nome VARCHAR(255) NOT NULL,
    data_nascimento VARCHAR(255) NULL,
    parentesco VARCHAR(20) NOT NULL DEFAULT 'FILHO',
    batizado BOOLEAN NOT NULL DEFAULT FALSE,
    primeira_comunhao BOOLEAN NOT NULL DEFAULT FALSE,
    crismado BOOLEAN NOT NULL DEFAULT FALSE,
    criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_fiel_dependentes_fiel_id ON calendario.fiel_dependentes (fiel_id);
