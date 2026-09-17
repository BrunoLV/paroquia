/**
 * MIGRAÇÃO INICIAL: V006__local.sql
 * CONTEXTO: Cria os locais físicos disponíveis para eventos e atividades paroquiais.
 * REGRA: Capacidade, disponibilidade e vínculo territorial orientam o uso da agenda.
 */

CREATE TABLE calendario.locais (
    id uuid NOT NULL,
    nome character varying(160) NOT NULL,
    tipo character varying(32) NOT NULL,
    capacidade integer,
    ativo boolean DEFAULT true NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    endereco character varying(500),
    caracteristicas text,
    grupo_paroquial_id uuid,
    cep character varying(9),
    logradouro character varying(150),
    numero character varying(20),
    complemento character varying(100),
    bairro character varying(100),
    cidade character varying(100),
    uf character varying(2),
    descricao text,
    capacidade_maxima integer,
    deleted_at timestamp with time zone,
    criado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.locais IS 'Dependências físicas e salas vinculadas a paróquia ou comunidades para celebrações e eventos.';
COMMENT ON COLUMN calendario.locais.cep IS 'Código de Endereçamento Postal formatado (00000-000).';
COMMENT ON COLUMN calendario.locais.logradouro IS 'Nome da rua, avenida ou praça do local físico.';
COMMENT ON COLUMN calendario.locais.numero IS 'Número predial do local físico.';
COMMENT ON COLUMN calendario.locais.complemento IS 'Complemento predial ou de localização.';
COMMENT ON COLUMN calendario.locais.bairro IS 'Bairro do local físico.';
COMMENT ON COLUMN calendario.locais.cidade IS 'Município do local físico.';
COMMENT ON COLUMN calendario.locais.uf IS 'Unidade Federativa (sigla de 2 caracteres).';
COMMENT ON COLUMN calendario.locais.descricao IS 'Descrição detalhada de uso, equipamentos disponíveis ou restrições.';
COMMENT ON COLUMN calendario.locais.capacidade_maxima IS 'Lotação máxima de pessoas permitida no espaço físico.';
COMMENT ON COLUMN calendario.locais.deleted_at IS 'Data/hora de exclusão lógica (soft delete). Registros excluídos não aparecem em listagens gerais.';
COMMENT ON COLUMN calendario.locais.criado_em_utc IS 'Timestamp UTC de criacao do registro gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.locais.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao do registro gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.locais.criado_por IS 'Identificador do usuario/sistema que criou o registro.';
COMMENT ON COLUMN calendario.locais.atualizado_por IS 'Identificador do usuario/sistema que realizou a ultima modificacao.';
COMMENT ON COLUMN calendario.locais.id IS 'Identificador único do registro de locais.';
COMMENT ON COLUMN calendario.locais.nome IS 'Informação textual de nome no contexto de locais.';
COMMENT ON COLUMN calendario.locais.tipo IS 'Propriedade tipo que caracteriza o registro de locais.';
COMMENT ON COLUMN calendario.locais.capacidade IS 'Propriedade capacidade que caracteriza o registro de locais.';
COMMENT ON COLUMN calendario.locais.ativo IS 'Indicador booleano de vigência operacional: ativo.';
COMMENT ON COLUMN calendario.locais.version IS 'Versão usada para controle de concorrência otimista do registro.';
COMMENT ON COLUMN calendario.locais.endereco IS 'Propriedade endereco que caracteriza o registro de locais.';
COMMENT ON COLUMN calendario.locais.caracteristicas IS 'Propriedade caracteristicas que caracteriza o registro de locais.';
COMMENT ON COLUMN calendario.locais.grupo_paroquial_id IS 'Identificador do registro relacionado por grupo paroquial no contexto de locais.';
ALTER TABLE ONLY calendario.locais
    ADD CONSTRAINT locais_pkey PRIMARY KEY (id);
ALTER TABLE ONLY calendario.locais
    ADD CONSTRAINT uk_locais_nome UNIQUE (nome);
CREATE INDEX idx_locais_ativo_deleted ON calendario.locais USING btree (ativo, deleted_at);
CREATE INDEX idx_locais_grupo_paroquial_deleted ON calendario.locais USING btree (grupo_paroquial_id, deleted_at);
ALTER TABLE ONLY calendario.locais
    ADD CONSTRAINT locais_grupo_paroquial_id_fkey FOREIGN KEY (grupo_paroquial_id) REFERENCES calendario.unidade_territorial(id);

