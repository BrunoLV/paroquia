/**
 * MIGRAÇÃO EVOLUTIVA: V040__lgpd_expansao_cifra_blind_index_e_desafios.sql
 * CONTEXTO: Fase de EXPANSÃO (expand & contract) da adequação LGPD de consentimento, DPO e clero.
 * REGRA: o ciphertext AES-GCM versionado ("v1:" + Base64) é bem maior que o dado em claro; as colunas que
 * passam a ser cifradas são alargadas aqui. A cifragem dos dados existentes e o rehash do blind index de CPF
 * ocorrem em V041 (migração Java, pois exigem a chave da aplicação); V042 torna a trilha append-only.
 */

-- 1. Colunas de PII do canal do DPO que passam a ser cifradas (SolicitacaoDpoEntity)
ALTER TABLE calendario.solicitacoes_dpo
    ALTER COLUMN titular_nome TYPE character varying(1024),
    ALTER COLUMN titular_email TYPE character varying(1024),
    ALTER COLUMN titular_cpf TYPE character varying(512);

COMMENT ON COLUMN calendario.solicitacoes_dpo.titular_nome IS 'Nome civil informado pelo solicitante, cifrado em repouso (AES-GCM versionado).';
COMMENT ON COLUMN calendario.solicitacoes_dpo.titular_email IS 'E-mail para resposta formal da paróquia, cifrado em repouso (AES-GCM versionado).';
COMMENT ON COLUMN calendario.solicitacoes_dpo.titular_cpf IS 'CPF opcional do titular, cifrado em repouso (AES-GCM versionado).';

-- 2. Trilha de consentimento: IP e dados do responsável legal passam a ser cifrados
ALTER TABLE calendario.historico_consentimentos
    ALTER COLUMN ip_origem TYPE character varying(512),
    ALTER COLUMN responsavel_menor_nome TYPE character varying(1024),
    ALTER COLUMN responsavel_menor_cpf TYPE character varying(512);

COMMENT ON COLUMN calendario.historico_consentimentos.ip_origem IS 'Endereço IP do cliente na data da manifestação, cifrado em repouso (AES-GCM versionado).';
COMMENT ON COLUMN calendario.historico_consentimentos.responsavel_menor_nome IS 'Nome civil do responsável legal quando titular menor de 18 anos, cifrado em repouso.';
COMMENT ON COLUMN calendario.historico_consentimentos.responsavel_menor_cpf IS 'CPF do responsável legal quando titular menor de 18 anos, cifrado em repouso.';

-- 3. Fiéis: o responsável do menor já era cifrado pela aplicação, porém a coluna só comportava 14 caracteres
ALTER TABLE calendario.fieis
    ALTER COLUMN responsavel_menor_nome TYPE character varying(1024),
    ALTER COLUMN responsavel_menor_cpf TYPE character varying(512);

COMMENT ON COLUMN calendario.fieis.responsavel_menor_nome IS 'Nome civil do pai, mãe ou responsável legal quando titular for menor de 18 anos (Art. 14 LGPD), cifrado em repouso.';
COMMENT ON COLUMN calendario.fieis.responsavel_menor_cpf IS 'CPF do responsável legal anuente quando titular for menor de 18 anos, cifrado em repouso.';

-- 4. Paridade clérigo/fiel: blind index de CPF e marca de anonimização definitiva
ALTER TABLE calendario.clerigos
    ADD COLUMN IF NOT EXISTS cpf_hash character varying(64),
    ADD COLUMN IF NOT EXISTS dados_cadastrais_eliminados boolean NOT NULL DEFAULT false;

-- A anonimização zera os dados civis; estas colunas não podem mais ser obrigatórias no banco
-- (a obrigatoriedade no cadastro continua garantida pelo domínio e pela validação dos DTOs).
ALTER TABLE calendario.clerigos
    ALTER COLUMN nome_civil DROP NOT NULL,
    ALTER COLUMN data_nascimento DROP NOT NULL,
    ALTER COLUMN email DROP NOT NULL,
    ALTER COLUMN celular DROP NOT NULL;

CREATE INDEX IF NOT EXISTS idx_clerigos_cpf_hash ON calendario.clerigos (cpf_hash);

COMMENT ON COLUMN calendario.clerigos.cpf_hash IS 'Blind index do CPF (HMAC-SHA256 com pepper), para busca exata sem decifrar a base.';
COMMENT ON COLUMN calendario.clerigos.dados_cadastrais_eliminados IS 'Indica anonimização definitiva dos dados civis do clérigo (LGPD); irreversível.';

-- 5. Desafios OTP: janela de pedidos (rate limit) e token de consentimento de uso único
ALTER TABLE calendario.desafios_consentimento
    ADD COLUMN IF NOT EXISTS validado_em_utc timestamp with time zone,
    ADD COLUMN IF NOT EXISTS consumido_em_utc timestamp with time zone;

CREATE INDEX IF NOT EXISTS idx_desafios_consentimento_titular_criado
    ON calendario.desafios_consentimento (titular_tipo, titular_id, criado_em_utc DESC);

COMMENT ON COLUMN calendario.desafios_consentimento.validado_em_utc IS 'Momento em que o código OTP foi aceito; o desafio validado é a identidade (jti) do token de consentimento.';
COMMENT ON COLUMN calendario.desafios_consentimento.consumido_em_utc IS 'Momento em que o token de consentimento foi usado; o token é de uso único.';
