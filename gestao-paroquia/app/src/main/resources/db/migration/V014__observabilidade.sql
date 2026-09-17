/**
 * MIGRAÇÃO INICIAL: V014__observabilidade.sql
 * CONTEXTO: Cria auditoria operacional e bloqueios distribuídos para tarefas agendadas.
 * REGRA: Eventos de auditoria e exclusão mútua de jobs suportam rastreabilidade e execução confiável.
 */

CREATE TABLE calendario.auditoria_operacoes (
    id uuid NOT NULL,
    grupo_paroquial_id uuid NOT NULL,
    evento_id uuid,
    recurso_tipo character varying(64) NOT NULL,
    recurso_id character varying(255) NOT NULL,
    acao character varying(64) NOT NULL,
    resultado character varying(32) NOT NULL,
    ator character varying(255) NOT NULL,
    ator_usuario_id uuid,
    correlation_id character varying(128) NOT NULL,
    detalhes_auditaveis_json character varying(4000) NOT NULL,
    ocorrido_em_utc timestamp with time zone NOT NULL,
    criado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.auditoria_operacoes IS 'Trilha de operações relevantes para rastreabilidade administrativa e investigação de incidentes.';
COMMENT ON COLUMN calendario.auditoria_operacoes.criado_em_utc IS 'Timestamp UTC de criacao do registro de auditoria operacional.';
COMMENT ON COLUMN calendario.auditoria_operacoes.atualizado_em_utc IS 'Timestamp UTC de atualizacao do registro de auditoria operacional.';
COMMENT ON COLUMN calendario.auditoria_operacoes.criado_por IS 'Identificador do autor do registro.';
COMMENT ON COLUMN calendario.auditoria_operacoes.atualizado_por IS 'Identificador do modificador do registro.';
COMMENT ON COLUMN calendario.auditoria_operacoes.id IS 'Identificador único do registro de auditoria operacoes.';
COMMENT ON COLUMN calendario.auditoria_operacoes.grupo_paroquial_id IS 'Identificador do registro relacionado por grupo paroquial no contexto de auditoria operacoes.';
COMMENT ON COLUMN calendario.auditoria_operacoes.evento_id IS 'Identificador do registro relacionado por evento no contexto de auditoria operacoes.';
COMMENT ON COLUMN calendario.auditoria_operacoes.recurso_tipo IS 'Propriedade recurso tipo que caracteriza o registro de auditoria operacoes.';
COMMENT ON COLUMN calendario.auditoria_operacoes.recurso_id IS 'Identificador do registro relacionado por recurso no contexto de auditoria operacoes.';
COMMENT ON COLUMN calendario.auditoria_operacoes.acao IS 'Propriedade acao que caracteriza o registro de auditoria operacoes.';
COMMENT ON COLUMN calendario.auditoria_operacoes.resultado IS 'Propriedade resultado que caracteriza o registro de auditoria operacoes.';
COMMENT ON COLUMN calendario.auditoria_operacoes.ator IS 'Propriedade ator que caracteriza o registro de auditoria operacoes.';
COMMENT ON COLUMN calendario.auditoria_operacoes.ator_usuario_id IS 'Identificador do registro relacionado por ator usuario no contexto de auditoria operacoes.';
COMMENT ON COLUMN calendario.auditoria_operacoes.correlation_id IS 'Identificador do registro relacionado por correlation no contexto de auditoria operacoes.';
COMMENT ON COLUMN calendario.auditoria_operacoes.detalhes_auditaveis_json IS 'Representação estruturada em JSON de detalhes auditaveis.';
COMMENT ON COLUMN calendario.auditoria_operacoes.ocorrido_em_utc IS 'Instante UTC associado a ocorrido no ciclo de vida do registro.';
ALTER TABLE ONLY calendario.auditoria_operacoes
    ADD CONSTRAINT auditoria_operacoes_pkey PRIMARY KEY (id);
CREATE INDEX idx_auditoria_operacoes_correlation ON calendario.auditoria_operacoes USING btree (correlation_id, ocorrido_em_utc, id);
CREATE INDEX idx_auditoria_operacoes_evento_periodo ON calendario.auditoria_operacoes USING btree (evento_id, ocorrido_em_utc, id);
CREATE INDEX idx_auditoria_operacoes_org_periodo ON calendario.auditoria_operacoes USING btree (grupo_paroquial_id, ocorrido_em_utc, id);

CREATE TABLE calendario.job_locks (
    name character varying(128) NOT NULL,
    locked_until timestamp with time zone NOT NULL,
    locked_at timestamp with time zone NOT NULL,
    criado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.job_locks IS 'Bloqueios distribuídos de tarefas agendadas para impedir execução concorrente.';
COMMENT ON COLUMN calendario.job_locks.criado_em_utc IS 'Timestamp UTC de criacao do lock distribuido.';
COMMENT ON COLUMN calendario.job_locks.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao do lock distribuido.';
COMMENT ON COLUMN calendario.job_locks.criado_por IS 'Identificador da instancia/sistema que criou o lock.';
COMMENT ON COLUMN calendario.job_locks.atualizado_por IS 'Identificador da instancia/sistema que atualizou o lock.';
COMMENT ON COLUMN calendario.job_locks.name IS 'Propriedade name que caracteriza o registro de job locks.';
COMMENT ON COLUMN calendario.job_locks.locked_until IS 'Propriedade locked until que caracteriza o registro de job locks.';
COMMENT ON COLUMN calendario.job_locks.locked_at IS 'Propriedade locked at que caracteriza o registro de job locks.';
ALTER TABLE ONLY calendario.job_locks
    ADD CONSTRAINT job_locks_pkey PRIMARY KEY (name);

