/**
 * MIGRAÇÃO INICIAL: V003__clero.sql
 * CONTEXTO: Cria o cadastro clerical, os encargos ministeriais e as solicitações de ajuste do clero.
 * REGRA: O histórico de encargos preserva a unidade territorial e o período de exercício.
 */

CREATE TABLE calendario.clerigos (
    id uuid NOT NULL,
    nome_civil character varying(2048) NOT NULL,
    nome_eclesiastico character varying(255) NOT NULL,
    data_nascimento character varying(2048) NOT NULL,
    pais_origem character varying(3) NOT NULL,
    cpf character varying(2048),
    rg character varying(2048),
    documento_estrangeiro character varying(2048),
    email character varying(2048) NOT NULL,
    celular character varying(2048) NOT NULL,
    telefone_fixo character varying(2048),
    whatsapp boolean DEFAULT false NOT NULL,
    endereco character varying(4096),
    grau_ordem character varying(32) NOT NULL,
    diocese_incardinacao character varying(255) NOT NULL,
    data_ordenacao_diaconal date,
    data_ordenacao_presbiteral date,
    data_ordenacao_episcopal date,
    ativo boolean DEFAULT true NOT NULL,
    deleted_at timestamp with time zone,
    criado_em_utc timestamp with time zone NOT NULL,
    atualizado_em_utc timestamp with time zone NOT NULL,
    criado_por character varying(255) NOT NULL,
    atualizado_por character varying(255) NOT NULL,
    version bigint DEFAULT 0 NOT NULL
);
COMMENT ON TABLE calendario.clerigos IS 'Cadastro canônico e administrativo de ministros ordenados vinculados à paróquia.';
COMMENT ON COLUMN calendario.clerigos.id IS 'Identificador único do registro de clerigos.';
COMMENT ON COLUMN calendario.clerigos.nome_civil IS 'Propriedade nome civil que caracteriza o registro de clerigos.';
COMMENT ON COLUMN calendario.clerigos.nome_eclesiastico IS 'Propriedade nome eclesiastico que caracteriza o registro de clerigos.';
COMMENT ON COLUMN calendario.clerigos.data_nascimento IS 'Data de negócio referente a nascimento.';
COMMENT ON COLUMN calendario.clerigos.pais_origem IS 'Propriedade pais origem que caracteriza o registro de clerigos.';
COMMENT ON COLUMN calendario.clerigos.cpf IS 'Propriedade cpf que caracteriza o registro de clerigos.';
COMMENT ON COLUMN calendario.clerigos.rg IS 'Propriedade rg que caracteriza o registro de clerigos.';
COMMENT ON COLUMN calendario.clerigos.documento_estrangeiro IS 'Propriedade documento estrangeiro que caracteriza o registro de clerigos.';
COMMENT ON COLUMN calendario.clerigos.email IS 'Propriedade email que caracteriza o registro de clerigos.';
COMMENT ON COLUMN calendario.clerigos.celular IS 'Propriedade celular que caracteriza o registro de clerigos.';
COMMENT ON COLUMN calendario.clerigos.telefone_fixo IS 'Propriedade telefone fixo que caracteriza o registro de clerigos.';
COMMENT ON COLUMN calendario.clerigos.whatsapp IS 'Propriedade whatsapp que caracteriza o registro de clerigos.';
COMMENT ON COLUMN calendario.clerigos.endereco IS 'Propriedade endereco que caracteriza o registro de clerigos.';
COMMENT ON COLUMN calendario.clerigos.grau_ordem IS 'Propriedade grau ordem que caracteriza o registro de clerigos.';
COMMENT ON COLUMN calendario.clerigos.diocese_incardinacao IS 'Propriedade diocese incardinacao que caracteriza o registro de clerigos.';
COMMENT ON COLUMN calendario.clerigos.data_ordenacao_diaconal IS 'Data de negócio referente a ordenacao diaconal.';
COMMENT ON COLUMN calendario.clerigos.data_ordenacao_presbiteral IS 'Data de negócio referente a ordenacao presbiteral.';
COMMENT ON COLUMN calendario.clerigos.data_ordenacao_episcopal IS 'Data de negócio referente a ordenacao episcopal.';
COMMENT ON COLUMN calendario.clerigos.ativo IS 'Indicador booleano de vigência operacional: ativo.';
COMMENT ON COLUMN calendario.clerigos.deleted_at IS 'Propriedade deleted at que caracteriza o registro de clerigos.';
COMMENT ON COLUMN calendario.clerigos.criado_em_utc IS 'Instante UTC de criação imutável do registro.';
COMMENT ON COLUMN calendario.clerigos.atualizado_em_utc IS 'Instante UTC da última atualização do registro.';
COMMENT ON COLUMN calendario.clerigos.criado_por IS 'Identificador do responsável por criado.';
COMMENT ON COLUMN calendario.clerigos.atualizado_por IS 'Identificador do responsável por atualizado.';
COMMENT ON COLUMN calendario.clerigos.version IS 'Versão usada para controle de concorrência otimista do registro.';
ALTER TABLE ONLY calendario.clerigos
    ADD CONSTRAINT clerigos_pkey PRIMARY KEY (id);
CREATE INDEX idx_clerigos_ativos ON calendario.clerigos USING btree (ativo, nome_eclesiastico);

CREATE TABLE calendario.encargos_clericais (
    id uuid NOT NULL,
    clerigo_id uuid NOT NULL,
    unidade_territorial_id uuid NOT NULL,
    funcao character varying(32) NOT NULL,
    atual boolean DEFAULT true NOT NULL,
    criado_em_utc timestamp with time zone NOT NULL,
    encerrado_em_utc timestamp with time zone,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.encargos_clericais IS 'Encargos ministeriais exercidos por clérigos em unidades territoriais, com vigência.';
COMMENT ON COLUMN calendario.encargos_clericais.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao do encargo clerical.';
COMMENT ON COLUMN calendario.encargos_clericais.criado_por IS 'Identificador do usuario/sistema que registrou o encargo clerical.';
COMMENT ON COLUMN calendario.encargos_clericais.atualizado_por IS 'Identificador do usuario que atualizou o encargo clerical.';
COMMENT ON COLUMN calendario.encargos_clericais.id IS 'Identificador único do registro de encargos clericais.';
COMMENT ON COLUMN calendario.encargos_clericais.clerigo_id IS 'Identificador do registro relacionado por clerigo no contexto de encargos clericais.';
COMMENT ON COLUMN calendario.encargos_clericais.unidade_territorial_id IS 'Identificador do registro relacionado por unidade territorial no contexto de encargos clericais.';
COMMENT ON COLUMN calendario.encargos_clericais.funcao IS 'Propriedade funcao que caracteriza o registro de encargos clericais.';
COMMENT ON COLUMN calendario.encargos_clericais.atual IS 'Indicador booleano de vigência operacional: atual.';
COMMENT ON COLUMN calendario.encargos_clericais.criado_em_utc IS 'Instante UTC de criação imutável do registro.';
COMMENT ON COLUMN calendario.encargos_clericais.encerrado_em_utc IS 'Instante UTC associado a encerrado no ciclo de vida do registro.';
ALTER TABLE ONLY calendario.encargos_clericais
    ADD CONSTRAINT encargos_clericais_pkey PRIMARY KEY (id);
CREATE INDEX idx_encargo_clerical_funcao_atual ON calendario.encargos_clericais USING btree (funcao, atual);
ALTER TABLE ONLY calendario.encargos_clericais
    ADD CONSTRAINT encargos_clericais_clerigo_id_fkey FOREIGN KEY (clerigo_id) REFERENCES calendario.clerigos(id);
ALTER TABLE ONLY calendario.encargos_clericais
    ADD CONSTRAINT encargos_clericais_unidade_territorial_id_fkey FOREIGN KEY (unidade_territorial_id) REFERENCES calendario.unidade_territorial(id);

CREATE TABLE calendario.solicitacoes_ajuste_clerical (
    id uuid NOT NULL,
    usuario_solicitante_id uuid NOT NULL,
    clerigo_id uuid NOT NULL,
    email character varying(2048) NOT NULL,
    celular character varying(2048) NOT NULL,
    telefone_fixo character varying(2048),
    endereco character varying(4096) NOT NULL,
    status character varying(16) DEFAULT 'PENDENTE'::character varying NOT NULL,
    justificativa_analise character varying(2048),
    analisado_por_usuario_id uuid,
    analisado_em_utc timestamp with time zone,
    criado_em_utc timestamp with time zone NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.solicitacoes_ajuste_clerical IS 'Solicitações auditáveis de correção de dados canônicos e administrativos do clero.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_clerical.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao da solicitacao de ajuste clerical.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_clerical.criado_por IS 'Identificador do solicitante/sistema.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_clerical.atualizado_por IS 'Identificador do revisor/sistema.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_clerical.id IS 'Identificador único do registro de solicitacoes ajuste clerical.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_clerical.usuario_solicitante_id IS 'Identificador do registro relacionado por usuario solicitante no contexto de solicitacoes ajuste clerical.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_clerical.clerigo_id IS 'Identificador do registro relacionado por clerigo no contexto de solicitacoes ajuste clerical.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_clerical.email IS 'Propriedade email que caracteriza o registro de solicitacoes ajuste clerical.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_clerical.celular IS 'Propriedade celular que caracteriza o registro de solicitacoes ajuste clerical.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_clerical.telefone_fixo IS 'Propriedade telefone fixo que caracteriza o registro de solicitacoes ajuste clerical.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_clerical.endereco IS 'Propriedade endereco que caracteriza o registro de solicitacoes ajuste clerical.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_clerical.status IS 'Estado atual do ciclo de vida e das transições permitidas pelo domínio.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_clerical.justificativa_analise IS 'Propriedade justificativa analise que caracteriza o registro de solicitacoes ajuste clerical.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_clerical.analisado_por_usuario_id IS 'Identificador do registro relacionado por analisado por usuario no contexto de solicitacoes ajuste clerical.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_clerical.analisado_em_utc IS 'Instante UTC associado a analisado no ciclo de vida do registro.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_clerical.criado_em_utc IS 'Instante UTC de criação imutável do registro.';
ALTER TABLE ONLY calendario.solicitacoes_ajuste_clerical
    ADD CONSTRAINT solicitacoes_ajuste_clerical_pkey PRIMARY KEY (id);
CREATE INDEX idx_solicitacoes_ajuste_clerical_pendentes ON calendario.solicitacoes_ajuste_clerical USING btree (status, criado_em_utc);
ALTER TABLE ONLY calendario.solicitacoes_ajuste_clerical
    ADD CONSTRAINT solicitacoes_ajuste_clerical_clerigo_id_fkey FOREIGN KEY (clerigo_id) REFERENCES calendario.clerigos(id);

