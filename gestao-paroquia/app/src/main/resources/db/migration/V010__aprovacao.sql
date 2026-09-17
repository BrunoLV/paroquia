/**
 * MIGRAÇÃO INICIAL: V010__aprovacao.sql
 * CONTEXTO: Cria o fluxo genérico de aprovação de ações administrativas e pastorais.
 * REGRA: Cada decisão preserva solicitante, aprovador, estado e correlação operacional.
 */

CREATE TABLE calendario.aprovacoes (
    id uuid NOT NULL,
    evento_id uuid,
    tipo_solicitacao character varying(64) NOT NULL,
    aprovador_papel character varying(64) NOT NULL,
    status character varying(32) NOT NULL,
    criado_em_utc timestamp with time zone NOT NULL,
    decidido_em_utc timestamp with time zone,
    version bigint DEFAULT 0 NOT NULL,
    executado_em_utc timestamp with time zone,
    solicitante_id character varying(255),
    solicitante_papel character varying(64),
    solicitante_tipo_grupo_paroquial character varying(64),
    aprovador_id character varying(255),
    motivo_cancelamento_snapshot character varying(2000),
    decision_observacao character varying(2000),
    action_payload_json character varying(4000),
    correlation_id character varying(128),
    mensagem_erro_execucao character varying(2000),
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.aprovacoes IS 'Solicitações de aprovação vinculadas a eventos, tramitando por papel aprovador até deferimento/indeferimento.';
COMMENT ON COLUMN calendario.aprovacoes.id IS 'Identificador primário da solicitação de aprovação (UUID).';
COMMENT ON COLUMN calendario.aprovacoes.evento_id IS 'Evento ao qual esta solicitação de aprovação se refere.';
COMMENT ON COLUMN calendario.aprovacoes.tipo_solicitacao IS 'Natureza da solicitação (ex.: criação, alteração, cancelamento de evento).';
COMMENT ON COLUMN calendario.aprovacoes.aprovador_papel IS 'Papel eclesiástico/administrativo com alçada para decidir a solicitação.';
COMMENT ON COLUMN calendario.aprovacoes.status IS 'Estado da solicitação (pendente, aprovada, rejeitada etc.).';
COMMENT ON COLUMN calendario.aprovacoes.criado_em_utc IS 'Data e hora de criação da solicitação.';
COMMENT ON COLUMN calendario.aprovacoes.decidido_em_utc IS 'Data e hora em que a solicitação foi decidida (aprovada ou rejeitada).';
COMMENT ON COLUMN calendario.aprovacoes.version IS 'Controle de versão para concorrência otimista (JPA).';
COMMENT ON COLUMN calendario.aprovacoes.solicitante_tipo_grupo_paroquial IS 'Tipo de grupo paroquial do solicitante, usado para determinar a alçada de aprovação.';
COMMENT ON COLUMN calendario.aprovacoes.mensagem_erro_execucao IS 'Mensagem de erro registrada quando a execução automática da ação aprovada falha.';
COMMENT ON COLUMN calendario.aprovacoes.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.aprovacoes.criado_por IS 'Identificador do usuario que criou a solicitacao de aprovacao.';
COMMENT ON COLUMN calendario.aprovacoes.atualizado_por IS 'Identificador do usuario que realizou a ultima alteracao no workflow.';
COMMENT ON COLUMN calendario.aprovacoes.executado_em_utc IS 'Instante UTC associado a executado no ciclo de vida do registro.';
COMMENT ON COLUMN calendario.aprovacoes.solicitante_id IS 'Identificador do registro relacionado por solicitante no contexto de aprovacoes.';
COMMENT ON COLUMN calendario.aprovacoes.solicitante_papel IS 'Propriedade solicitante papel que caracteriza o registro de aprovacoes.';
COMMENT ON COLUMN calendario.aprovacoes.aprovador_id IS 'Identificador do registro relacionado por aprovador no contexto de aprovacoes.';
COMMENT ON COLUMN calendario.aprovacoes.motivo_cancelamento_snapshot IS 'Propriedade motivo cancelamento snapshot que caracteriza o registro de aprovacoes.';
COMMENT ON COLUMN calendario.aprovacoes.decision_observacao IS 'Propriedade decision observacao que caracteriza o registro de aprovacoes.';
COMMENT ON COLUMN calendario.aprovacoes.action_payload_json IS 'Representação estruturada em JSON de action payload.';
COMMENT ON COLUMN calendario.aprovacoes.correlation_id IS 'Identificador do registro relacionado por correlation no contexto de aprovacoes.';
ALTER TABLE ONLY calendario.aprovacoes
    ADD CONSTRAINT aprovacoes_pkey PRIMARY KEY (id);
CREATE INDEX idx_aprovacoes_correlation_id ON calendario.aprovacoes USING btree (correlation_id);
CREATE INDEX idx_aprovacoes_evento_id ON calendario.aprovacoes USING btree (evento_id);
CREATE INDEX idx_aprovacoes_evento_tipo_status ON calendario.aprovacoes USING btree (evento_id, tipo_solicitacao, status);
CREATE INDEX idx_aprovacoes_status ON calendario.aprovacoes USING btree (status);
CREATE INDEX idx_aprovacoes_tipo_status_criado ON calendario.aprovacoes USING btree (tipo_solicitacao, status, criado_em_utc);

