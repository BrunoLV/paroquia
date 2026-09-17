/**
 * MIGRAÇÃO INICIAL: V002__fiel.sql
 * CONTEXTO: Cria o cadastro pastoral de fiéis, dependentes, vínculos familiares e solicitações cadastrais.
 * REGRA: Dados civis e relacionamentos permanecem auditáveis e segregados por finalidade.
 */

CREATE TABLE calendario.fieis (
    id uuid NOT NULL,
    nome character varying(255),
    cpf character varying(255),
    cpf_hash character varying(64),
    data_nascimento character varying(255),
    email character varying(255),
    telefone character varying(255),
    vocacao character varying(32),
    ativo boolean DEFAULT true NOT NULL,
    criado_em_utc timestamp with time zone NOT NULL,
    atualizado_em_utc timestamp with time zone NOT NULL,
    criado_por character varying(255) NOT NULL,
    atualizado_por character varying(255) NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    endereco_logradouro character varying(500),
    endereco_numero character varying(255),
    endereco_complemento character varying(255),
    endereco_bairro character varying(255),
    endereco_cep character varying(255),
    endereco_cidade character varying(255),
    endereco_estado character varying(2),
    rg character varying(255),
    sexo character varying(20) DEFAULT 'NAO_INFORMADO'::character varying,
    profissao character varying(100),
    telefone_fixo character varying(255),
    celular_whatsapp character varying(255),
    preferencia_canal_comunicacao character varying(20) DEFAULT 'WHATSAPP'::character varying,
    estado_civil character varying(30) DEFAULT 'SOLTEIRO'::character varying,
    conjuge_id uuid,
    habilidades_voluntariado text,
    termo_consentimento_aceito boolean DEFAULT false NOT NULL,
    data_hora_consentimento_utc timestamp with time zone,
    ip_consentimento character varying(255),
    versao_termo_consentimento character varying(20) DEFAULT 'v1.0'::character varying,
    uso_imagem_autorizado boolean DEFAULT false NOT NULL,
    permitir_divulgacao_aniversario boolean DEFAULT true NOT NULL,
    desativado_em_utc timestamp with time zone,
    dados_cadastrais_eliminados boolean DEFAULT false NOT NULL,
    pais_origem character varying(3),
    documento_estrangeiro_tipo character varying(16),
    documento_estrangeiro_numero character varying(255),
    celular character varying(255),
    is_whatsapp boolean DEFAULT false NOT NULL,
    unidade_territorial_id uuid,
    is_dizimista boolean DEFAULT false NOT NULL
);
COMMENT ON TABLE calendario.fieis IS 'Cadastro de fiéis (leigos) da paróquia: dados civis, contato e vínculos sacramentais/territoriais, sob regime LGPD.';
COMMENT ON COLUMN calendario.fieis.id IS 'Identificador primário do fiel (UUID).';
COMMENT ON COLUMN calendario.fieis.nome IS 'Nome civil completo do fiel.';
COMMENT ON COLUMN calendario.fieis.cpf IS 'CPF do fiel, armazenado de forma reversível para exibição autorizada.';
COMMENT ON COLUMN calendario.fieis.cpf_hash IS 'Hash irreversível do CPF, usado como chave de unicidade e busca sem expor o dado sensível.';
COMMENT ON COLUMN calendario.fieis.data_nascimento IS 'Data de nascimento do fiel.';
COMMENT ON COLUMN calendario.fieis.email IS 'E-mail de contato do fiel.';
COMMENT ON COLUMN calendario.fieis.telefone IS 'Telefone de contato do fiel.';
COMMENT ON COLUMN calendario.fieis.vocacao IS 'Vocação/estado de vida eclesial do fiel (leigo, religioso etc.).';
COMMENT ON COLUMN calendario.fieis.ativo IS 'Indica se o cadastro está ativo (falso em caso de desativação/ciclo de vida LGPD).';
COMMENT ON COLUMN calendario.fieis.criado_em_utc IS 'Data e hora de criação do cadastro.';
COMMENT ON COLUMN calendario.fieis.atualizado_em_utc IS 'Data e hora da última atualização do cadastro.';
COMMENT ON COLUMN calendario.fieis.criado_por IS 'Usuário/processo que criou o registro.';
COMMENT ON COLUMN calendario.fieis.atualizado_por IS 'Usuário/processo que fez a última atualização.';
COMMENT ON COLUMN calendario.fieis.version IS 'Controle de versão para concorrência otimista (JPA).';
COMMENT ON COLUMN calendario.fieis.unidade_territorial_id IS 'Unidade territorial (matriz/comunidade) à qual o fiel está vinculado.';
COMMENT ON COLUMN calendario.fieis.is_dizimista IS 'Indica se o fiel é dizimista cadastrado.';
COMMENT ON COLUMN calendario.fieis.endereco_logradouro IS 'Propriedade endereco logradouro que caracteriza o registro de fieis.';
COMMENT ON COLUMN calendario.fieis.endereco_numero IS 'Propriedade endereco numero que caracteriza o registro de fieis.';
COMMENT ON COLUMN calendario.fieis.endereco_complemento IS 'Propriedade endereco complemento que caracteriza o registro de fieis.';
COMMENT ON COLUMN calendario.fieis.endereco_bairro IS 'Propriedade endereco bairro que caracteriza o registro de fieis.';
COMMENT ON COLUMN calendario.fieis.endereco_cep IS 'Propriedade endereco cep que caracteriza o registro de fieis.';
COMMENT ON COLUMN calendario.fieis.endereco_cidade IS 'Propriedade endereco cidade que caracteriza o registro de fieis.';
COMMENT ON COLUMN calendario.fieis.endereco_estado IS 'Propriedade endereco estado que caracteriza o registro de fieis.';
COMMENT ON COLUMN calendario.fieis.rg IS 'Propriedade rg que caracteriza o registro de fieis.';
COMMENT ON COLUMN calendario.fieis.sexo IS 'Propriedade sexo que caracteriza o registro de fieis.';
COMMENT ON COLUMN calendario.fieis.profissao IS 'Propriedade profissao que caracteriza o registro de fieis.';
COMMENT ON COLUMN calendario.fieis.telefone_fixo IS 'Propriedade telefone fixo que caracteriza o registro de fieis.';
COMMENT ON COLUMN calendario.fieis.celular_whatsapp IS 'Propriedade celular whatsapp que caracteriza o registro de fieis.';
COMMENT ON COLUMN calendario.fieis.preferencia_canal_comunicacao IS 'Propriedade preferencia canal comunicacao que caracteriza o registro de fieis.';
COMMENT ON COLUMN calendario.fieis.estado_civil IS 'Propriedade estado civil que caracteriza o registro de fieis.';
COMMENT ON COLUMN calendario.fieis.conjuge_id IS 'Identificador do registro relacionado por conjuge no contexto de fieis.';
COMMENT ON COLUMN calendario.fieis.habilidades_voluntariado IS 'Propriedade habilidades voluntariado que caracteriza o registro de fieis.';
COMMENT ON COLUMN calendario.fieis.termo_consentimento_aceito IS 'Propriedade termo consentimento aceito que caracteriza o registro de fieis.';
COMMENT ON COLUMN calendario.fieis.data_hora_consentimento_utc IS 'Data de negócio referente a hora consentimento utc.';
COMMENT ON COLUMN calendario.fieis.ip_consentimento IS 'Propriedade ip consentimento que caracteriza o registro de fieis.';
COMMENT ON COLUMN calendario.fieis.versao_termo_consentimento IS 'Propriedade versao termo consentimento que caracteriza o registro de fieis.';
COMMENT ON COLUMN calendario.fieis.uso_imagem_autorizado IS 'Propriedade uso imagem autorizado que caracteriza o registro de fieis.';
COMMENT ON COLUMN calendario.fieis.permitir_divulgacao_aniversario IS 'Propriedade permitir divulgacao aniversario que caracteriza o registro de fieis.';
COMMENT ON COLUMN calendario.fieis.desativado_em_utc IS 'Instante UTC associado a desativado no ciclo de vida do registro.';
COMMENT ON COLUMN calendario.fieis.dados_cadastrais_eliminados IS 'Propriedade dados cadastrais eliminados que caracteriza o registro de fieis.';
COMMENT ON COLUMN calendario.fieis.pais_origem IS 'Propriedade pais origem que caracteriza o registro de fieis.';
COMMENT ON COLUMN calendario.fieis.documento_estrangeiro_tipo IS 'Propriedade documento estrangeiro tipo que caracteriza o registro de fieis.';
COMMENT ON COLUMN calendario.fieis.documento_estrangeiro_numero IS 'Propriedade documento estrangeiro numero que caracteriza o registro de fieis.';
COMMENT ON COLUMN calendario.fieis.celular IS 'Propriedade celular que caracteriza o registro de fieis.';
COMMENT ON COLUMN calendario.fieis.is_whatsapp IS 'Propriedade is whatsapp que caracteriza o registro de fieis.';
ALTER TABLE ONLY calendario.fieis
    ADD CONSTRAINT fieis_pkey PRIMARY KEY (id);
ALTER TABLE ONLY calendario.fieis
    ADD CONSTRAINT uk_fieis_cpf_hash UNIQUE (cpf_hash);
CREATE INDEX idx_fieis_ativo ON calendario.fieis USING btree (ativo);
CREATE INDEX idx_fieis_retencao_lgpd ON calendario.fieis USING btree (ativo, dados_cadastrais_eliminados, desativado_em_utc);
CREATE INDEX idx_fieis_unidade_territorial ON calendario.fieis USING btree (unidade_territorial_id);

CREATE TABLE calendario.fiel_dependentes (
    id uuid NOT NULL,
    fiel_id uuid NOT NULL,
    nome character varying(255),
    data_nascimento character varying(255),
    parentesco character varying(20) DEFAULT 'FILHO'::character varying NOT NULL,
    batizado boolean DEFAULT false NOT NULL,
    primeira_comunhao boolean DEFAULT false NOT NULL,
    crismado boolean DEFAULT false NOT NULL,
    criado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.fiel_dependentes IS 'Dependentes vinculados ao cadastro de um fiel, com dados sacramentais mínimos.';
COMMENT ON COLUMN calendario.fiel_dependentes.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao do dependente.';
COMMENT ON COLUMN calendario.fiel_dependentes.criado_por IS 'Identificador do usuario que registrou o dependente.';
COMMENT ON COLUMN calendario.fiel_dependentes.atualizado_por IS 'Identificador do usuario que atualizou o dependente.';
COMMENT ON COLUMN calendario.fiel_dependentes.id IS 'Identificador único do registro de fiel dependentes.';
COMMENT ON COLUMN calendario.fiel_dependentes.fiel_id IS 'Identificador do registro relacionado por fiel no contexto de fiel dependentes.';
COMMENT ON COLUMN calendario.fiel_dependentes.nome IS 'Informação textual de nome no contexto de fiel dependentes.';
COMMENT ON COLUMN calendario.fiel_dependentes.data_nascimento IS 'Data de negócio referente a nascimento.';
COMMENT ON COLUMN calendario.fiel_dependentes.parentesco IS 'Propriedade parentesco que caracteriza o registro de fiel dependentes.';
COMMENT ON COLUMN calendario.fiel_dependentes.batizado IS 'Propriedade batizado que caracteriza o registro de fiel dependentes.';
COMMENT ON COLUMN calendario.fiel_dependentes.primeira_comunhao IS 'Propriedade primeira comunhao que caracteriza o registro de fiel dependentes.';
COMMENT ON COLUMN calendario.fiel_dependentes.crismado IS 'Propriedade crismado que caracteriza o registro de fiel dependentes.';
COMMENT ON COLUMN calendario.fiel_dependentes.criado_em_utc IS 'Instante UTC de criação imutável do registro.';
ALTER TABLE ONLY calendario.fiel_dependentes
    ADD CONSTRAINT fiel_dependentes_pkey PRIMARY KEY (id);
CREATE INDEX idx_fiel_dependentes_fiel_id ON calendario.fiel_dependentes USING btree (fiel_id);
ALTER TABLE ONLY calendario.fiel_dependentes
    ADD CONSTRAINT fiel_dependentes_fiel_id_fkey FOREIGN KEY (fiel_id) REFERENCES calendario.fieis(id) ON DELETE CASCADE;

CREATE TABLE calendario.solicitacoes_ajuste_cadastral (
    id uuid NOT NULL,
    fiel_id uuid NOT NULL,
    usuario_solicitante_id uuid NOT NULL,
    email character varying(512),
    celular character varying(255),
    telefone_fixo character varying(255),
    endereco_logradouro character varying(512),
    endereco_numero character varying(255),
    endereco_complemento character varying(255),
    endereco_bairro character varying(255),
    endereco_cep character varying(255),
    endereco_cidade character varying(255),
    endereco_estado character varying(2),
    status character varying(16) NOT NULL,
    justificativa_analise character varying(500),
    analisado_por_usuario_id uuid,
    analisado_em_utc timestamp with time zone,
    criado_em_utc timestamp with time zone NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.solicitacoes_ajuste_cadastral IS 'Solicitações auditáveis de alteração de dados cadastrais e exercício de direitos LGPD.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_cadastral.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao da solicitacao de ajuste cadastral.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_cadastral.criado_por IS 'Identificador do solicitante/sistema.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_cadastral.atualizado_por IS 'Identificador do revisor/sistema.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_cadastral.id IS 'Identificador único do registro de solicitacoes ajuste cadastral.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_cadastral.fiel_id IS 'Identificador do registro relacionado por fiel no contexto de solicitacoes ajuste cadastral.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_cadastral.usuario_solicitante_id IS 'Identificador do registro relacionado por usuario solicitante no contexto de solicitacoes ajuste cadastral.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_cadastral.email IS 'Propriedade email que caracteriza o registro de solicitacoes ajuste cadastral.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_cadastral.celular IS 'Propriedade celular que caracteriza o registro de solicitacoes ajuste cadastral.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_cadastral.telefone_fixo IS 'Propriedade telefone fixo que caracteriza o registro de solicitacoes ajuste cadastral.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_cadastral.endereco_logradouro IS 'Propriedade endereco logradouro que caracteriza o registro de solicitacoes ajuste cadastral.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_cadastral.endereco_numero IS 'Propriedade endereco numero que caracteriza o registro de solicitacoes ajuste cadastral.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_cadastral.endereco_complemento IS 'Propriedade endereco complemento que caracteriza o registro de solicitacoes ajuste cadastral.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_cadastral.endereco_bairro IS 'Propriedade endereco bairro que caracteriza o registro de solicitacoes ajuste cadastral.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_cadastral.endereco_cep IS 'Propriedade endereco cep que caracteriza o registro de solicitacoes ajuste cadastral.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_cadastral.endereco_cidade IS 'Propriedade endereco cidade que caracteriza o registro de solicitacoes ajuste cadastral.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_cadastral.endereco_estado IS 'Propriedade endereco estado que caracteriza o registro de solicitacoes ajuste cadastral.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_cadastral.status IS 'Estado atual do ciclo de vida e das transições permitidas pelo domínio.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_cadastral.justificativa_analise IS 'Propriedade justificativa analise que caracteriza o registro de solicitacoes ajuste cadastral.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_cadastral.analisado_por_usuario_id IS 'Identificador do registro relacionado por analisado por usuario no contexto de solicitacoes ajuste cadastral.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_cadastral.analisado_em_utc IS 'Instante UTC associado a analisado no ciclo de vida do registro.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_cadastral.criado_em_utc IS 'Instante UTC de criação imutável do registro.';
ALTER TABLE ONLY calendario.solicitacoes_ajuste_cadastral
    ADD CONSTRAINT solicitacoes_ajuste_cadastral_pkey PRIMARY KEY (id);
CREATE INDEX idx_ajuste_cadastral_pendente ON calendario.solicitacoes_ajuste_cadastral USING btree (status, criado_em_utc);
ALTER TABLE ONLY calendario.solicitacoes_ajuste_cadastral
    ADD CONSTRAINT solicitacoes_ajuste_cadastral_fiel_id_fkey FOREIGN KEY (fiel_id) REFERENCES calendario.fieis(id);

CREATE TABLE calendario.relacionamentos_familiares (
    id uuid NOT NULL,
    par_id uuid NOT NULL,
    pessoa_origem_id uuid NOT NULL,
    tipo_pessoa_origem character varying(16) DEFAULT 'FIEL'::character varying NOT NULL,
    pessoa_destino_id uuid NOT NULL,
    tipo_pessoa_destino character varying(16) DEFAULT 'FIEL'::character varying NOT NULL,
    tipo_relacao character varying(16) NOT NULL,
    criado_em_utc timestamp with time zone NOT NULL,
    deleted_at timestamp with time zone,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.relacionamentos_familiares IS 'Vínculos familiares polimórficos entre fiéis e clérigos usados para contexto pastoral.';
COMMENT ON COLUMN calendario.relacionamentos_familiares.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao do relacionamento familiar.';
COMMENT ON COLUMN calendario.relacionamentos_familiares.criado_por IS 'Identificador do usuario que registrou o relacionamento.';
COMMENT ON COLUMN calendario.relacionamentos_familiares.atualizado_por IS 'Identificador do usuario que atualizou o relacionamento.';
COMMENT ON COLUMN calendario.relacionamentos_familiares.id IS 'Identificador único do registro de relacionamentos familiares.';
COMMENT ON COLUMN calendario.relacionamentos_familiares.par_id IS 'Identificador do registro relacionado por par no contexto de relacionamentos familiares.';
COMMENT ON COLUMN calendario.relacionamentos_familiares.pessoa_origem_id IS 'Identificador do registro relacionado por pessoa origem no contexto de relacionamentos familiares.';
COMMENT ON COLUMN calendario.relacionamentos_familiares.tipo_pessoa_origem IS 'Propriedade tipo pessoa origem que caracteriza o registro de relacionamentos familiares.';
COMMENT ON COLUMN calendario.relacionamentos_familiares.pessoa_destino_id IS 'Identificador do registro relacionado por pessoa destino no contexto de relacionamentos familiares.';
COMMENT ON COLUMN calendario.relacionamentos_familiares.tipo_pessoa_destino IS 'Propriedade tipo pessoa destino que caracteriza o registro de relacionamentos familiares.';
COMMENT ON COLUMN calendario.relacionamentos_familiares.tipo_relacao IS 'Propriedade tipo relacao que caracteriza o registro de relacionamentos familiares.';
COMMENT ON COLUMN calendario.relacionamentos_familiares.criado_em_utc IS 'Instante UTC de criação imutável do registro.';
COMMENT ON COLUMN calendario.relacionamentos_familiares.deleted_at IS 'Propriedade deleted at que caracteriza o registro de relacionamentos familiares.';
ALTER TABLE ONLY calendario.relacionamentos_familiares
    ADD CONSTRAINT relacionamentos_familiares_pkey PRIMARY KEY (id);
CREATE INDEX idx_relacao_familiar_polimorfica ON calendario.relacionamentos_familiares USING btree (pessoa_origem_id, tipo_pessoa_origem, deleted_at);

