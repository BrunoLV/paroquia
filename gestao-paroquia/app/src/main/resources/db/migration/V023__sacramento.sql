/**
 * MIGRAÇÃO: V023__sacramento.sql
 * CONTEXTO: Cria o livro de assentos sacramentais (Batismo, Confirmação, Matrimônio e Ordem), seus
 * participantes (snapshot textual + vínculo tardio opcional a fiel/clérigo), averbações marginais,
 * ressalvas, notificações canônicas e a trilha estrita de histórico.
 * REGRA: registros sacramentais são perpétuos (Cân. 535): triggers vedam DELETE em todo o livro e
 * UPDATE em tabelas append-only. Penitência jamais é registrada (Cân. 983).
 */

CREATE FUNCTION calendario.bloquear_alteracao_sacramental() RETURNS trigger
    LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'Registro sacramental e perpetuo: % em % nao e permitido (Can. 535).', TG_OP, TG_TABLE_NAME;
END;
$$;
COMMENT ON FUNCTION calendario.bloquear_alteracao_sacramental() IS 'Veda exclusão e alteração de registros sacramentais perpétuos (Cân. 535).';

CREATE TABLE calendario.assentos_sacramentais (
    id uuid NOT NULL,
    tipo character varying(16) NOT NULL,
    status character varying(16) NOT NULL,
    visibilidade character varying(16) NOT NULL,
    data_lavratura date NOT NULL,
    lavrado_por character varying(128),
    livro character varying(32),
    folha character varying(32),
    termo character varying(32),
    paroquia_custodiante character varying(255),
    data_celebracao timestamp without time zone,
    local_celebracao character varying(255),
    dado_sensivel boolean DEFAULT false NOT NULL,
    dados_especificos jsonb DEFAULT '{}'::jsonb NOT NULL,
    homologado_por character varying(128),
    homologado_em_utc timestamp with time zone,
    motivo_cancelamento text,
    cancelado_por character varying(128),
    cancelado_em_utc timestamp with time zone,
    criado_em_utc timestamp with time zone NOT NULL,
    atualizado_em_utc timestamp with time zone NOT NULL,
    criado_por character varying(128) NOT NULL,
    atualizado_por character varying(128) NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT assentos_sacramentais_pkey PRIMARY KEY (id),
    CONSTRAINT assentos_sacramentais_tipo_ck CHECK (tipo IN ('BATISMO', 'CONFIRMACAO', 'MATRIMONIO', 'ORDEM')),
    CONSTRAINT assentos_sacramentais_status_ck CHECK (status IN ('DRAFT', 'SEALED', 'CANCELADO')),
    CONSTRAINT assentos_sacramentais_visibilidade_ck CHECK (visibilidade IN ('COMUM', 'RESERVADO')),
    CONSTRAINT assentos_sacramentais_selado_ck CHECK (status <> 'SEALED' OR homologado_por IS NOT NULL),
    CONSTRAINT assentos_sacramentais_cancelado_ck CHECK (status <> 'CANCELADO' OR motivo_cancelamento IS NOT NULL)
);
COMMENT ON TABLE calendario.assentos_sacramentais IS 'Livro de assentos sacramentais com fé pública (Cân. 535). Nunca excluído; cancelamento exige motivo.';
COMMENT ON COLUMN calendario.assentos_sacramentais.id IS 'Identificador único do assento (UUID).';
COMMENT ON COLUMN calendario.assentos_sacramentais.tipo IS 'Sacramento do assento: BATISMO, CONFIRMACAO, MATRIMONIO ou ORDEM.';
COMMENT ON COLUMN calendario.assentos_sacramentais.status IS 'Ciclo de vida: DRAFT (editável), SEALED (homologado pelo Pároco, somente leitura) ou CANCELADO.';
COMMENT ON COLUMN calendario.assentos_sacramentais.visibilidade IS 'COMUM (livro paroquial) ou RESERVADO (matrimônio secreto, invisível à secretaria).';
COMMENT ON COLUMN calendario.assentos_sacramentais.data_lavratura IS 'Data da lavratura do termo, distinta da data da celebração.';
COMMENT ON COLUMN calendario.assentos_sacramentais.lavrado_por IS 'Usuário que lavrou o rascunho.';
COMMENT ON COLUMN calendario.assentos_sacramentais.livro IS 'Número do livro oficial.';
COMMENT ON COLUMN calendario.assentos_sacramentais.folha IS 'Número da folha/página no livro oficial.';
COMMENT ON COLUMN calendario.assentos_sacramentais.termo IS 'Número do termo (assento) na folha.';
COMMENT ON COLUMN calendario.assentos_sacramentais.paroquia_custodiante IS 'Paróquia responsável pelo assento e guarda do arquivo.';
COMMENT ON COLUMN calendario.assentos_sacramentais.data_celebracao IS 'Data e hora local da celebração do sacramento.';
COMMENT ON COLUMN calendario.assentos_sacramentais.local_celebracao IS 'Templo, capela ou local autorizado pelo Ordinário.';
COMMENT ON COLUMN calendario.assentos_sacramentais.dado_sensivel IS 'Marca filiação adotiva ou notas reservadas, omitidas de certidões simples (LGPD).';
COMMENT ON COLUMN calendario.assentos_sacramentais.dados_especificos IS 'Campos próprios do sacramento (processo de habilitação, grau da ordem, referência ao batismo etc.).';
COMMENT ON COLUMN calendario.assentos_sacramentais.homologado_por IS 'Pároco que selou o termo.';
COMMENT ON COLUMN calendario.assentos_sacramentais.homologado_em_utc IS 'Instante UTC da homologação.';
COMMENT ON COLUMN calendario.assentos_sacramentais.motivo_cancelamento IS 'Motivo formal do cancelamento (duplicidade ou erro formal grave).';
COMMENT ON COLUMN calendario.assentos_sacramentais.cancelado_por IS 'Pároco que cancelou o termo.';
COMMENT ON COLUMN calendario.assentos_sacramentais.cancelado_em_utc IS 'Instante UTC do cancelamento.';
COMMENT ON COLUMN calendario.assentos_sacramentais.criado_em_utc IS 'Instante UTC de criação imutável do registro.';
COMMENT ON COLUMN calendario.assentos_sacramentais.atualizado_em_utc IS 'Instante UTC da última atualização do registro.';
COMMENT ON COLUMN calendario.assentos_sacramentais.criado_por IS 'Identificador do responsável pela criação.';
COMMENT ON COLUMN calendario.assentos_sacramentais.atualizado_por IS 'Identificador do responsável pela última atualização.';
COMMENT ON COLUMN calendario.assentos_sacramentais.version IS 'Versão para controle de concorrência otimista.';
CREATE UNIQUE INDEX uk_assento_sacramental_termo ON calendario.assentos_sacramentais USING btree (tipo, livro, folha, termo)
    WHERE livro IS NOT NULL AND folha IS NOT NULL AND termo IS NOT NULL AND status <> 'CANCELADO';
CREATE INDEX idx_assento_sacramental_tipo_status ON calendario.assentos_sacramentais USING btree (tipo, status, visibilidade);
CREATE TRIGGER trg_assentos_sacramentais_sem_delete BEFORE DELETE ON calendario.assentos_sacramentais
    FOR EACH ROW EXECUTE FUNCTION calendario.bloquear_alteracao_sacramental();

CREATE TABLE calendario.assento_participantes (
    id uuid NOT NULL,
    assento_id uuid NOT NULL,
    papel character varying(32) NOT NULL,
    nome_historico character varying(255) NOT NULL,
    documento character varying(2048),
    local_nascimento character varying(255),
    catolico boolean,
    crismado boolean,
    idade_na_celebracao integer,
    condicao_canonica character varying(16),
    fiel_id uuid,
    clerigo_id uuid,
    criado_em_utc timestamp with time zone NOT NULL,
    atualizado_em_utc timestamp with time zone NOT NULL,
    criado_por character varying(128) NOT NULL,
    atualizado_por character varying(128) NOT NULL,
    CONSTRAINT assento_participantes_pkey PRIMARY KEY (id),
    CONSTRAINT assento_participantes_assento_fk FOREIGN KEY (assento_id) REFERENCES calendario.assentos_sacramentais (id),
    CONSTRAINT assento_participantes_fiel_fk FOREIGN KEY (fiel_id) REFERENCES calendario.fieis (id),
    CONSTRAINT assento_participantes_clerigo_fk FOREIGN KEY (clerigo_id) REFERENCES calendario.clerigos (id),
    CONSTRAINT assento_participantes_vinculo_ck CHECK (fiel_id IS NULL OR clerigo_id IS NULL)
);
COMMENT ON TABLE calendario.assento_participantes IS 'Snapshot textual das pessoas no assento; fiel_id/clerigo_id são ponteiros opcionais de vínculo tardio (late binding).';
COMMENT ON COLUMN calendario.assento_participantes.id IS 'Identificador único do participante no assento.';
COMMENT ON COLUMN calendario.assento_participantes.assento_id IS 'Assento ao qual o participante pertence.';
COMMENT ON COLUMN calendario.assento_participantes.papel IS 'Função no ato (batizando, pai, padrinho, ministro celebrante etc.).';
COMMENT ON COLUMN calendario.assento_participantes.nome_historico IS 'Nome exatamente como atestado no dia da celebração; nunca alterado por vínculo.';
COMMENT ON COLUMN calendario.assento_participantes.documento IS 'Documento do participante, cifrado em repouso (LGPD).';
COMMENT ON COLUMN calendario.assento_participantes.local_nascimento IS 'Município/UF/país de nascimento atestado.';
COMMENT ON COLUMN calendario.assento_participantes.catolico IS 'Condição canônica atestada: católico (elegibilidade de padrinho, Cân. 874).';
COMMENT ON COLUMN calendario.assento_participantes.crismado IS 'Condição canônica atestada: crismado.';
COMMENT ON COLUMN calendario.assento_participantes.idade_na_celebracao IS 'Idade atestada na data da celebração.';
COMMENT ON COLUMN calendario.assento_participantes.condicao_canonica IS 'Para ministros: BISPO, PRESBITERO ou DIACONO.';
COMMENT ON COLUMN calendario.assento_participantes.fiel_id IS 'Vínculo tardio opcional ao cadastro de fiel.';
COMMENT ON COLUMN calendario.assento_participantes.clerigo_id IS 'Vínculo tardio opcional ao cadastro de clérigo.';
COMMENT ON COLUMN calendario.assento_participantes.criado_em_utc IS 'Instante UTC de criação imutável do registro.';
COMMENT ON COLUMN calendario.assento_participantes.atualizado_em_utc IS 'Instante UTC da última atualização do registro.';
COMMENT ON COLUMN calendario.assento_participantes.criado_por IS 'Identificador do responsável pela criação.';
COMMENT ON COLUMN calendario.assento_participantes.atualizado_por IS 'Identificador do responsável pela última atualização.';
CREATE INDEX idx_assento_participantes_assento ON calendario.assento_participantes USING btree (assento_id);
CREATE INDEX idx_assento_participantes_fiel ON calendario.assento_participantes USING btree (fiel_id) WHERE fiel_id IS NOT NULL;
CREATE INDEX idx_assento_participantes_clerigo ON calendario.assento_participantes USING btree (clerigo_id) WHERE clerigo_id IS NOT NULL;

CREATE TABLE calendario.averbacoes_batismo (
    id uuid NOT NULL,
    assento_id uuid NOT NULL,
    tipo_evento character varying(32) NOT NULL,
    data_averbacao date NOT NULL,
    numero_documento character varying(128),
    teor_nota text NOT NULL,
    reservada boolean DEFAULT false NOT NULL,
    inserida_por character varying(128),
    status character varying(32) NOT NULL,
    homologada_por character varying(128),
    homologada_em_utc timestamp with time zone,
    criado_em_utc timestamp with time zone NOT NULL,
    atualizado_em_utc timestamp with time zone NOT NULL,
    criado_por character varying(128) NOT NULL,
    atualizado_por character varying(128) NOT NULL,
    CONSTRAINT averbacoes_batismo_pkey PRIMARY KEY (id),
    CONSTRAINT averbacoes_batismo_assento_fk FOREIGN KEY (assento_id) REFERENCES calendario.assentos_sacramentais (id),
    CONSTRAINT averbacoes_batismo_status_ck CHECK (status IN ('PENDENTE_HOMOLOGACAO', 'HOMOLOGADA'))
);
COMMENT ON TABLE calendario.averbacoes_batismo IS 'Notas marginais append-only do assento de Batismo (Cân. 535 §2).';
COMMENT ON COLUMN calendario.averbacoes_batismo.id IS 'Identificador único da averbação.';
COMMENT ON COLUMN calendario.averbacoes_batismo.assento_id IS 'Assento de batismo averbado.';
COMMENT ON COLUMN calendario.averbacoes_batismo.tipo_evento IS 'Evento averbado: CONFIRMACAO, MATRIMONIO, ORDEM, VOTOS_PERPETUOS, NULIDADE ou ANOTACAO_DISCIPLINAR.';
COMMENT ON COLUMN calendario.averbacoes_batismo.data_averbacao IS 'Data da averbação.';
COMMENT ON COLUMN calendario.averbacoes_batismo.numero_documento IS 'Número do documento ou notificação comprobatória.';
COMMENT ON COLUMN calendario.averbacoes_batismo.teor_nota IS 'Teor da nota marginal.';
COMMENT ON COLUMN calendario.averbacoes_batismo.reservada IS 'Nota reservada, omitida de certidões simples (LGPD).';
COMMENT ON COLUMN calendario.averbacoes_batismo.inserida_por IS 'Usuário que digitou a averbação.';
COMMENT ON COLUMN calendario.averbacoes_batismo.status IS 'PENDENTE_HOMOLOGACAO (digitada) ou HOMOLOGADA pelo Pároco.';
COMMENT ON COLUMN calendario.averbacoes_batismo.homologada_por IS 'Pároco que homologou a averbação.';
COMMENT ON COLUMN calendario.averbacoes_batismo.homologada_em_utc IS 'Instante UTC da homologação da averbação.';
COMMENT ON COLUMN calendario.averbacoes_batismo.criado_em_utc IS 'Instante UTC de criação imutável do registro.';
COMMENT ON COLUMN calendario.averbacoes_batismo.atualizado_em_utc IS 'Instante UTC da última atualização do registro.';
COMMENT ON COLUMN calendario.averbacoes_batismo.criado_por IS 'Identificador do responsável pela criação.';
COMMENT ON COLUMN calendario.averbacoes_batismo.atualizado_por IS 'Identificador do responsável pela última atualização.';
CREATE INDEX idx_averbacoes_batismo_assento ON calendario.averbacoes_batismo USING btree (assento_id);
CREATE TRIGGER trg_averbacoes_batismo_sem_delete BEFORE DELETE ON calendario.averbacoes_batismo
    FOR EACH ROW EXECUTE FUNCTION calendario.bloquear_alteracao_sacramental();

CREATE TABLE calendario.assento_ressalvas (
    id uuid NOT NULL,
    assento_id uuid NOT NULL,
    campo character varying(128) NOT NULL,
    valor_original text,
    valor_corrigido text NOT NULL,
    justificativa text NOT NULL,
    atestada_por character varying(128),
    atestada_em_utc timestamp with time zone NOT NULL,
    criado_em_utc timestamp with time zone NOT NULL,
    atualizado_em_utc timestamp with time zone NOT NULL,
    criado_por character varying(128) NOT NULL,
    atualizado_por character varying(128) NOT NULL,
    CONSTRAINT assento_ressalvas_pkey PRIMARY KEY (id),
    CONSTRAINT assento_ressalvas_assento_fk FOREIGN KEY (assento_id) REFERENCES calendario.assentos_sacramentais (id)
);
COMMENT ON TABLE calendario.assento_ressalvas IS 'Retificações materiais atestadas pelo Pároco; acrescentam-se ao termo sem sobrescrever o original.';
COMMENT ON COLUMN calendario.assento_ressalvas.id IS 'Identificador único da ressalva.';
COMMENT ON COLUMN calendario.assento_ressalvas.assento_id IS 'Assento retificado.';
COMMENT ON COLUMN calendario.assento_ressalvas.campo IS 'Campo do termo objeto da retificação.';
COMMENT ON COLUMN calendario.assento_ressalvas.valor_original IS 'Valor lavrado originalmente.';
COMMENT ON COLUMN calendario.assento_ressalvas.valor_corrigido IS 'Valor correto atestado.';
COMMENT ON COLUMN calendario.assento_ressalvas.justificativa IS 'Justificativa formal da retificação.';
COMMENT ON COLUMN calendario.assento_ressalvas.atestada_por IS 'Pároco que atestou a ressalva.';
COMMENT ON COLUMN calendario.assento_ressalvas.atestada_em_utc IS 'Instante UTC da atestação.';
COMMENT ON COLUMN calendario.assento_ressalvas.criado_em_utc IS 'Instante UTC de criação imutável do registro.';
COMMENT ON COLUMN calendario.assento_ressalvas.atualizado_em_utc IS 'Instante UTC da última atualização do registro.';
COMMENT ON COLUMN calendario.assento_ressalvas.criado_por IS 'Identificador do responsável pela criação.';
COMMENT ON COLUMN calendario.assento_ressalvas.atualizado_por IS 'Identificador do responsável pela última atualização.';
CREATE INDEX idx_assento_ressalvas_assento ON calendario.assento_ressalvas USING btree (assento_id);
CREATE TRIGGER trg_assento_ressalvas_append_only BEFORE UPDATE OR DELETE ON calendario.assento_ressalvas
    FOR EACH ROW EXECUTE FUNCTION calendario.bloquear_alteracao_sacramental();

CREATE TABLE calendario.assento_notificacoes (
    id uuid NOT NULL,
    assento_id uuid NOT NULL,
    destinatario_paroquia character varying(255) NOT NULL,
    emitida_em date NOT NULL,
    enviada_em date,
    registrada_por character varying(128),
    criado_em_utc timestamp with time zone NOT NULL,
    atualizado_em_utc timestamp with time zone NOT NULL,
    criado_por character varying(128) NOT NULL,
    atualizado_por character varying(128) NOT NULL,
    CONSTRAINT assento_notificacoes_pkey PRIMARY KEY (id),
    CONSTRAINT assento_notificacoes_assento_fk FOREIGN KEY (assento_id) REFERENCES calendario.assentos_sacramentais (id)
);
COMMENT ON TABLE calendario.assento_notificacoes IS 'Notificações canônicas ao pároco do local de batismo para averbação marginal (Cânn. 895, 1054, 1122).';
COMMENT ON COLUMN calendario.assento_notificacoes.id IS 'Identificador único da notificação.';
COMMENT ON COLUMN calendario.assento_notificacoes.assento_id IS 'Assento que originou a notificação.';
COMMENT ON COLUMN calendario.assento_notificacoes.destinatario_paroquia IS 'Paróquia de batismo destinatária.';
COMMENT ON COLUMN calendario.assento_notificacoes.emitida_em IS 'Data de emissão da notificação.';
COMMENT ON COLUMN calendario.assento_notificacoes.enviada_em IS 'Data de envio da notificação, quando enviada.';
COMMENT ON COLUMN calendario.assento_notificacoes.registrada_por IS 'Usuário que registrou a notificação.';
COMMENT ON COLUMN calendario.assento_notificacoes.criado_em_utc IS 'Instante UTC de criação imutável do registro.';
COMMENT ON COLUMN calendario.assento_notificacoes.atualizado_em_utc IS 'Instante UTC da última atualização do registro.';
COMMENT ON COLUMN calendario.assento_notificacoes.criado_por IS 'Identificador do responsável pela criação.';
COMMENT ON COLUMN calendario.assento_notificacoes.atualizado_por IS 'Identificador do responsável pela última atualização.';
CREATE INDEX idx_assento_notificacoes_assento ON calendario.assento_notificacoes USING btree (assento_id);
CREATE TRIGGER trg_assento_notificacoes_sem_delete BEFORE DELETE ON calendario.assento_notificacoes
    FOR EACH ROW EXECUTE FUNCTION calendario.bloquear_alteracao_sacramental();

CREATE TABLE calendario.assento_historico (
    id uuid NOT NULL,
    assento_id uuid NOT NULL,
    acao character varying(32) NOT NULL,
    usuario_id uuid,
    ator character varying(128) NOT NULL,
    perfil character varying(64),
    endereco_ip character varying(64),
    valores_anteriores text,
    valores_posteriores text,
    ocorrido_em_utc timestamp with time zone NOT NULL,
    criado_em_utc timestamp with time zone NOT NULL,
    atualizado_em_utc timestamp with time zone NOT NULL,
    criado_por character varying(128) NOT NULL,
    atualizado_por character varying(128) NOT NULL,
    CONSTRAINT assento_historico_pkey PRIMARY KEY (id),
    CONSTRAINT assento_historico_assento_fk FOREIGN KEY (assento_id) REFERENCES calendario.assentos_sacramentais (id)
);
COMMENT ON TABLE calendario.assento_historico IS 'Trilha estrita e append-only do assento (Cân. 535 §4): quem, perfil, IP, ação e valores prévios/posteriores.';
COMMENT ON COLUMN calendario.assento_historico.id IS 'Identificador único da entrada de histórico.';
COMMENT ON COLUMN calendario.assento_historico.assento_id IS 'Assento ao qual a entrada se refere.';
COMMENT ON COLUMN calendario.assento_historico.acao IS 'Ação executada (lavrar, atualizar, homologar, vincular, averbar, retificar, cancelar etc.).';
COMMENT ON COLUMN calendario.assento_historico.usuario_id IS 'Identificador interno do usuário autor.';
COMMENT ON COLUMN calendario.assento_historico.ator IS 'Login do usuário autor.';
COMMENT ON COLUMN calendario.assento_historico.perfil IS 'Perfil (papel) do autor no momento da ação.';
COMMENT ON COLUMN calendario.assento_historico.endereco_ip IS 'Endereço IP de origem da requisição.';
COMMENT ON COLUMN calendario.assento_historico.valores_anteriores IS 'Estado serializado antes da ação.';
COMMENT ON COLUMN calendario.assento_historico.valores_posteriores IS 'Estado serializado depois da ação.';
COMMENT ON COLUMN calendario.assento_historico.ocorrido_em_utc IS 'Instante UTC em que a ação ocorreu.';
COMMENT ON COLUMN calendario.assento_historico.criado_em_utc IS 'Instante UTC de criação imutável do registro.';
COMMENT ON COLUMN calendario.assento_historico.atualizado_em_utc IS 'Instante UTC da última atualização do registro.';
COMMENT ON COLUMN calendario.assento_historico.criado_por IS 'Identificador do responsável pela criação.';
COMMENT ON COLUMN calendario.assento_historico.atualizado_por IS 'Identificador do responsável pela última atualização.';
CREATE INDEX idx_assento_historico_assento ON calendario.assento_historico USING btree (assento_id, ocorrido_em_utc);
CREATE TRIGGER trg_assento_historico_append_only BEFORE UPDATE OR DELETE ON calendario.assento_historico
    FOR EACH ROW EXECUTE FUNCTION calendario.bloquear_alteracao_sacramental();
