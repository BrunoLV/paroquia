/**
 * MIGRAÇÃO INICIAL: V009__calendario.sql
 * CONTEXTO: Cria calendário paroquial, ciclos litúrgicos, eventos recorrentes e notas colaborativas.
 * REGRA: Referências litúrgicas permanentes orientam conflitos, prioridade e planejamento da agenda.
 */

CREATE TABLE calendario.paroquia_configuracao (
    id uuid NOT NULL,
    nome character varying(255) NOT NULL,
    diocese character varying(255) NOT NULL,
    endereco character varying(500) NOT NULL,
    logo_bytes bytea,
    cor_primaria_hex character varying(7) NOT NULL,
    cor_secundaria_hex character varying(7) NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    criado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.paroquia_configuracao IS 'Configuração institucional única da paróquia usada em documentos e comunicações oficiais.';
COMMENT ON COLUMN calendario.paroquia_configuracao.criado_em_utc IS 'Timestamp UTC da configuracao inicial da paroquia.';
COMMENT ON COLUMN calendario.paroquia_configuracao.atualizado_em_utc IS 'Timestamp UTC da ultima alteracao das configuracoes.';
COMMENT ON COLUMN calendario.paroquia_configuracao.criado_por IS 'Identificador do usuario que configurou.';
COMMENT ON COLUMN calendario.paroquia_configuracao.atualizado_por IS 'Identificador do usuario que modificou.';
COMMENT ON COLUMN calendario.paroquia_configuracao.id IS 'Identificador único do registro de paroquia configuracao.';
COMMENT ON COLUMN calendario.paroquia_configuracao.nome IS 'Informação textual de nome no contexto de paroquia configuracao.';
COMMENT ON COLUMN calendario.paroquia_configuracao.diocese IS 'Propriedade diocese que caracteriza o registro de paroquia configuracao.';
COMMENT ON COLUMN calendario.paroquia_configuracao.endereco IS 'Propriedade endereco que caracteriza o registro de paroquia configuracao.';
COMMENT ON COLUMN calendario.paroquia_configuracao.logo_bytes IS 'Propriedade logo bytes que caracteriza o registro de paroquia configuracao.';
COMMENT ON COLUMN calendario.paroquia_configuracao.cor_primaria_hex IS 'Propriedade cor primaria hex que caracteriza o registro de paroquia configuracao.';
COMMENT ON COLUMN calendario.paroquia_configuracao.cor_secundaria_hex IS 'Propriedade cor secundaria hex que caracteriza o registro de paroquia configuracao.';
COMMENT ON COLUMN calendario.paroquia_configuracao.version IS 'Versão usada para controle de concorrência otimista do registro.';
ALTER TABLE ONLY calendario.paroquia_configuracao
    ADD CONSTRAINT paroquia_configuracao_pkey PRIMARY KEY (id);

CREATE TABLE calendario.anos_liturgicos (
    ano integer NOT NULL,
    ciclo_letra character(1) NOT NULL,
    data_inicio_advento timestamp with time zone NOT NULL,
    data_fim_comum timestamp with time zone NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    criado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.anos_liturgicos IS 'Calendário anual do ciclo litúrgico que referencia eventos e períodos pastorais.';
COMMENT ON COLUMN calendario.anos_liturgicos.criado_em_utc IS 'Timestamp UTC de criacao do ano liturgico.';
COMMENT ON COLUMN calendario.anos_liturgicos.criado_por IS 'Identificador do usuario que cadastrou o ano liturgico.';
COMMENT ON COLUMN calendario.anos_liturgicos.atualizado_por IS 'Identificador do usuario que modificou o ano liturgico.';
COMMENT ON COLUMN calendario.anos_liturgicos.ano IS 'Propriedade ano que caracteriza o registro de anos liturgicos.';
COMMENT ON COLUMN calendario.anos_liturgicos.ciclo_letra IS 'Propriedade ciclo letra que caracteriza o registro de anos liturgicos.';
COMMENT ON COLUMN calendario.anos_liturgicos.data_inicio_advento IS 'Data de negócio referente a inicio advento.';
COMMENT ON COLUMN calendario.anos_liturgicos.data_fim_comum IS 'Data de negócio referente a fim comum.';
COMMENT ON COLUMN calendario.anos_liturgicos.atualizado_em_utc IS 'Instante UTC da última atualização do registro.';
ALTER TABLE ONLY calendario.anos_liturgicos
    ADD CONSTRAINT anos_liturgicos_pkey PRIMARY KEY (ano);

CREATE TABLE calendario.tb_ciclo_liturgico (
    id uuid NOT NULL,
    nome character varying(120) NOT NULL,
    tipo_ciclo character varying(40) NOT NULL,
    cor_liturgica character varying(30) NOT NULL,
    mes_inicio integer NOT NULL,
    dia_inicio integer NOT NULL,
    mes_fim integer NOT NULL,
    dia_fim integer NOT NULL,
    fator_demanda double precision DEFAULT 1.0 NOT NULL,
    descricao character varying(500),
    criado_em_utc timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    ano_liturgico_ref integer,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.tb_ciclo_liturgico IS 'Períodos do calendário litúrgico com cor e fator de demanda para o planejamento da agenda.';
COMMENT ON COLUMN calendario.tb_ciclo_liturgico.criado_em_utc IS 'Timestamp UTC de criacao do ciclo liturgico.';
COMMENT ON COLUMN calendario.tb_ciclo_liturgico.atualizado_em_utc IS 'Timestamp UTC de modificacao do ciclo liturgico.';
COMMENT ON COLUMN calendario.tb_ciclo_liturgico.criado_por IS 'Identificador do usuario que cadastrou o ciclo.';
COMMENT ON COLUMN calendario.tb_ciclo_liturgico.atualizado_por IS 'Identificador do usuario que atualizou o ciclo.';
COMMENT ON COLUMN calendario.tb_ciclo_liturgico.id IS 'Identificador único do registro de tb ciclo liturgico.';
COMMENT ON COLUMN calendario.tb_ciclo_liturgico.nome IS 'Informação textual de nome no contexto de tb ciclo liturgico.';
COMMENT ON COLUMN calendario.tb_ciclo_liturgico.tipo_ciclo IS 'Propriedade tipo ciclo que caracteriza o registro de tb ciclo liturgico.';
COMMENT ON COLUMN calendario.tb_ciclo_liturgico.cor_liturgica IS 'Propriedade cor liturgica que caracteriza o registro de tb ciclo liturgico.';
COMMENT ON COLUMN calendario.tb_ciclo_liturgico.mes_inicio IS 'Propriedade mes inicio que caracteriza o registro de tb ciclo liturgico.';
COMMENT ON COLUMN calendario.tb_ciclo_liturgico.dia_inicio IS 'Propriedade dia inicio que caracteriza o registro de tb ciclo liturgico.';
COMMENT ON COLUMN calendario.tb_ciclo_liturgico.mes_fim IS 'Propriedade mes fim que caracteriza o registro de tb ciclo liturgico.';
COMMENT ON COLUMN calendario.tb_ciclo_liturgico.dia_fim IS 'Propriedade dia fim que caracteriza o registro de tb ciclo liturgico.';
COMMENT ON COLUMN calendario.tb_ciclo_liturgico.fator_demanda IS 'Propriedade fator demanda que caracteriza o registro de tb ciclo liturgico.';
COMMENT ON COLUMN calendario.tb_ciclo_liturgico.descricao IS 'Informação textual de descricao no contexto de tb ciclo liturgico.';
COMMENT ON COLUMN calendario.tb_ciclo_liturgico.version IS 'Versão usada para controle de concorrência otimista do registro.';
COMMENT ON COLUMN calendario.tb_ciclo_liturgico.ano_liturgico_ref IS 'Propriedade ano liturgico ref que caracteriza o registro de tb ciclo liturgico.';
ALTER TABLE ONLY calendario.tb_ciclo_liturgico
    ADD CONSTRAINT tb_ciclo_liturgico_pkey PRIMARY KEY (id);
CREATE INDEX idx_ciclo_liturgico_tipo ON calendario.tb_ciclo_liturgico USING btree (tipo_ciclo);
ALTER TABLE ONLY calendario.tb_ciclo_liturgico
    ADD CONSTRAINT fk_tb_ciclo_liturgico_ano_ref FOREIGN KEY (ano_liturgico_ref) REFERENCES calendario.anos_liturgicos(ano);

CREATE TABLE calendario.tb_solenidade_liturgica (
    id uuid NOT NULL,
    chave character varying(64) NOT NULL,
    nome character varying(160) NOT NULL,
    tipo_solenidade character varying(32) NOT NULL,
    mes_fixo integer,
    dia_fixo integer,
    offset_dias_pascoa integer,
    cor_liturgica character varying(32) NOT NULL,
    fator_demanda double precision DEFAULT 1.0 NOT NULL,
    nivel_prioridade integer DEFAULT 2 NOT NULL,
    descricao character varying(500),
    ativo boolean DEFAULT true NOT NULL,
    criado_em_utc timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.tb_solenidade_liturgica IS 'Solenidades e celebrações litúrgicas que influenciam a agenda paroquial.';
COMMENT ON COLUMN calendario.tb_solenidade_liturgica.criado_em_utc IS 'Timestamp UTC de criacao da solenidade.';
COMMENT ON COLUMN calendario.tb_solenidade_liturgica.atualizado_em_utc IS 'Timestamp UTC de modificacao da solenidade.';
COMMENT ON COLUMN calendario.tb_solenidade_liturgica.criado_por IS 'Identificador do usuario que registrou a solenidade.';
COMMENT ON COLUMN calendario.tb_solenidade_liturgica.atualizado_por IS 'Identificador do usuario que atualizou a solenidade.';
COMMENT ON COLUMN calendario.tb_solenidade_liturgica.id IS 'Identificador único do registro de tb solenidade liturgica.';
COMMENT ON COLUMN calendario.tb_solenidade_liturgica.chave IS 'Propriedade chave que caracteriza o registro de tb solenidade liturgica.';
COMMENT ON COLUMN calendario.tb_solenidade_liturgica.nome IS 'Informação textual de nome no contexto de tb solenidade liturgica.';
COMMENT ON COLUMN calendario.tb_solenidade_liturgica.tipo_solenidade IS 'Propriedade tipo solenidade que caracteriza o registro de tb solenidade liturgica.';
COMMENT ON COLUMN calendario.tb_solenidade_liturgica.mes_fixo IS 'Propriedade mes fixo que caracteriza o registro de tb solenidade liturgica.';
COMMENT ON COLUMN calendario.tb_solenidade_liturgica.dia_fixo IS 'Propriedade dia fixo que caracteriza o registro de tb solenidade liturgica.';
COMMENT ON COLUMN calendario.tb_solenidade_liturgica.offset_dias_pascoa IS 'Propriedade offset dias pascoa que caracteriza o registro de tb solenidade liturgica.';
COMMENT ON COLUMN calendario.tb_solenidade_liturgica.cor_liturgica IS 'Propriedade cor liturgica que caracteriza o registro de tb solenidade liturgica.';
COMMENT ON COLUMN calendario.tb_solenidade_liturgica.fator_demanda IS 'Propriedade fator demanda que caracteriza o registro de tb solenidade liturgica.';
COMMENT ON COLUMN calendario.tb_solenidade_liturgica.nivel_prioridade IS 'Propriedade nivel prioridade que caracteriza o registro de tb solenidade liturgica.';
COMMENT ON COLUMN calendario.tb_solenidade_liturgica.descricao IS 'Informação textual de descricao no contexto de tb solenidade liturgica.';
COMMENT ON COLUMN calendario.tb_solenidade_liturgica.ativo IS 'Indicador booleano de vigência operacional: ativo.';
COMMENT ON COLUMN calendario.tb_solenidade_liturgica.version IS 'Versão usada para controle de concorrência otimista do registro.';
ALTER TABLE ONLY calendario.tb_solenidade_liturgica
    ADD CONSTRAINT tb_solenidade_liturgica_chave_key UNIQUE (chave);
ALTER TABLE ONLY calendario.tb_solenidade_liturgica
    ADD CONSTRAINT tb_solenidade_liturgica_pkey PRIMARY KEY (id);
CREATE INDEX idx_solenidade_liturgica_chave ON calendario.tb_solenidade_liturgica USING btree (chave);
CREATE INDEX idx_solenidade_liturgica_tipo ON calendario.tb_solenidade_liturgica USING btree (tipo_solenidade);

CREATE TABLE calendario.evento_idempotency (
    idempotency_key character varying(128) NOT NULL,
    request_hash character varying(64) NOT NULL,
    evento_id uuid NOT NULL,
    response_status integer NOT NULL,
    criado_em_utc timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.evento_idempotency IS 'Registro de idempotência que impede a duplicação de comandos de criação de eventos.';
COMMENT ON COLUMN calendario.evento_idempotency.criado_em_utc IS 'Timestamp UTC de criacao do token de idempotencia gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.evento_idempotency.atualizado_em_utc IS 'Timestamp UTC de atualizacao do token de idempotencia.';
COMMENT ON COLUMN calendario.evento_idempotency.criado_por IS 'Identificador do criador do registro de idempotencia.';
COMMENT ON COLUMN calendario.evento_idempotency.atualizado_por IS 'Identificador do atualizador do registro de idempotencia.';
COMMENT ON COLUMN calendario.evento_idempotency.idempotency_key IS 'Propriedade idempotency key que caracteriza o registro de evento idempotency.';
COMMENT ON COLUMN calendario.evento_idempotency.request_hash IS 'Resumo criptográfico de request para comparação segura.';
COMMENT ON COLUMN calendario.evento_idempotency.evento_id IS 'Identificador do registro relacionado por evento no contexto de evento idempotency.';
COMMENT ON COLUMN calendario.evento_idempotency.response_status IS 'Propriedade response status que caracteriza o registro de evento idempotency.';
ALTER TABLE ONLY calendario.evento_idempotency
    ADD CONSTRAINT evento_idempotency_pkey PRIMARY KEY (idempotency_key);
CREATE INDEX idx_evento_idempotency_evento_id ON calendario.evento_idempotency USING btree (evento_id);

CREATE TABLE calendario.eventos (
    id uuid NOT NULL,
    titulo character varying(160) NOT NULL,
    descricao character varying(4000),
    grupo_paroquial_responsavel_id uuid NOT NULL,
    inicio_utc timestamp with time zone NOT NULL,
    fim_utc timestamp with time zone NOT NULL,
    status character varying(32) NOT NULL,
    cancelado_motivo character varying(2000),
    adicionado_extra_justificativa character varying(4000),
    version bigint DEFAULT 0 NOT NULL,
    conflict_state character varying(32),
    conflict_reason character varying(2000),
    recorrencia_id uuid,
    projeto_id uuid,
    categoria character varying(32),
    local_id uuid,
    nivel_prioridade integer DEFAULT 4 NOT NULL,
    autorizado_pelo_cpp boolean DEFAULT false NOT NULL,
    setor_pastoral character varying(64),
    ano_liturgico_ref integer,
    ciclo_liturgico_id uuid,
    igreja_vinculo_id uuid,
    criado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.eventos IS 'Compromissos da agenda paroquial (missas, reuniões, solenidades) sujeitos a fluxo de aprovação e conflito de local.';
COMMENT ON COLUMN calendario.eventos.id IS 'Identificador primário do evento (UUID).';
COMMENT ON COLUMN calendario.eventos.titulo IS 'Título exibido na agenda.';
COMMENT ON COLUMN calendario.eventos.descricao IS 'Descrição livre do evento.';
COMMENT ON COLUMN calendario.eventos.grupo_paroquial_responsavel_id IS 'Grupo paroquial ou unidade territorial responsável pelo evento; a referência polimórfica é validada pela aplicação.';
COMMENT ON COLUMN calendario.eventos.inicio_utc IS 'Instante de início do evento em UTC.';
COMMENT ON COLUMN calendario.eventos.fim_utc IS 'Instante de término do evento em UTC.';
COMMENT ON COLUMN calendario.eventos.status IS 'Estado do fluxo de aprovação/execução do evento.';
COMMENT ON COLUMN calendario.eventos.cancelado_motivo IS 'Justificativa obrigatória quando o evento é cancelado.';
COMMENT ON COLUMN calendario.eventos.version IS 'Controle de versão para concorrência otimista (JPA).';
COMMENT ON COLUMN calendario.eventos.recorrencia_id IS 'Identificador de série para eventos recorrentes.';
COMMENT ON COLUMN calendario.eventos.categoria IS 'Categoria litúrgica/administrativa do evento.';
COMMENT ON COLUMN calendario.eventos.local_id IS 'Local físico reservado para o evento, quando aplicável.';
COMMENT ON COLUMN calendario.eventos.nivel_prioridade IS 'Prioridade de agenda usada para resolução de conflitos entre eventos concorrentes.';
COMMENT ON COLUMN calendario.eventos.criado_em_utc IS 'Timestamp UTC de criacao do evento gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.eventos.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao do evento gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.eventos.criado_por IS 'Identificador do usuario/sistema que criou o evento.';
COMMENT ON COLUMN calendario.eventos.atualizado_por IS 'Identificador do usuario/sistema que modificou o evento.';
COMMENT ON COLUMN calendario.eventos.adicionado_extra_justificativa IS 'Propriedade adicionado extra justificativa que caracteriza o registro de eventos.';
COMMENT ON COLUMN calendario.eventos.conflict_state IS 'Propriedade conflict state que caracteriza o registro de eventos.';
COMMENT ON COLUMN calendario.eventos.conflict_reason IS 'Propriedade conflict reason que caracteriza o registro de eventos.';
COMMENT ON COLUMN calendario.eventos.projeto_id IS 'Identificador do registro relacionado por projeto no contexto de eventos.';
COMMENT ON COLUMN calendario.eventos.autorizado_pelo_cpp IS 'Propriedade autorizado pelo cpp que caracteriza o registro de eventos.';
COMMENT ON COLUMN calendario.eventos.setor_pastoral IS 'Propriedade setor pastoral que caracteriza o registro de eventos.';
COMMENT ON COLUMN calendario.eventos.ano_liturgico_ref IS 'Propriedade ano liturgico ref que caracteriza o registro de eventos.';
COMMENT ON COLUMN calendario.eventos.ciclo_liturgico_id IS 'Identificador do registro relacionado por ciclo liturgico no contexto de eventos.';
COMMENT ON COLUMN calendario.eventos.igreja_vinculo_id IS 'Identificador do registro relacionado por igreja vinculo no contexto de eventos.';
ALTER TABLE ONLY calendario.eventos
    ADD CONSTRAINT eventos_pkey PRIMARY KEY (id);
CREATE INDEX idx_eventos_conflict_state ON calendario.eventos USING btree (conflict_state);
CREATE INDEX idx_eventos_inicio_fim ON calendario.eventos USING btree (inicio_utc, fim_utc);
CREATE INDEX idx_eventos_nivel_prioridade ON calendario.eventos USING btree (nivel_prioridade);
CREATE INDEX idx_eventos_projeto_id ON calendario.eventos USING btree (projeto_id);
CREATE INDEX idx_eventos_recorrencia_id ON calendario.eventos USING btree (recorrencia_id);
CREATE INDEX idx_eventos_status ON calendario.eventos USING btree (status);
ALTER TABLE ONLY calendario.eventos
    ADD CONSTRAINT eventos_igreja_vinculo_id_new_fkey FOREIGN KEY (igreja_vinculo_id) REFERENCES calendario.unidade_territorial(id);
ALTER TABLE ONLY calendario.eventos
    ADD CONSTRAINT fk_eventos_ano_liturgico FOREIGN KEY (ano_liturgico_ref) REFERENCES calendario.anos_liturgicos(ano);
ALTER TABLE ONLY calendario.eventos
    ADD CONSTRAINT fk_eventos_ciclo_liturgico FOREIGN KEY (ciclo_liturgico_id) REFERENCES calendario.tb_ciclo_liturgico(id);
ALTER TABLE ONLY calendario.eventos
    ADD CONSTRAINT fk_eventos_local FOREIGN KEY (local_id) REFERENCES calendario.locais(id);
ALTER TABLE ONLY calendario.eventos
    ADD CONSTRAINT fk_eventos_projeto FOREIGN KEY (projeto_id) REFERENCES calendario.projetos_eventos(id);

CREATE TABLE calendario.eventos_recorrencia (
    id uuid NOT NULL,
    evento_base_id uuid NOT NULL,
    frequencia character varying(32) NOT NULL,
    intervalo integer NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    regra_json character varying(4000) NOT NULL,
    data_fim_utc timestamp with time zone,
    criado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.eventos_recorrencia IS 'Regra que define a repetição de um evento-base na agenda paroquial.';
COMMENT ON COLUMN calendario.eventos_recorrencia.criado_em_utc IS 'Timestamp UTC de criacao da regra de recorrencia.';
COMMENT ON COLUMN calendario.eventos_recorrencia.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao da recorrencia.';
COMMENT ON COLUMN calendario.eventos_recorrencia.criado_por IS 'Identificador do usuario que criou a recorrencia.';
COMMENT ON COLUMN calendario.eventos_recorrencia.atualizado_por IS 'Identificador do usuario que modificou a recorrencia.';
COMMENT ON COLUMN calendario.eventos_recorrencia.id IS 'Identificador único do registro de eventos recorrencia.';
COMMENT ON COLUMN calendario.eventos_recorrencia.evento_base_id IS 'Identificador do registro relacionado por evento base no contexto de eventos recorrencia.';
COMMENT ON COLUMN calendario.eventos_recorrencia.frequencia IS 'Propriedade frequencia que caracteriza o registro de eventos recorrencia.';
COMMENT ON COLUMN calendario.eventos_recorrencia.intervalo IS 'Propriedade intervalo que caracteriza o registro de eventos recorrencia.';
COMMENT ON COLUMN calendario.eventos_recorrencia.version IS 'Versão usada para controle de concorrência otimista do registro.';
COMMENT ON COLUMN calendario.eventos_recorrencia.regra_json IS 'Representação estruturada em JSON de regra.';
COMMENT ON COLUMN calendario.eventos_recorrencia.data_fim_utc IS 'Data de negócio referente a fim utc.';
ALTER TABLE ONLY calendario.eventos_recorrencia
    ADD CONSTRAINT eventos_recorrencia_pkey PRIMARY KEY (id);
ALTER TABLE ONLY calendario.eventos_recorrencia
    ADD CONSTRAINT fk_eventos_recorrencia_evento FOREIGN KEY (evento_base_id) REFERENCES calendario.eventos(id) ON DELETE CASCADE;

CREATE TABLE calendario.eventos_envolvidos (
    evento_id uuid NOT NULL,
    grupo_paroquial_id uuid NOT NULL,
    papel_participacao character varying(64),
    criado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.eventos_envolvidos IS 'Participação de grupos paroquiais em eventos, com seu papel de apoio ou responsabilidade.';
COMMENT ON COLUMN calendario.eventos_envolvidos.criado_em_utc IS 'Timestamp UTC de associacao da organizacao ao evento.';
COMMENT ON COLUMN calendario.eventos_envolvidos.atualizado_em_utc IS 'Timestamp UTC de modificacao da participacao.';
COMMENT ON COLUMN calendario.eventos_envolvidos.criado_por IS 'Identificador do usuario que associou a organizacao.';
COMMENT ON COLUMN calendario.eventos_envolvidos.atualizado_por IS 'Identificador do usuario que atualizou a participacao.';
COMMENT ON COLUMN calendario.eventos_envolvidos.evento_id IS 'Identificador do registro relacionado por evento no contexto de eventos envolvidos.';
COMMENT ON COLUMN calendario.eventos_envolvidos.grupo_paroquial_id IS 'Identificador do registro relacionado por grupo paroquial no contexto de eventos envolvidos.';
COMMENT ON COLUMN calendario.eventos_envolvidos.papel_participacao IS 'Propriedade papel participacao que caracteriza o registro de eventos envolvidos.';
ALTER TABLE ONLY calendario.eventos_envolvidos
    ADD CONSTRAINT eventos_envolvidos_pkey PRIMARY KEY (evento_id, grupo_paroquial_id);
ALTER TABLE ONLY calendario.eventos_envolvidos
    ADD CONSTRAINT fk_eventos_envolvidos_evento FOREIGN KEY (evento_id) REFERENCES calendario.eventos(id) ON DELETE CASCADE;

CREATE TABLE calendario.observacoes_evento (
    id uuid NOT NULL,
    evento_id uuid NOT NULL,
    usuario_id uuid NOT NULL,
    tipo character varying(64) NOT NULL,
    conteudo character varying(4000) NOT NULL,
    criado_em_utc timestamp with time zone NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    removida boolean DEFAULT false NOT NULL,
    removida_em_utc timestamp with time zone,
    removida_por_usuario_id uuid,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.observacoes_evento IS 'Notas colaborativas registradas no histórico operacional de um evento.';
COMMENT ON COLUMN calendario.observacoes_evento.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao da observacao do evento.';
COMMENT ON COLUMN calendario.observacoes_evento.criado_por IS 'Identificador do autor da observacao.';
COMMENT ON COLUMN calendario.observacoes_evento.atualizado_por IS 'Identificador do ultimo usuario a modificar a observacao.';
COMMENT ON COLUMN calendario.observacoes_evento.id IS 'Identificador único do registro de observacoes evento.';
COMMENT ON COLUMN calendario.observacoes_evento.evento_id IS 'Identificador do registro relacionado por evento no contexto de observacoes evento.';
COMMENT ON COLUMN calendario.observacoes_evento.usuario_id IS 'Identificador do registro relacionado por usuario no contexto de observacoes evento.';
COMMENT ON COLUMN calendario.observacoes_evento.tipo IS 'Propriedade tipo que caracteriza o registro de observacoes evento.';
COMMENT ON COLUMN calendario.observacoes_evento.conteudo IS 'Informação textual de conteudo no contexto de observacoes evento.';
COMMENT ON COLUMN calendario.observacoes_evento.criado_em_utc IS 'Instante UTC de criação imutável do registro.';
COMMENT ON COLUMN calendario.observacoes_evento.version IS 'Versão usada para controle de concorrência otimista do registro.';
COMMENT ON COLUMN calendario.observacoes_evento.removida IS 'Indicador booleano de vigência operacional: removida.';
COMMENT ON COLUMN calendario.observacoes_evento.removida_em_utc IS 'Instante UTC associado a removida no ciclo de vida do registro.';
COMMENT ON COLUMN calendario.observacoes_evento.removida_por_usuario_id IS 'Identificador do registro relacionado por removida por usuario no contexto de observacoes evento.';
ALTER TABLE ONLY calendario.observacoes_evento
    ADD CONSTRAINT observacoes_evento_pkey PRIMARY KEY (id);
CREATE INDEX idx_observacoes_evento_evento_removida_criado ON calendario.observacoes_evento USING btree (evento_id, removida, criado_em_utc, id);
CREATE INDEX idx_observacoes_evento_evento_usuario_removida ON calendario.observacoes_evento USING btree (evento_id, usuario_id, removida, criado_em_utc, id);

CREATE TABLE calendario.observacoes_nota_revisoes (
    id uuid NOT NULL,
    observacao_id uuid NOT NULL,
    conteudo_anterior character varying(4000) NOT NULL,
    conteudo_novo character varying(4000) NOT NULL,
    revisado_por_usuario_id uuid NOT NULL,
    revisado_em_utc timestamp with time zone NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    criado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.observacoes_nota_revisoes IS 'Histórico imutável de alterações feitas em notas de eventos.';
COMMENT ON COLUMN calendario.observacoes_nota_revisoes.criado_em_utc IS 'Timestamp UTC da revisao de observacao.';
COMMENT ON COLUMN calendario.observacoes_nota_revisoes.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao da revisao.';
COMMENT ON COLUMN calendario.observacoes_nota_revisoes.criado_por IS 'Identificador do autor da revisao.';
COMMENT ON COLUMN calendario.observacoes_nota_revisoes.atualizado_por IS 'Identificador do usuario que modificou a revisao.';
COMMENT ON COLUMN calendario.observacoes_nota_revisoes.id IS 'Identificador único do registro de observacoes nota revisoes.';
COMMENT ON COLUMN calendario.observacoes_nota_revisoes.observacao_id IS 'Identificador do registro relacionado por observacao no contexto de observacoes nota revisoes.';
COMMENT ON COLUMN calendario.observacoes_nota_revisoes.conteudo_anterior IS 'Propriedade conteudo anterior que caracteriza o registro de observacoes nota revisoes.';
COMMENT ON COLUMN calendario.observacoes_nota_revisoes.conteudo_novo IS 'Propriedade conteudo novo que caracteriza o registro de observacoes nota revisoes.';
COMMENT ON COLUMN calendario.observacoes_nota_revisoes.revisado_por_usuario_id IS 'Identificador do registro relacionado por revisado por usuario no contexto de observacoes nota revisoes.';
COMMENT ON COLUMN calendario.observacoes_nota_revisoes.revisado_em_utc IS 'Instante UTC associado a revisado no ciclo de vida do registro.';
COMMENT ON COLUMN calendario.observacoes_nota_revisoes.version IS 'Versão usada para controle de concorrência otimista do registro.';
ALTER TABLE ONLY calendario.observacoes_nota_revisoes
    ADD CONSTRAINT observacoes_nota_revisoes_pkey PRIMARY KEY (id);
CREATE INDEX idx_observacoes_nota_revisoes_observacao ON calendario.observacoes_nota_revisoes USING btree (observacao_id, revisado_em_utc, id);
ALTER TABLE ONLY calendario.observacoes_nota_revisoes
    ADD CONSTRAINT observacoes_nota_revisoes_observacao_id_fkey FOREIGN KEY (observacao_id) REFERENCES calendario.observacoes_evento(id) ON DELETE CASCADE;

ALTER TABLE ONLY calendario.eventos
    ADD CONSTRAINT fk_eventos_recorrencia FOREIGN KEY (recorrencia_id) REFERENCES calendario.eventos_recorrencia(id);

/**
 * DADOS DE REFERÊNCIA: ciclos e anos litúrgicos canônicos necessários à classificação da agenda.
 * São dados institucionais estáveis do contexto Calendário, não massa demonstrativa local.
 */

INSERT INTO calendario.anos_liturgicos (ano, ciclo_letra, data_inicio_advento, data_fim_comum, atualizado_em_utc)
VALUES
(2026, 'C', '2025-11-30 00:00:00+00', '2026-11-28 23:59:59+00', CURRENT_TIMESTAMP),
(2027, 'A', '2026-11-29 00:00:00+00', '2027-11-27 23:59:59+00', CURRENT_TIMESTAMP),
(2028, 'B', '2027-11-28 00:00:00+00', '2028-12-02 23:59:59+00', CURRENT_TIMESTAMP),
(2029, 'C', '2028-12-03 00:00:00+00', '2029-12-01 23:59:59+00', CURRENT_TIMESTAMP),
(2030, 'A', '2029-12-02 00:00:00+00', '2030-11-30 23:59:59+00', CURRENT_TIMESTAMP),
(2031, 'B', '2030-12-01 00:00:00+00', '2031-11-29 23:59:59+00', CURRENT_TIMESTAMP),
(2032, 'C', '2031-11-30 00:00:00+00', '2032-11-27 23:59:59+00', CURRENT_TIMESTAMP),
(2033, 'A', '2032-11-28 00:00:00+00', '2033-11-26 23:59:59+00', CURRENT_TIMESTAMP),
(2034, 'B', '2033-11-27 00:00:00+00', '2034-12-02 23:59:59+00', CURRENT_TIMESTAMP),
(2035, 'C', '2034-12-03 00:00:00+00', '2035-12-01 23:59:59+00', CURRENT_TIMESTAMP);

-- Associar os ciclos existentes ao Ano Litúrgico 2026
INSERT INTO calendario.tb_ciclo_liturgico (id, nome, tipo_ciclo, cor_liturgica, mes_inicio, dia_inicio, mes_fim, dia_fim, fator_demanda, descricao, criado_em_utc, atualizado_em_utc, version)
VALUES
('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c61', 'Advento', 'ADVENTO', 'ROXO', 11, 27, 12, 24, 1.8, 'Preparação para o Nascimento de Jesus', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c62', 'Tempo do Natal', 'NATAL', 'BRANCO', 12, 25, 1, 13, 2.2, 'Celebração do Nascimento de Nosso Senhor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c63', 'Quaresma', 'QUARESMA', 'ROXO', 2, 10, 3, 31, 2.5, 'Período de penitência e conversão', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c64', 'Semana Santa e Páscoa', 'PASCOA', 'BRANCO', 4, 1, 5, 20, 3.0, 'Celebração da Paixão, Morte e Ressurreição de Cristo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c65', 'Festa Padroeira N. Sra. de Fátima', 'FESTA_PADROEIRA', 'DOURADO', 5, 1, 5, 31, 2.8, 'Festividades da Padroeira Paroquial', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c66', 'Tempo Comum', 'TEMPO_COMUM', 'VERDE', 6, 1, 11, 26, 1.0, 'Tempo Ordinário da Igreja', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

UPDATE calendario.tb_ciclo_liturgico
SET ano_liturgico_ref = 2026
WHERE ano_liturgico_ref IS NULL;

INSERT INTO calendario.tb_ciclo_liturgico (id, nome, tipo_ciclo, cor_liturgica, mes_inicio, dia_inicio, mes_fim, dia_fim, fator_demanda, descricao, ano_liturgico_ref)
VALUES
('71000000-0000-0000-0000-000000002027', 'Advento 2027', 'ADVENTO', 'ROXO', 11, 29, 12, 24, 1.8, 'Tempo de preparação e expectativa do nascimento de Cristo', 2027),
('72000000-0000-0000-0000-000000002027', 'Tempo do Natal 2027', 'NATAL', 'BRANCO', 12, 25, 1, 10, 2.0, 'Celebração do Nascimento do Senhor e Epifania', 2027),
('73000000-0000-0000-0000-000000002027', 'Quaresma 2027', 'QUARESMA', 'ROXO', 2, 10, 3, 27, 2.2, 'Tempo de penitência, conversão e Oração', 2027),
('74000000-0000-0000-0000-000000002027', 'Tempo Pascal 2027', 'PASCOA', 'BRANCO', 3, 28, 5, 16, 2.5, 'Cinquenta dias de alegria da Ressurreição até Pentecostes', 2027),
('75000000-0000-0000-0000-000000002027', 'Tempo Comum 2027', 'TEMPO_COMUM', 'VERDE', 1, 11, 11, 27, 1.0, 'Caminhada ordinária da Igreja', 2027),
('76000000-0000-0000-0000-000000002027', 'Festa Padroeira 2027', 'FESTA_PADROEIRA', 'BRANCO', 5, 4, 5, 13, 3.0, 'Novena e Festa da Padroeira Nossa Senhora de Fátima', 2027),

('71000000-0000-0000-0000-000000002028', 'Advento 2028', 'ADVENTO', 'ROXO', 11, 28, 12, 24, 1.8, 'Tempo de expectativa messiânica', 2028),
('72000000-0000-0000-0000-000000002028', 'Tempo do Natal 2028', 'NATAL', 'BRANCO', 12, 25, 1, 8, 2.0, 'Ciclo do Natal', 2028),
('73000000-0000-0000-0000-000000002028', 'Quaresma 2028', 'QUARESMA', 'ROXO', 3, 1, 4, 15, 2.2, 'Caminhada quaresmal', 2028),
('74000000-0000-0000-0000-000000002028', 'Tempo Pascal 2028', 'PASCOA', 'BRANCO', 4, 16, 6, 4, 2.5, 'Tempo da Ressurreição', 2028),
('75000000-0000-0000-0000-000000002028', 'Tempo Comum 2028', 'TEMPO_COMUM', 'VERDE', 1, 9, 11, 27, 1.0, 'Tempo Comum', 2028),
('76000000-0000-0000-0000-000000002028', 'Festa Padroeira 2028', 'FESTA_PADROEIRA', 'BRANCO', 5, 4, 5, 13, 3.0, 'Festividades da Padroeira', 2028),

('71000000-0000-0000-0000-000000002029', 'Advento 2029', 'ADVENTO', 'ROXO', 12, 3, 12, 24, 1.8, 'Advento 2029', 2029),
('72000000-0000-0000-0000-000000002029', 'Tempo do Natal 2029', 'NATAL', 'BRANCO', 12, 25, 1, 13, 2.0, 'Natal 2029', 2029),
('73000000-0000-0000-0000-000000002029', 'Quaresma 2029', 'QUARESMA', 'ROXO', 2, 14, 3, 31, 2.2, 'Quaresma 2029', 2029),
('74000000-0000-0000-0000-000000002029', 'Tempo Pascal 2029', 'PASCOA', 'BRANCO', 4, 1, 5, 20, 2.5, 'Páscoa 2029', 2029),
('75000000-0000-0000-0000-000000002029', 'Tempo Comum 2029', 'TEMPO_COMUM', 'VERDE', 1, 14, 12, 1, 1.0, 'Tempo Comum 2029', 2029),

('71000000-0000-0000-0000-000000002030', 'Advento 2030', 'ADVENTO', 'ROXO', 12, 2, 12, 24, 1.8, 'Advento 2030', 2030),
('72000000-0000-0000-0000-000000002030', 'Tempo do Natal 2030', 'NATAL', 'BRANCO', 12, 25, 1, 12, 2.0, 'Natal 2030', 2030),
('73000000-0000-0000-0000-000000002030', 'Quaresma 2030', 'QUARESMA', 'ROXO', 3, 6, 4, 20, 2.2, 'Quaresma 2030', 2030),
('74000000-0000-0000-0000-000000002030', 'Tempo Pascal 2030', 'PASCOA', 'BRANCO', 4, 21, 6, 9, 2.5, 'Páscoa 2030', 2030),
('75000000-0000-0000-0000-000000002030', 'Tempo Comum 2030', 'TEMPO_COMUM', 'VERDE', 1, 13, 11, 30, 1.0, 'Tempo Comum 2030', 2030),

('71000000-0000-0000-0000-000000002031', 'Advento 2031', 'ADVENTO', 'ROXO', 12, 1, 12, 24, 1.8, 'Advento 2031', 2031),
('72000000-0000-0000-0000-000000002031', 'Tempo do Natal 2031', 'NATAL', 'BRANCO', 12, 25, 1, 11, 2.0, 'Natal 2031', 2031),
('73000000-0000-0000-0000-000000002031', 'Quaresma 2031', 'QUARESMA', 'ROXO', 2, 26, 4, 12, 2.2, 'Quaresma 2031', 2031),
('74000000-0000-0000-0000-000000002031', 'Tempo Pascal 2031', 'PASCOA', 'BRANCO', 4, 13, 6, 1, 2.5, 'Páscoa 2031', 2031),
('75000000-0000-0000-0000-000000002031', 'Tempo Comum 2031', 'TEMPO_COMUM', 'VERDE', 1, 12, 11, 29, 1.0, 'Tempo Comum 2031', 2031),

('71000000-0000-0000-0000-000000002032', 'Advento 2032', 'ADVENTO', 'ROXO', 11, 30, 12, 24, 1.8, 'Advento 2032', 2032),
('72000000-0000-0000-0000-000000002032', 'Tempo do Natal 2032', 'NATAL', 'BRANCO', 12, 25, 1, 10, 2.0, 'Natal 2032', 2032),
('73000000-0000-0000-0000-000000002032', 'Quaresma 2032', 'QUARESMA', 'ROXO', 2, 11, 3, 27, 2.2, 'Quaresma 2032', 2032),
('74000000-0000-0000-0000-000000002032', 'Tempo Pascal 2032', 'PASCOA', 'BRANCO', 3, 28, 5, 16, 2.5, 'Páscoa 2032', 2032),
('75000000-0000-0000-0000-000000002032', 'Tempo Comum 2032', 'TEMPO_COMUM', 'VERDE', 1, 11, 11, 27, 1.0, 'Tempo Comum 2032', 2032),

('71000000-0000-0000-0000-000000002033', 'Advento 2033', 'ADVENTO', 'ROXO', 11, 28, 12, 24, 1.8, 'Advento 2033', 2033),
('72000000-0000-0000-0000-000000002033', 'Tempo do Natal 2033', 'NATAL', 'BRANCO', 12, 25, 1, 9, 2.0, 'Natal 2033', 2033),
('73000000-0000-0000-0000-000000002033', 'Quaresma 2033', 'QUARESMA', 'ROXO', 3, 2, 4, 16, 2.2, 'Quaresma 2033', 2033),
('74000000-0000-0000-0000-000000002033', 'Tempo Pascal 2033', 'PASCOA', 'BRANCO', 4, 17, 6, 5, 2.5, 'Páscoa 2033', 2033),
('75000000-0000-0000-0000-000000002033', 'Tempo Comum 2033', 'TEMPO_COMUM', 'VERDE', 1, 10, 11, 26, 1.0, 'Tempo Comum 2033', 2033),

('71000000-0000-0000-0000-000000002034', 'Advento 2034', 'ADVENTO', 'ROXO', 11, 27, 12, 24, 1.8, 'Advento 2034', 2034),
('72000000-0000-0000-0000-000000002034', 'Tempo do Natal 2034', 'NATAL', 'BRANCO', 12, 25, 1, 8, 2.0, 'Natal 2034', 2034),
('73000000-0000-0000-0000-000000002034', 'Quaresma 2034', 'QUARESMA', 'ROXO', 2, 22, 4, 8, 2.2, 'Quaresma 2034', 2034),
('74000000-0000-0000-0000-000000002034', 'Tempo Pascal 2034', 'PASCOA', 'BRANCO', 4, 9, 5, 28, 2.5, 'Páscoa 2034', 2034),
('75000000-0000-0000-0000-000000002034', 'Tempo Comum 2034', 'TEMPO_COMUM', 'VERDE', 1, 9, 12, 2, 1.0, 'Tempo Comum 2034', 2034),

('71000000-0000-0000-0000-000000002035', 'Advento 2035', 'ADVENTO', 'ROXO', 12, 3, 12, 24, 1.8, 'Advento 2035', 2035),
('72000000-0000-0000-0000-000000002035', 'Tempo do Natal 2035', 'NATAL', 'BRANCO', 12, 25, 1, 13, 2.0, 'Natal 2035', 2035),
('73000000-0000-0000-0000-000000002035', 'Quaresma 2035', 'QUARESMA', 'ROXO', 2, 7, 3, 24, 2.2, 'Quaresma 2035', 2035),
('74000000-0000-0000-0000-000000002035', 'Tempo Pascal 2035', 'PASCOA', 'BRANCO', 3, 25, 5, 13, 2.5, 'Páscoa 2035', 2035),
('75000000-0000-0000-0000-000000002035', 'Tempo Comum 2035', 'TEMPO_COMUM', 'VERDE', 1, 14, 12, 1, 1.0, 'Tempo Comum 2035', 2035);
