/**
 * MIGRAÇÃO INICIAL: V012__patrimonio.sql
 * CONTEXTO: Cria ativos patrimoniais, numeração anual, manutenções e observações de conservação.
 * REGRA: A identificação patrimonial é única por ano e o histórico de manutenção é auditável.
 */

CREATE TABLE calendario.ativos_patrimoniais (
    id uuid NOT NULL,
    nome character varying(200) NOT NULL,
    codigo_tombamento character varying(32) NOT NULL,
    categoria character varying(32) NOT NULL,
    status character varying(32) DEFAULT 'DISPONIVEL'::character varying NOT NULL,
    is_reservavel boolean DEFAULT false NOT NULL,
    grupo_paroquial_id uuid NOT NULL,
    data_aquisicao date NOT NULL,
    valor_aquisicao numeric(12,2) NOT NULL,
    criado_em_utc timestamp with time zone NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    descricao text,
    cadastrado_por_id uuid,
    deleted_at timestamp with time zone,
    atualizado_em_utc timestamp with time zone NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    CONSTRAINT chk_ativos_patrimoniais_status CHECK (((status)::text = ANY ((ARRAY['DISPONIVEL'::character varying, 'RESERVADO'::character varying, 'EM_MANUTENCAO'::character varying])::text[]))),
    CONSTRAINT chk_ativos_patrimoniais_valor_aquisicao CHECK ((valor_aquisicao >= (0)::numeric))
);
COMMENT ON TABLE calendario.ativos_patrimoniais IS 'Bens patrimoniais móveis da paróquia (equipamentos, veículos etc.) sujeitos a tombamento, alocação e reserva.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.id IS 'Identificador primário do ativo patrimonial (UUID).';
COMMENT ON COLUMN calendario.ativos_patrimoniais.nome IS 'Nome/descrição curta do ativo.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.codigo_tombamento IS 'Código único de tombamento patrimonial do ativo.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.categoria IS 'Tipo/categoria do ativo (exibido como "Tipo" na UI).';
COMMENT ON COLUMN calendario.ativos_patrimoniais.status IS 'Estado atual do ativo (ex.: disponível, em manutenção, baixado).';
COMMENT ON COLUMN calendario.ativos_patrimoniais.is_reservavel IS 'Indica se o ativo pode ser reservado via calendario.reservas.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.grupo_paroquial_id IS 'Grupo paroquial responsável/proprietário do ativo.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.data_aquisicao IS 'Data de aquisição do ativo.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.valor_aquisicao IS 'Valor de aquisição do ativo, em reais.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.criado_em_utc IS 'Timestamp UTC de criacao do registro gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.version IS 'Controle de versão para concorrência otimista (JPA).';
COMMENT ON COLUMN calendario.ativos_patrimoniais.descricao IS 'Descrição detalhada do ativo.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.cadastrado_por_id IS 'Usuário autor do cadastro (auditoria); nulo apenas para linhas anteriores à V083.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.deleted_at IS 'Data de exclusão lógica (soft delete).';
COMMENT ON COLUMN calendario.ativos_patrimoniais.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.criado_por IS 'Identificador do usuario/sistema que cadastrou o ativo patrimonial.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.atualizado_por IS 'Identificador do usuario/sistema que realizou a ultima modificacao.';
ALTER TABLE ONLY calendario.ativos_patrimoniais
    ADD CONSTRAINT ativos_patrimoniais_pkey PRIMARY KEY (id);
ALTER TABLE ONLY calendario.ativos_patrimoniais
    ADD CONSTRAINT uk_ativos_patrimoniais_numero_tombamento UNIQUE (codigo_tombamento);
CREATE INDEX idx_ativos_patrimoniais_grupo_deleted ON calendario.ativos_patrimoniais USING btree (grupo_paroquial_id, deleted_at);
CREATE INDEX idx_ativos_patrimoniais_organizacao ON calendario.ativos_patrimoniais USING btree (grupo_paroquial_id);
CREATE INDEX idx_ativos_patrimoniais_status ON calendario.ativos_patrimoniais USING btree (status);

CREATE TABLE calendario.patrimonio_numeracao_anual (
    ano integer NOT NULL,
    ultimo_numero integer DEFAULT 0 NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    criado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.patrimonio_numeracao_anual IS 'Controle sequencial anual utilizado na geração de tombamentos patrimoniais.';
COMMENT ON COLUMN calendario.patrimonio_numeracao_anual.criado_em_utc IS 'Timestamp UTC de inicializacao da sequencia anual de patrimonio.';
COMMENT ON COLUMN calendario.patrimonio_numeracao_anual.atualizado_em_utc IS 'Timestamp UTC da ultima alteracao da numeracao.';
COMMENT ON COLUMN calendario.patrimonio_numeracao_anual.criado_por IS 'Identificador do sistema.';
COMMENT ON COLUMN calendario.patrimonio_numeracao_anual.atualizado_por IS 'Identificador do sistema.';
COMMENT ON COLUMN calendario.patrimonio_numeracao_anual.ano IS 'Propriedade ano que caracteriza o registro de patrimonio numeracao anual.';
COMMENT ON COLUMN calendario.patrimonio_numeracao_anual.ultimo_numero IS 'Propriedade ultimo numero que caracteriza o registro de patrimonio numeracao anual.';
COMMENT ON COLUMN calendario.patrimonio_numeracao_anual.version IS 'Versão usada para controle de concorrência otimista do registro.';
ALTER TABLE ONLY calendario.patrimonio_numeracao_anual
    ADD CONSTRAINT patrimonio_numeracao_anual_pkey PRIMARY KEY (ano);

CREATE TABLE calendario.ativo_manutencoes (
    id uuid NOT NULL,
    ativo_id uuid NOT NULL,
    descricao_reparo text NOT NULL,
    data_inicio timestamp with time zone NOT NULL,
    data_fim_prevista timestamp with time zone NOT NULL,
    data_fim_real timestamp with time zone,
    registrado_por_id uuid NOT NULL,
    criado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    CONSTRAINT chk_ativo_manutencoes_periodo CHECK ((data_fim_prevista > data_inicio))
);
COMMENT ON TABLE calendario.ativo_manutencoes IS 'Períodos de manutenção (conserto, revisão, reparo) que retiram um ativo patrimonial de circulação e bloqueiam o calendário de reservas no intervalo.';
COMMENT ON COLUMN calendario.ativo_manutencoes.id IS 'Identificador primário único da manutenção (UUID).';
COMMENT ON COLUMN calendario.ativo_manutencoes.ativo_id IS 'Ativo patrimonial sob manutenção.';
COMMENT ON COLUMN calendario.ativo_manutencoes.descricao_reparo IS 'Relatório do problema e do serviço a ser executado.';
COMMENT ON COLUMN calendario.ativo_manutencoes.data_inicio IS 'Início do período de indisponibilidade.';
COMMENT ON COLUMN calendario.ativo_manutencoes.data_fim_prevista IS 'Previsão de retorno do bem; sempre posterior a data_inicio.';
COMMENT ON COLUMN calendario.ativo_manutencoes.data_fim_real IS 'Registro efetivo da conclusão do reparo; nulo enquanto a manutenção está em aberto.';
COMMENT ON COLUMN calendario.ativo_manutencoes.registrado_por_id IS 'Usuário que enviou o item para manutenção (auditoria).';
COMMENT ON COLUMN calendario.ativo_manutencoes.criado_em_utc IS 'Timestamp UTC de criacao do registro gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.ativo_manutencoes.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao da manutencao de ativo.';
COMMENT ON COLUMN calendario.ativo_manutencoes.criado_por IS 'Identificador do usuario que abriu a manutencao.';
COMMENT ON COLUMN calendario.ativo_manutencoes.atualizado_por IS 'Identificador do usuario que atualizou a manutencao.';
COMMENT ON COLUMN calendario.ativo_manutencoes.version IS 'Versão usada para controle de concorrência otimista do registro.';
ALTER TABLE ONLY calendario.ativo_manutencoes
    ADD CONSTRAINT ativo_manutencoes_pkey PRIMARY KEY (id);
CREATE INDEX idx_ativo_manutencoes_abertas ON calendario.ativo_manutencoes USING btree (ativo_id, data_fim_real);
CREATE INDEX idx_ativo_manutencoes_ativo ON calendario.ativo_manutencoes USING btree (ativo_id, data_inicio);
ALTER TABLE ONLY calendario.ativo_manutencoes
    ADD CONSTRAINT fk_ativo_manutencoes_ativo FOREIGN KEY (ativo_id) REFERENCES calendario.ativos_patrimoniais(id);

CREATE TABLE calendario.observacoes_ativo_patrimonial (
    id uuid NOT NULL,
    ativo_patrimonial_id uuid NOT NULL,
    usuario_id uuid NOT NULL,
    tipo character varying(64) NOT NULL,
    conteudo character varying(4000) NOT NULL,
    criado_em_utc timestamp with time zone NOT NULL,
    removida boolean DEFAULT false NOT NULL,
    removida_em_utc timestamp with time zone,
    removida_por_usuario_id uuid,
    version bigint DEFAULT 0 NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.observacoes_ativo_patrimonial IS 'Notas auditáveis sobre conservação, manutenção e uso de ativos patrimoniais.';
COMMENT ON COLUMN calendario.observacoes_ativo_patrimonial.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao da observacao patrimonial.';
COMMENT ON COLUMN calendario.observacoes_ativo_patrimonial.criado_por IS 'Identificador do autor da observacao patrimonial.';
COMMENT ON COLUMN calendario.observacoes_ativo_patrimonial.atualizado_por IS 'Identificador do usuario que atualizou a observacao patrimonial.';
COMMENT ON COLUMN calendario.observacoes_ativo_patrimonial.id IS 'Identificador único do registro de observacoes ativo patrimonial.';
COMMENT ON COLUMN calendario.observacoes_ativo_patrimonial.ativo_patrimonial_id IS 'Identificador do registro relacionado por ativo patrimonial no contexto de observacoes ativo patrimonial.';
COMMENT ON COLUMN calendario.observacoes_ativo_patrimonial.usuario_id IS 'Identificador do registro relacionado por usuario no contexto de observacoes ativo patrimonial.';
COMMENT ON COLUMN calendario.observacoes_ativo_patrimonial.tipo IS 'Propriedade tipo que caracteriza o registro de observacoes ativo patrimonial.';
COMMENT ON COLUMN calendario.observacoes_ativo_patrimonial.conteudo IS 'Informação textual de conteudo no contexto de observacoes ativo patrimonial.';
COMMENT ON COLUMN calendario.observacoes_ativo_patrimonial.criado_em_utc IS 'Instante UTC de criação imutável do registro.';
COMMENT ON COLUMN calendario.observacoes_ativo_patrimonial.removida IS 'Indicador booleano de vigência operacional: removida.';
COMMENT ON COLUMN calendario.observacoes_ativo_patrimonial.removida_em_utc IS 'Instante UTC associado a removida no ciclo de vida do registro.';
COMMENT ON COLUMN calendario.observacoes_ativo_patrimonial.removida_por_usuario_id IS 'Identificador do registro relacionado por removida por usuario no contexto de observacoes ativo patrimonial.';
COMMENT ON COLUMN calendario.observacoes_ativo_patrimonial.version IS 'Versão usada para controle de concorrência otimista do registro.';
ALTER TABLE ONLY calendario.observacoes_ativo_patrimonial
    ADD CONSTRAINT observacoes_ativo_patrimonial_pkey PRIMARY KEY (id);
CREATE INDEX idx_observacoes_ativo_patrimonial_ativo ON calendario.observacoes_ativo_patrimonial USING btree (ativo_patrimonial_id);

