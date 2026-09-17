/**
 * MIGRAÇÃO INICIAL: V007__governanca.sql
 * CONTEXTO: Cria os anos paroquiais que organizam planejamento, execução e encerramento das atividades.
 * REGRA: O status do ano paroquial controla as operações permitidas durante seu ciclo.
 */

CREATE TABLE calendario.anos_paroquiais (
    ano integer NOT NULL,
    status character varying(32) NOT NULL,
    data_fechamento_utc timestamp with time zone,
    atualizado_em_utc timestamp with time zone DEFAULT CURRENT_TIMESTAMP,
    criado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.anos_paroquiais IS 'Ciclos anuais de governança paroquial que controlam o planejamento e encerramento de atividades.';
COMMENT ON COLUMN calendario.anos_paroquiais.atualizado_em_utc IS 'Timestamp UTC da ultima alteracao no ano paroquial.';
COMMENT ON COLUMN calendario.anos_paroquiais.criado_em_utc IS 'Timestamp UTC de abertura do ano paroquial.';
COMMENT ON COLUMN calendario.anos_paroquiais.criado_por IS 'Identificador do usuario que abriu o ano.';
COMMENT ON COLUMN calendario.anos_paroquiais.atualizado_por IS 'Identificador do usuario que modificou o ano.';
COMMENT ON COLUMN calendario.anos_paroquiais.ano IS 'Propriedade ano que caracteriza o registro de anos paroquiais.';
COMMENT ON COLUMN calendario.anos_paroquiais.status IS 'Estado atual do ciclo de vida e das transições permitidas pelo domínio.';
COMMENT ON COLUMN calendario.anos_paroquiais.data_fechamento_utc IS 'Data de negócio referente a fechamento utc.';
ALTER TABLE ONLY calendario.anos_paroquiais
    ADD CONSTRAINT anos_paroquiais_pkey PRIMARY KEY (ano);

