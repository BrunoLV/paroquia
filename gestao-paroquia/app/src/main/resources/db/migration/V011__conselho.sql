/**
 * MIGRAÇÃO INICIAL: V011__conselho.sql
 * CONTEXTO: Cria deliberações formais dos conselhos paroquiais e comunitários.
 * REGRA: Pautas, homologação e veto permanecem rastreáveis para governança colegiada.
 */

CREATE TABLE calendario.cpc_decisoes (
    id uuid NOT NULL,
    titulo character varying(160) NOT NULL,
    pauta_descricao character varying(4000) NOT NULL,
    data_reuniao date NOT NULL,
    comunidade_id uuid NOT NULL,
    conselho_tipo character varying(32) DEFAULT 'CPC'::character varying NOT NULL,
    status_homologacao character varying(32) DEFAULT 'PENDENTE_HOMOLOGACAO'::character varying NOT NULL,
    solicitante_id character varying(128) NOT NULL,
    solicitante_nome character varying(160),
    homologador_id character varying(128),
    motivo_veto character varying(2000),
    criado_em_utc timestamp with time zone NOT NULL,
    decidido_em_utc timestamp with time zone,
    version bigint DEFAULT 0 NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.cpc_decisoes IS 'Decisões formais dos conselhos paroquiais e comunitários, com estado de deliberação.';
COMMENT ON COLUMN calendario.cpc_decisoes.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.cpc_decisoes.criado_por IS 'Identificador do solicitante/sistema que registrou a decisao do CPC.';
COMMENT ON COLUMN calendario.cpc_decisoes.atualizado_por IS 'Identificador do usuario que realizou a ultima alteracao na decisao.';
COMMENT ON COLUMN calendario.cpc_decisoes.id IS 'Identificador único do registro de cpc decisoes.';
COMMENT ON COLUMN calendario.cpc_decisoes.titulo IS 'Informação textual de titulo no contexto de cpc decisoes.';
COMMENT ON COLUMN calendario.cpc_decisoes.pauta_descricao IS 'Propriedade pauta descricao que caracteriza o registro de cpc decisoes.';
COMMENT ON COLUMN calendario.cpc_decisoes.data_reuniao IS 'Data de negócio referente a reuniao.';
COMMENT ON COLUMN calendario.cpc_decisoes.comunidade_id IS 'Identificador do registro relacionado por comunidade no contexto de cpc decisoes.';
COMMENT ON COLUMN calendario.cpc_decisoes.conselho_tipo IS 'Propriedade conselho tipo que caracteriza o registro de cpc decisoes.';
COMMENT ON COLUMN calendario.cpc_decisoes.status_homologacao IS 'Propriedade status homologacao que caracteriza o registro de cpc decisoes.';
COMMENT ON COLUMN calendario.cpc_decisoes.solicitante_id IS 'Identificador do registro relacionado por solicitante no contexto de cpc decisoes.';
COMMENT ON COLUMN calendario.cpc_decisoes.solicitante_nome IS 'Propriedade solicitante nome que caracteriza o registro de cpc decisoes.';
COMMENT ON COLUMN calendario.cpc_decisoes.homologador_id IS 'Identificador do registro relacionado por homologador no contexto de cpc decisoes.';
COMMENT ON COLUMN calendario.cpc_decisoes.motivo_veto IS 'Propriedade motivo veto que caracteriza o registro de cpc decisoes.';
COMMENT ON COLUMN calendario.cpc_decisoes.criado_em_utc IS 'Instante UTC de criação imutável do registro.';
COMMENT ON COLUMN calendario.cpc_decisoes.decidido_em_utc IS 'Instante UTC associado a decidido no ciclo de vida do registro.';
COMMENT ON COLUMN calendario.cpc_decisoes.version IS 'Versão usada para controle de concorrência otimista do registro.';
ALTER TABLE ONLY calendario.cpc_decisoes
    ADD CONSTRAINT cpc_decisoes_pkey PRIMARY KEY (id);
CREATE INDEX idx_cpc_decisoes_comunidade ON calendario.cpc_decisoes USING btree (comunidade_id);
CREATE INDEX idx_cpc_decisoes_status ON calendario.cpc_decisoes USING btree (status_homologacao);

