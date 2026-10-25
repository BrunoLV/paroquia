-- V032: Consentimento via Portal, Trilha de Auditoria LGPD, Desafios OTP e Solicitações ao DPO (DSAR)

-- 1. Paridade Clérigo: colunas de consentimento e LGPD
ALTER TABLE calendario.clerigos
    ADD COLUMN IF NOT EXISTS termo_consentimento_aceito BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS data_hora_consentimento_utc TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS ip_consentimento VARCHAR(45),
    ADD COLUMN IF NOT EXISTS versao_termo_consentimento VARCHAR(20) DEFAULT 'v1.0',
    ADD COLUMN IF NOT EXISTS canal_consentimento VARCHAR(30) DEFAULT 'PORTAL_AUTOATENDIMENTO',
    ADD COLUMN IF NOT EXISTS hash_termo_consentimento VARCHAR(64),
    ADD COLUMN IF NOT EXISTS uso_imagem_autorizado BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS permitir_divulgacao_aniversario BOOLEAN NOT NULL DEFAULT FALSE;

COMMENT ON COLUMN calendario.clerigos.termo_consentimento_aceito IS 'Indica se o clérigo consentiu com o tratamento dos dados eclesiásticos e pastorais sob a LGPD';
COMMENT ON COLUMN calendario.clerigos.data_hora_consentimento_utc IS 'Carimbo UTC de concessão ou revogação do consentimento';
COMMENT ON COLUMN calendario.clerigos.ip_consentimento IS 'IP de origem registrado no momento da manifestação de vontade';
COMMENT ON COLUMN calendario.clerigos.versao_termo_consentimento IS 'Versão do termo aceito pelo clérigo';
COMMENT ON COLUMN calendario.clerigos.canal_consentimento IS 'Canal utilizado para coleta (ex: PORTAL_AUTOATENDIMENTO, PRESENCIAL)';
COMMENT ON COLUMN calendario.clerigos.hash_termo_consentimento IS 'Hash SHA-256 do texto canônico e termo aceito';
COMMENT ON COLUMN calendario.clerigos.uso_imagem_autorizado IS 'Autorização para uso de imagem e voz em transmissões e divulgações';
COMMENT ON COLUMN calendario.clerigos.permitir_divulgacao_aniversario IS 'Autorização para divulgação comunitária de aniversário de ordenação ou nascimento';

-- 2. Complemento em fiéis: canal, hash e responsável de menor (Art. 14 da LGPD)
ALTER TABLE calendario.fieis
    ADD COLUMN IF NOT EXISTS canal_consentimento VARCHAR(30) DEFAULT 'PORTAL_AUTOATENDIMENTO',
    ADD COLUMN IF NOT EXISTS hash_termo_consentimento VARCHAR(64),
    ADD COLUMN IF NOT EXISTS responsavel_menor_nome VARCHAR(255),
    ADD COLUMN IF NOT EXISTS responsavel_menor_cpf VARCHAR(14);

COMMENT ON COLUMN calendario.fieis.canal_consentimento IS 'Canal de manifestação do consentimento (ex: PORTAL_AUTOATENDIMENTO, PRESENCIAL)';
COMMENT ON COLUMN calendario.fieis.hash_termo_consentimento IS 'Hash criptográfico SHA-256 do termo assinado digitalmente';
COMMENT ON COLUMN calendario.fieis.responsavel_menor_nome IS 'Nome civil do pai, mãe ou responsável legal quando titular for menor de 18 anos (Art. 14 LGPD)';
COMMENT ON COLUMN calendario.fieis.responsavel_menor_cpf IS 'CPF do responsável legal anuente quando titular for menor de 18 anos';

-- 3. Tabela imutável de auditoria de consentimento (Art. 8º, § 2º e Art. 6º, X da LGPD)
CREATE TABLE IF NOT EXISTS calendario.historico_consentimentos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titular_tipo VARCHAR(20) NOT NULL, -- 'FIEL' ou 'CLERIGO'
    titular_id UUID NOT NULL,
    usuario_id UUID, -- NULL quando operado pelo próprio titular via autoatendimento
    tipo_evento VARCHAR(30) NOT NULL, -- 'CONCESSAO', 'ATUALIZACAO', 'REVOGACAO'
    canal VARCHAR(30) NOT NULL DEFAULT 'PORTAL_AUTOATENDIMENTO',
    metodo_autenticacao VARCHAR(30) NOT NULL DEFAULT 'OTP_EMAIL',
    termo_versao VARCHAR(20) NOT NULL,
    termo_hash VARCHAR(64) NOT NULL,
    termo_aceito BOOLEAN NOT NULL,
    uso_imagem_autorizado BOOLEAN NOT NULL DEFAULT FALSE,
    permitir_divulgacao_aniversario BOOLEAN NOT NULL DEFAULT FALSE,
    ip_origem VARCHAR(45),
    user_agent VARCHAR(500),
    responsavel_menor_nome VARCHAR(255),
    responsavel_menor_cpf VARCHAR(14),
    criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema',
    atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema'
);

CREATE INDEX IF NOT EXISTS idx_hist_consentimentos_titular 
    ON calendario.historico_consentimentos(titular_tipo, titular_id);

CREATE INDEX IF NOT EXISTS idx_hist_consentimentos_data 
    ON calendario.historico_consentimentos(criado_em_utc DESC);

COMMENT ON TABLE calendario.historico_consentimentos IS 'Trilha imutável e indelével de auditoria de consentimento e revogação sob os Arts. 8º § 2º e 6º X da LGPD';
COMMENT ON COLUMN calendario.historico_consentimentos.id IS 'Identificador único do evento de auditoria';
COMMENT ON COLUMN calendario.historico_consentimentos.titular_tipo IS 'Tipo do titular afetado: FIEL ou CLERIGO';
COMMENT ON COLUMN calendario.historico_consentimentos.titular_id IS 'Identificador UUID do titular';
COMMENT ON COLUMN calendario.historico_consentimentos.usuario_id IS 'Identificador do operador quando não for autoatendimento direto';
COMMENT ON COLUMN calendario.historico_consentimentos.tipo_evento IS 'Natureza da manifestação: CONCESSAO, ATUALIZACAO ou REVOGACAO';
COMMENT ON COLUMN calendario.historico_consentimentos.canal IS 'Canal de manifestação: PORTAL_AUTOATENDIMENTO, PRESENCIAL';
COMMENT ON COLUMN calendario.historico_consentimentos.metodo_autenticacao IS 'Método de verificação: OTP_EMAIL, PRESENCIAL_SECRETARIA';
COMMENT ON COLUMN calendario.historico_consentimentos.termo_versao IS 'Versão do termo de consentimento vigente';
COMMENT ON COLUMN calendario.historico_consentimentos.termo_hash IS 'Hash SHA-256 do texto normativo assinado';
COMMENT ON COLUMN calendario.historico_consentimentos.termo_aceito IS 'Indicador se o termo pastoral foi aceito ou revogado';
COMMENT ON COLUMN calendario.historico_consentimentos.uso_imagem_autorizado IS 'Autorização para uso de imagem e voz';
COMMENT ON COLUMN calendario.historico_consentimentos.permitir_divulgacao_aniversario IS 'Autorização para divulgação de aniversário';
COMMENT ON COLUMN calendario.historico_consentimentos.ip_origem IS 'Endereço IP do cliente na data da manifestação';
COMMENT ON COLUMN calendario.historico_consentimentos.user_agent IS 'Dispositivo e navegador do titular';
COMMENT ON COLUMN calendario.historico_consentimentos.responsavel_menor_nome IS 'Nome civil do responsável legal quando titular menor de 18 anos';
COMMENT ON COLUMN calendario.historico_consentimentos.responsavel_menor_cpf IS 'CPF do responsável legal quando titular menor de 18 anos';
COMMENT ON COLUMN calendario.historico_consentimentos.criado_em_utc IS 'Carimbo UTC de criação do registro';
COMMENT ON COLUMN calendario.historico_consentimentos.atualizado_em_utc IS 'Carimbo UTC de atualização do registro';
COMMENT ON COLUMN calendario.historico_consentimentos.criado_por IS 'Identificador do autor do registro';
COMMENT ON COLUMN calendario.historico_consentimentos.atualizado_por IS 'Identificador do autor da última modificação';

-- 4. Tabela de desafios de verificação efêmeros (OTP por e-mail)
CREATE TABLE IF NOT EXISTS calendario.desafios_consentimento (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titular_tipo VARCHAR(20) NOT NULL,
    titular_id UUID NOT NULL,
    codigo_hash VARCHAR(64) NOT NULL,
    tentativas INT NOT NULL DEFAULT 0,
    expira_em_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema',
    atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema'
);

CREATE INDEX IF NOT EXISTS idx_desafios_consentimento_titular 
    ON calendario.desafios_consentimento(titular_tipo, titular_id);

CREATE INDEX IF NOT EXISTS idx_desafios_consentimento_expira 
    ON calendario.desafios_consentimento(expira_em_utc);

COMMENT ON TABLE calendario.desafios_consentimento IS 'Armazena desafios OTP temporários de uso único para validação de posse de e-mail';
COMMENT ON COLUMN calendario.desafios_consentimento.id IS 'Identificador único do desafio de validação';
COMMENT ON COLUMN calendario.desafios_consentimento.titular_tipo IS 'Tipo do titular: FIEL ou CLERIGO';
COMMENT ON COLUMN calendario.desafios_consentimento.titular_id IS 'Identificador do titular solicitante';
COMMENT ON COLUMN calendario.desafios_consentimento.codigo_hash IS 'Hash SHA-256 do código numérico de 6 dígitos enviado por e-mail';
COMMENT ON COLUMN calendario.desafios_consentimento.tentativas IS 'Quantidade de tentativas incorretas realizadas';
COMMENT ON COLUMN calendario.desafios_consentimento.expira_em_utc IS 'Carimbo UTC de expiração do código temporário';
COMMENT ON COLUMN calendario.desafios_consentimento.criado_em_utc IS 'Carimbo UTC de geração do desafio';
COMMENT ON COLUMN calendario.desafios_consentimento.atualizado_em_utc IS 'Carimbo UTC de atualização do desafio';
COMMENT ON COLUMN calendario.desafios_consentimento.criado_por IS 'Autor da criação do desafio';
COMMENT ON COLUMN calendario.desafios_consentimento.atualizado_por IS 'Autor da última atualização do desafio';

-- 5. Tabela de solicitações de exercício de direitos dos titulares ao DPO (DSAR - Arts. 18 e 19 da LGPD)
CREATE TABLE IF NOT EXISTS calendario.solicitacoes_dpo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    protocolo VARCHAR(36) NOT NULL UNIQUE,
    titular_nome VARCHAR(255) NOT NULL,
    titular_email VARCHAR(255) NOT NULL,
    titular_cpf VARCHAR(14),
    tipo_solicitacao VARCHAR(50) NOT NULL,
    mensagem TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'RECEBIDA',
    resposta TEXT,
    prazo_resposta_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    respondido_em_utc TIMESTAMP WITH TIME ZONE,
    criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (CURRENT_TIMESTAMP AT TIME ZONE 'UTC'),
    criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema',
    atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema'
);

CREATE INDEX IF NOT EXISTS idx_solicitacoes_dpo_status 
    ON calendario.solicitacoes_dpo(status);

CREATE INDEX IF NOT EXISTS idx_solicitacoes_dpo_protocolo 
    ON calendario.solicitacoes_dpo(protocolo);

COMMENT ON TABLE calendario.solicitacoes_dpo IS 'Registro e controle do atendimento de solicitações de direitos dos titulares ao Encarregado (Arts. 18 e 19 da LGPD)';
COMMENT ON COLUMN calendario.solicitacoes_dpo.id IS 'Identificador único da solicitação';
COMMENT ON COLUMN calendario.solicitacoes_dpo.protocolo IS 'Número público de protocolo para acompanhamento pelo titular';
COMMENT ON COLUMN calendario.solicitacoes_dpo.titular_nome IS 'Nome civil informado pelo solicitante';
COMMENT ON COLUMN calendario.solicitacoes_dpo.titular_email IS 'E-mail para resposta formal da paróquia';
COMMENT ON COLUMN calendario.solicitacoes_dpo.titular_cpf IS 'CPF opcional do titular para localização do prontuário';
COMMENT ON COLUMN calendario.solicitacoes_dpo.tipo_solicitacao IS 'Tipo de direito exercido: CONFIRMACAO, ACESSO, CORRECAO, PORTABILIDADE, REVOGACAO, OUTROS';
COMMENT ON COLUMN calendario.solicitacoes_dpo.mensagem IS 'Descrição detalhada do pedido do titular';
COMMENT ON COLUMN calendario.solicitacoes_dpo.status IS 'Status do atendimento: RECEBIDA, EM_ANALISE, ATENDIDA, REJEITADA';
COMMENT ON COLUMN calendario.solicitacoes_dpo.resposta IS 'Resposta fundamentada enviada pelo Encarregado (DPO)';
COMMENT ON COLUMN calendario.solicitacoes_dpo.prazo_resposta_utc IS 'Prazo legal improrrogável de 15 dias para fornecimento da resposta (Art. 19, II LGPD)';
COMMENT ON COLUMN calendario.solicitacoes_dpo.respondido_em_utc IS 'Carimbo UTC de conclusão do atendimento';
COMMENT ON COLUMN calendario.solicitacoes_dpo.criado_em_utc IS 'Carimbo UTC de registro da solicitação';
COMMENT ON COLUMN calendario.solicitacoes_dpo.atualizado_em_utc IS 'Carimbo UTC de última alteração';
COMMENT ON COLUMN calendario.solicitacoes_dpo.criado_por IS 'Autor do registro';
COMMENT ON COLUMN calendario.solicitacoes_dpo.atualizado_por IS 'Autor da última alteração';
