/**
 * MIGRAÇÃO INICIAL: V001__organizacao.sql
 * CONTEXTO: Cria a estrutura de unidades territoriais, grupos paroquiais e catálogo oficial de pastorais.
 * REGRA: Unidades territoriais representam a presença física; grupos paroquiais representam a atuação pastoral.
 */

CREATE SCHEMA IF NOT EXISTS calendario;

CREATE TABLE calendario.pastorais_movimentos_oficiais (
    id uuid NOT NULL,
    nome character varying(150) NOT NULL,
    tipo character varying(50) NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    criado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.pastorais_movimentos_oficiais IS 'Catálogo diocesano de pastorais e movimentos que pode referenciar grupos paroquiais locais.';
COMMENT ON COLUMN calendario.pastorais_movimentos_oficiais.criado_em_utc IS 'Timestamp UTC de criacao do catalogo oficial.';
COMMENT ON COLUMN calendario.pastorais_movimentos_oficiais.atualizado_em_utc IS 'Timestamp UTC de atualizacao do catalogo.';
COMMENT ON COLUMN calendario.pastorais_movimentos_oficiais.criado_por IS 'Identificador do usuario que registrou o movimento.';
COMMENT ON COLUMN calendario.pastorais_movimentos_oficiais.atualizado_por IS 'Identificador do usuario que modificou o movimento.';
COMMENT ON COLUMN calendario.pastorais_movimentos_oficiais.id IS 'Identificador único do registro de pastorais movimentos oficiais.';
COMMENT ON COLUMN calendario.pastorais_movimentos_oficiais.nome IS 'Informação textual de nome no contexto de pastorais movimentos oficiais.';
COMMENT ON COLUMN calendario.pastorais_movimentos_oficiais.tipo IS 'Propriedade tipo que caracteriza o registro de pastorais movimentos oficiais.';
COMMENT ON COLUMN calendario.pastorais_movimentos_oficiais.version IS 'Versão usada para controle de concorrência otimista do registro.';
ALTER TABLE ONLY calendario.pastorais_movimentos_oficiais
    ADD CONSTRAINT pastorais_movimentos_oficiais_nome_key UNIQUE (nome);
ALTER TABLE ONLY calendario.pastorais_movimentos_oficiais
    ADD CONSTRAINT pastorais_movimentos_oficiais_pkey PRIMARY KEY (id);

CREATE TABLE calendario.unidade_territorial (
    id uuid NOT NULL,
    nome character varying(255) NOT NULL,
    tipo character varying(32) NOT NULL,
    contato character varying(255),
    ativo boolean DEFAULT true NOT NULL,
    parent_id uuid,
    version bigint DEFAULT 0 NOT NULL,
    criado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.unidade_territorial IS 'Divisões territoriais da paróquia, como matriz, comunidades e clero; são distintas dos grupos de atuação pastoral.';
COMMENT ON COLUMN calendario.unidade_territorial.id IS 'Identificador primário da unidade territorial (UUID).';
COMMENT ON COLUMN calendario.unidade_territorial.nome IS 'Nome da unidade territorial.';
COMMENT ON COLUMN calendario.unidade_territorial.tipo IS 'Tipo da unidade territorial (ex.: MATRIZ, COMUNIDADE, CLERO).';
COMMENT ON COLUMN calendario.unidade_territorial.contato IS 'Informação de contato da unidade.';
COMMENT ON COLUMN calendario.unidade_territorial.ativo IS 'Indica se a unidade está ativa.';
COMMENT ON COLUMN calendario.unidade_territorial.parent_id IS 'Unidade territorial superior na hierarquia, quando aplicável.';
COMMENT ON COLUMN calendario.unidade_territorial.version IS 'Controle de versão para concorrência otimista (JPA).';
COMMENT ON COLUMN calendario.unidade_territorial.criado_em_utc IS 'Timestamp UTC de criacao da unidade territorial.';
COMMENT ON COLUMN calendario.unidade_territorial.atualizado_em_utc IS 'Timestamp UTC de atualizacao da unidade territorial.';
COMMENT ON COLUMN calendario.unidade_territorial.criado_por IS 'Identificador do usuario que cadastrou a unidade.';
COMMENT ON COLUMN calendario.unidade_territorial.atualizado_por IS 'Identificador do usuario que modificou a unidade.';
ALTER TABLE ONLY calendario.unidade_territorial
    ADD CONSTRAINT unidade_territorial_nome_key UNIQUE (nome);
ALTER TABLE ONLY calendario.unidade_territorial
    ADD CONSTRAINT unidade_territorial_pkey PRIMARY KEY (id);

CREATE TABLE calendario.grupo_paroquial (
    id uuid NOT NULL,
    nome character varying(255) NOT NULL,
    tipo character varying(32) DEFAULT 'OUTRO'::character varying NOT NULL,
    contato character varying(255),
    ativo boolean DEFAULT true NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    coordenador character varying(255),
    vice_coordenador character varying(255),
    pastoral_oficial_id uuid,
    setor_pastoral character varying(64),
    parent_id uuid,
    criado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.grupo_paroquial IS 'Pastorais, movimentos, conselhos e ministérios da paróquia; são grupos de atuação distintos das unidades territoriais.';
COMMENT ON COLUMN calendario.grupo_paroquial.id IS 'Identificador primário do grupo paroquial (UUID).';
COMMENT ON COLUMN calendario.grupo_paroquial.nome IS 'Nome do grupo paroquial.';
COMMENT ON COLUMN calendario.grupo_paroquial.tipo IS 'Tipo do grupo (PASTORAL, MOVIMENTO, CONSELHO, MINISTERIO).';
COMMENT ON COLUMN calendario.grupo_paroquial.contato IS 'Informação de contato do grupo.';
COMMENT ON COLUMN calendario.grupo_paroquial.ativo IS 'Indica se o grupo está ativo.';
COMMENT ON COLUMN calendario.grupo_paroquial.version IS 'Controle de versão para concorrência otimista (JPA).';
COMMENT ON COLUMN calendario.grupo_paroquial.parent_id IS 'Grupo paroquial ou unidade territorial superior na hierarquia (referência polimórfica validada em nível de aplicação).';
COMMENT ON COLUMN calendario.grupo_paroquial.criado_em_utc IS 'Timestamp UTC de criacao do grupo paroquial.';
COMMENT ON COLUMN calendario.grupo_paroquial.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao do grupo paroquial.';
COMMENT ON COLUMN calendario.grupo_paroquial.criado_por IS 'Identificador do usuario que cadastrou o grupo.';
COMMENT ON COLUMN calendario.grupo_paroquial.atualizado_por IS 'Identificador do usuario que modificou o grupo.';
COMMENT ON COLUMN calendario.grupo_paroquial.coordenador IS 'Propriedade coordenador que caracteriza o registro de grupo paroquial.';
COMMENT ON COLUMN calendario.grupo_paroquial.vice_coordenador IS 'Propriedade vice coordenador que caracteriza o registro de grupo paroquial.';
COMMENT ON COLUMN calendario.grupo_paroquial.pastoral_oficial_id IS 'Identificador do registro relacionado por pastoral oficial no contexto de grupo paroquial.';
COMMENT ON COLUMN calendario.grupo_paroquial.setor_pastoral IS 'Propriedade setor pastoral que caracteriza o registro de grupo paroquial.';
ALTER TABLE ONLY calendario.grupo_paroquial
    ADD CONSTRAINT organizacoes_pkey PRIMARY KEY (id);
ALTER TABLE ONLY calendario.grupo_paroquial
    ADD CONSTRAINT uk_organizacoes_nome UNIQUE (nome);
ALTER TABLE ONLY calendario.grupo_paroquial
    ADD CONSTRAINT fk_organizacoes_pastoral_oficial FOREIGN KEY (pastoral_oficial_id) REFERENCES calendario.pastorais_movimentos_oficiais(id);

