/**
 * MIGRAÇÃO INICIAL: V008__projeto.sql
 * CONTEXTO: Cria projetos pastorais e seus fluxos de solicitação de aprovação.
 * REGRA: Alterações relevantes em projetos são registradas para decisão rastreável.
 */

CREATE TABLE calendario.projetos_eventos (
    id uuid NOT NULL,
    nome character varying(160) NOT NULL,
    descricao character varying(2000),
    status character varying(32) DEFAULT 'ATIVO'::character varying NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    grupo_paroquial_responsavel_id uuid,
    inicio_utc timestamp with time zone,
    fim_utc timestamp with time zone,
    criado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.projetos_eventos IS 'Projetos que agrupam múltiplos eventos sob um objetivo pastoral comum, com fluxo de gestão e aprovação próprios.';
COMMENT ON COLUMN calendario.projetos_eventos.id IS 'Identificador primário do projeto (UUID).';
COMMENT ON COLUMN calendario.projetos_eventos.nome IS 'Nome do projeto.';
COMMENT ON COLUMN calendario.projetos_eventos.descricao IS 'Descrição do objetivo/escopo do projeto.';
COMMENT ON COLUMN calendario.projetos_eventos.status IS 'Estado do projeto (ex.: ativo, encerrado).';
COMMENT ON COLUMN calendario.projetos_eventos.version IS 'Controle de versão para concorrência otimista (JPA).';
COMMENT ON COLUMN calendario.projetos_eventos.grupo_paroquial_responsavel_id IS 'Grupo paroquial responsável pelo projeto.';
COMMENT ON COLUMN calendario.projetos_eventos.inicio_utc IS 'Data/hora de início planejado do projeto.';
COMMENT ON COLUMN calendario.projetos_eventos.fim_utc IS 'Data/hora de término planejado do projeto.';
COMMENT ON COLUMN calendario.projetos_eventos.criado_em_utc IS 'Timestamp UTC de criacao do projeto paroquial.';
COMMENT ON COLUMN calendario.projetos_eventos.atualizado_em_utc IS 'Timestamp UTC de atualizacao do projeto paroquial.';
COMMENT ON COLUMN calendario.projetos_eventos.criado_por IS 'Identificador do usuario que criou o projeto.';
COMMENT ON COLUMN calendario.projetos_eventos.atualizado_por IS 'Identificador do usuario que atualizou o projeto.';
ALTER TABLE ONLY calendario.projetos_eventos
    ADD CONSTRAINT projetos_eventos_pkey PRIMARY KEY (id);

CREATE TABLE calendario.projeto_solicitacoes_aprovacao (
    id uuid NOT NULL,
    projeto_id uuid,
    tipo_solicitacao character varying(64) NOT NULL,
    status character varying(32) NOT NULL,
    solicitante_id character varying(255) NOT NULL,
    solicitante_papel character varying(64) NOT NULL,
    solicitante_tipo_grupo_paroquial character varying(64) NOT NULL,
    action_payload_json character varying(4000),
    cancelar_eventos_vinculados boolean DEFAULT false,
    criado_em_utc timestamp with time zone NOT NULL,
    decidido_em_utc timestamp with time zone,
    aprovador_id character varying(255),
    decision_observacao character varying(2000),
    version bigint DEFAULT 0 NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.projeto_solicitacoes_aprovacao IS 'Workflow de aprovação de criação, alteração ou cancelamento de projetos pastorais.';
COMMENT ON COLUMN calendario.projeto_solicitacoes_aprovacao.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao da aprovacao de projeto.';
COMMENT ON COLUMN calendario.projeto_solicitacoes_aprovacao.criado_por IS 'Identificador do solicitante.';
COMMENT ON COLUMN calendario.projeto_solicitacoes_aprovacao.atualizado_por IS 'Identificador do aprovador.';
COMMENT ON COLUMN calendario.projeto_solicitacoes_aprovacao.id IS 'Identificador único do registro de projeto solicitacoes aprovacao.';
COMMENT ON COLUMN calendario.projeto_solicitacoes_aprovacao.projeto_id IS 'Identificador do registro relacionado por projeto no contexto de projeto solicitacoes aprovacao.';
COMMENT ON COLUMN calendario.projeto_solicitacoes_aprovacao.tipo_solicitacao IS 'Propriedade tipo solicitacao que caracteriza o registro de projeto solicitacoes aprovacao.';
COMMENT ON COLUMN calendario.projeto_solicitacoes_aprovacao.status IS 'Estado atual do ciclo de vida e das transições permitidas pelo domínio.';
COMMENT ON COLUMN calendario.projeto_solicitacoes_aprovacao.solicitante_id IS 'Identificador do registro relacionado por solicitante no contexto de projeto solicitacoes aprovacao.';
COMMENT ON COLUMN calendario.projeto_solicitacoes_aprovacao.solicitante_papel IS 'Propriedade solicitante papel que caracteriza o registro de projeto solicitacoes aprovacao.';
COMMENT ON COLUMN calendario.projeto_solicitacoes_aprovacao.solicitante_tipo_grupo_paroquial IS 'Propriedade solicitante tipo grupo paroquial que caracteriza o registro de projeto solicitacoes aprovacao.';
COMMENT ON COLUMN calendario.projeto_solicitacoes_aprovacao.action_payload_json IS 'Representação estruturada em JSON de action payload.';
COMMENT ON COLUMN calendario.projeto_solicitacoes_aprovacao.cancelar_eventos_vinculados IS 'Propriedade cancelar eventos vinculados que caracteriza o registro de projeto solicitacoes aprovacao.';
COMMENT ON COLUMN calendario.projeto_solicitacoes_aprovacao.criado_em_utc IS 'Instante UTC de criação imutável do registro.';
COMMENT ON COLUMN calendario.projeto_solicitacoes_aprovacao.decidido_em_utc IS 'Instante UTC associado a decidido no ciclo de vida do registro.';
COMMENT ON COLUMN calendario.projeto_solicitacoes_aprovacao.aprovador_id IS 'Identificador do registro relacionado por aprovador no contexto de projeto solicitacoes aprovacao.';
COMMENT ON COLUMN calendario.projeto_solicitacoes_aprovacao.decision_observacao IS 'Propriedade decision observacao que caracteriza o registro de projeto solicitacoes aprovacao.';
COMMENT ON COLUMN calendario.projeto_solicitacoes_aprovacao.version IS 'Versão usada para controle de concorrência otimista do registro.';
ALTER TABLE ONLY calendario.projeto_solicitacoes_aprovacao
    ADD CONSTRAINT projeto_solicitacoes_aprovacao_pkey PRIMARY KEY (id);
CREATE INDEX idx_proj_aprov_status ON calendario.projeto_solicitacoes_aprovacao USING btree (status);

