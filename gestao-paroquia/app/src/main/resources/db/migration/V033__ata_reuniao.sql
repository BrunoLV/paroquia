/**
 * MIGRAÇÃO: V033__ata_reuniao.sql
 * CONTEXTO: Substitui o registro simples de decisões de conselho (cpc_decisoes) pelo livro de atas de reuniões
 * da paróquia e das comunidades, por tipo de reunião, com itens de pauta, deliberações, presença e trilha de
 * auditoria própria.
 * REGRA: ata homologada ou registrada é imutável (RN-CON-03): trigger veda qualquer UPDATE a partir daí e todo
 * DELETE; correções nascem como nova ata retificadora ligada à original. A trilha é append-only.
 * NOTA: cpc_decisoes continha apenas dados de teste (decisão do projeto), por isso é removida sem migração.
 */

DROP TABLE calendario.cpc_decisoes;

CREATE FUNCTION calendario.bloquear_alteracao_ata() RETURNS trigger
    LANGUAGE plpgsql AS $$
BEGIN
    RAISE EXCEPTION 'Ata selada e trilha de auditoria sao imutaveis: % em % nao e permitido.', TG_OP, TG_TABLE_NAME;
END;
$$;
COMMENT ON FUNCTION calendario.bloquear_alteracao_ata() IS 'Veda alteração de atas seladas, exclusão de atas e qualquer mudança na trilha de auditoria das atas.';

CREATE FUNCTION calendario.bloquear_edicao_ata_selada() RETURNS trigger
    LANGUAGE plpgsql AS $$
BEGIN
    IF OLD.status IN ('HOMOLOGADA', 'REGISTRADA') THEN
        RAISE EXCEPTION 'Ata selada e imutavel: use a ata retificadora (RN-CON-03).';
    END IF;
    RETURN NEW;
END;
$$;
COMMENT ON FUNCTION calendario.bloquear_edicao_ata_selada() IS 'Impede qualquer UPDATE em ata já homologada ou registrada (RN-CON-03).';

CREATE TABLE calendario.ata_reuniao (
    id uuid NOT NULL,
    tipo character varying(32) NOT NULL,
    organizacao_convocante_id uuid NOT NULL,
    organizacao_associada_id uuid,
    titulo character varying(160) NOT NULL,
    data_reuniao date NOT NULL,
    carater character varying(16) NOT NULL,
    modalidade character varying(16) NOT NULL,
    status character varying(24) NOT NULL,
    conteudo text NOT NULL,
    numero integer,
    ano integer,
    membros_convocados integer,
    redator_id character varying(128) NOT NULL,
    homologador_id character varying(128),
    motivo_veto character varying(2000),
    decidido_em_utc timestamp with time zone,
    ata_retificada_id uuid,
    justificativa_retificacao character varying(2000),
    criado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    CONSTRAINT ata_reuniao_pkey PRIMARY KEY (id),
    CONSTRAINT ata_reuniao_convocante_fk FOREIGN KEY (organizacao_convocante_id) REFERENCES calendario.organizacao (id),
    CONSTRAINT ata_reuniao_associada_fk FOREIGN KEY (organizacao_associada_id) REFERENCES calendario.organizacao (id),
    CONSTRAINT ata_reuniao_retificada_fk FOREIGN KEY (ata_retificada_id) REFERENCES calendario.ata_reuniao (id),
    CONSTRAINT ata_reuniao_status_ck CHECK (status IN ('RASCUNHO', 'PENDENTE_HOMOLOGACAO', 'HOMOLOGADA', 'VETADA', 'REGISTRADA')),
    CONSTRAINT ata_reuniao_numeracao_ck CHECK ((numero IS NULL) = (ano IS NULL)),
    CONSTRAINT ata_reuniao_selada_numerada_ck CHECK ((status IN ('HOMOLOGADA', 'REGISTRADA')) = (numero IS NOT NULL))
);
COMMENT ON TABLE calendario.ata_reuniao IS 'Ata de reunião da paróquia ou de comunidade, por tipo de reunião. Conteúdo estruturado (pauta, deliberações, presença) em JSON; colunas escalares são projeção para filtro e consulta.';
COMMENT ON COLUMN calendario.ata_reuniao.id IS 'Identificador único da ata.';
COMMENT ON COLUMN calendario.ata_reuniao.tipo IS 'Tipo de reunião (CPP, CPC, CAEP, conjunta, pastoral, assembleias etc.); define homologação, quórum, presença, votação e redator.';
COMMENT ON COLUMN calendario.ata_reuniao.organizacao_convocante_id IS 'Organização que convoca a reunião; dona da série numérica das atas e do escopo de leitura.';
COMMENT ON COLUMN calendario.ata_reuniao.organizacao_associada_id IS 'Segunda organização da reunião conjunta (CAEP); nula nos demais tipos.';
COMMENT ON COLUMN calendario.ata_reuniao.titulo IS 'Título da ata, projetado do conteúdo para listagem.';
COMMENT ON COLUMN calendario.ata_reuniao.data_reuniao IS 'Data da reunião, projetada do conteúdo para filtro por período.';
COMMENT ON COLUMN calendario.ata_reuniao.carater IS 'Caráter da convocação: ORDINARIA ou EXTRAORDINARIA.';
COMMENT ON COLUMN calendario.ata_reuniao.modalidade IS 'Forma de realização: PRESENCIAL, VIRTUAL ou HIBRIDA.';
COMMENT ON COLUMN calendario.ata_reuniao.status IS 'Ciclo de vida: RASCUNHO, PENDENTE_HOMOLOGACAO, HOMOLOGADA, VETADA ou REGISTRADA (tipos sem homologação).';
COMMENT ON COLUMN calendario.ata_reuniao.conteudo IS 'Conteúdo editável serializado: local, presença (só identificadores de fiéis, sem nomes), ata anterior, itens de pauta e deliberações com votação e conflitos de interesse.';
COMMENT ON COLUMN calendario.ata_reuniao.numero IS 'Número sequencial da ata na série da organização convocante no ano; atribuído ao selar.';
COMMENT ON COLUMN calendario.ata_reuniao.ano IS 'Ano civil da série numérica; atribuído ao selar.';
COMMENT ON COLUMN calendario.ata_reuniao.membros_convocados IS 'Total de membros ativos convocados, congelado na submissão para apurar o quórum de forma estável.';
COMMENT ON COLUMN calendario.ata_reuniao.redator_id IS 'Identificador do autor da redação (nunca nome pessoal).';
COMMENT ON COLUMN calendario.ata_reuniao.homologador_id IS 'Identificador do Pároco que homologou ou vetou.';
COMMENT ON COLUMN calendario.ata_reuniao.motivo_veto IS 'Motivo do veto; mantido à vista do redator até a nova submissão.';
COMMENT ON COLUMN calendario.ata_reuniao.decidido_em_utc IS 'Instante UTC da homologação, veto ou registro.';
COMMENT ON COLUMN calendario.ata_reuniao.ata_retificada_id IS 'Ata selada que esta ata retifica; nula nas atas originais.';
COMMENT ON COLUMN calendario.ata_reuniao.justificativa_retificacao IS 'Justificativa formal da retificação.';
COMMENT ON COLUMN calendario.ata_reuniao.criado_em_utc IS 'Instante UTC de criação imutável do registro.';
COMMENT ON COLUMN calendario.ata_reuniao.atualizado_em_utc IS 'Instante UTC da última atualização do registro.';
COMMENT ON COLUMN calendario.ata_reuniao.criado_por IS 'Identificador do responsável pela criação.';
COMMENT ON COLUMN calendario.ata_reuniao.atualizado_por IS 'Identificador do responsável pela última atualização.';
CREATE UNIQUE INDEX uk_ata_reuniao_numeracao ON calendario.ata_reuniao USING btree (organizacao_convocante_id, ano, numero) WHERE numero IS NOT NULL;
CREATE INDEX idx_ata_reuniao_convocante_data ON calendario.ata_reuniao USING btree (organizacao_convocante_id, data_reuniao DESC);
CREATE INDEX idx_ata_reuniao_associada ON calendario.ata_reuniao USING btree (organizacao_associada_id) WHERE organizacao_associada_id IS NOT NULL;
CREATE INDEX idx_ata_reuniao_status ON calendario.ata_reuniao USING btree (status);
CREATE TRIGGER trg_ata_reuniao_selada_imutavel BEFORE UPDATE ON calendario.ata_reuniao
    FOR EACH ROW EXECUTE FUNCTION calendario.bloquear_edicao_ata_selada();
CREATE TRIGGER trg_ata_reuniao_sem_delete BEFORE DELETE ON calendario.ata_reuniao
    FOR EACH ROW EXECUTE FUNCTION calendario.bloquear_alteracao_ata();

CREATE TABLE calendario.ata_historico (
    id uuid NOT NULL,
    ata_id uuid NOT NULL,
    acao character varying(32) NOT NULL,
    usuario_id uuid,
    ator character varying(128) NOT NULL,
    perfil character varying(64),
    endereco_ip character varying(64),
    valores_anteriores text,
    valores_posteriores text,
    ocorrido_em_utc timestamp with time zone NOT NULL,
    criado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    CONSTRAINT ata_historico_pkey PRIMARY KEY (id),
    CONSTRAINT ata_historico_ata_fk FOREIGN KEY (ata_id) REFERENCES calendario.ata_reuniao (id)
);
COMMENT ON TABLE calendario.ata_historico IS 'Trilha append-only de auditoria da ata: quem, perfil, IP, ação e estado anterior e posterior, incluindo cada exportação.';
COMMENT ON COLUMN calendario.ata_historico.id IS 'Identificador único da entrada da trilha.';
COMMENT ON COLUMN calendario.ata_historico.ata_id IS 'Ata a que a entrada se refere.';
COMMENT ON COLUMN calendario.ata_historico.acao IS 'Ação executada (RASCUNHAR, REDIGIR, SUBMETER, HOMOLOGAR, VETAR, REABRIR, RETIFICAR, EXPORTAR_PDF, EXPORTAR_DOCX).';
COMMENT ON COLUMN calendario.ata_historico.usuario_id IS 'Identificador interno do usuário autor.';
COMMENT ON COLUMN calendario.ata_historico.ator IS 'Login do usuário autor.';
COMMENT ON COLUMN calendario.ata_historico.perfil IS 'Perfil (papel) do autor no momento da ação.';
COMMENT ON COLUMN calendario.ata_historico.endereco_ip IS 'Endereço IP de origem da requisição.';
COMMENT ON COLUMN calendario.ata_historico.valores_anteriores IS 'Estado serializado antes da ação.';
COMMENT ON COLUMN calendario.ata_historico.valores_posteriores IS 'Estado serializado depois da ação, ou os parâmetros da exportação.';
COMMENT ON COLUMN calendario.ata_historico.ocorrido_em_utc IS 'Instante UTC em que a ação ocorreu.';
COMMENT ON COLUMN calendario.ata_historico.criado_em_utc IS 'Instante UTC de criação imutável do registro.';
COMMENT ON COLUMN calendario.ata_historico.atualizado_em_utc IS 'Instante UTC da última atualização do registro.';
COMMENT ON COLUMN calendario.ata_historico.criado_por IS 'Identificador do responsável pela criação.';
COMMENT ON COLUMN calendario.ata_historico.atualizado_por IS 'Identificador do responsável pela última atualização.';
CREATE INDEX idx_ata_historico_ata ON calendario.ata_historico USING btree (ata_id, ocorrido_em_utc);
CREATE TRIGGER trg_ata_historico_append_only BEFORE UPDATE OR DELETE ON calendario.ata_historico
    FOR EACH ROW EXECUTE FUNCTION calendario.bloquear_alteracao_ata();
