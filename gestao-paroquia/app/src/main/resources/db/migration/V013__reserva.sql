/**
 * MIGRAÇÃO INICIAL: V013__reserva.sql
 * CONTEXTO: Cria reservas de locais, recursos e ativos para atividades paroquiais.
 * REGRA: Toda reserva mantém solicitante, período, estado e evidências de devolução.
 */

CREATE TABLE calendario.reservas (
    id uuid NOT NULL,
    tipo_item character varying(20) NOT NULL,
    item_id uuid NOT NULL,
    data_hora_inicio timestamp with time zone NOT NULL,
    data_hora_fim timestamp with time zone NOT NULL,
    buffer_minutos integer DEFAULT 30 NOT NULL,
    descricao text NOT NULL,
    solicitante_id uuid NOT NULL,
    pastoral_movimento_id uuid NOT NULL,
    evento_id uuid,
    grupo_recorrencia_id uuid,
    status character varying(32) NOT NULL,
    aprovador_id uuid,
    aprovado_em timestamp with time zone,
    motivo_cancelamento text,
    cancelado_por_id uuid,
    check_avarias text,
    criado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    deleted_at timestamp with time zone,
    version bigint DEFAULT 0 NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    CONSTRAINT chk_reservas_periodo CHECK ((data_hora_fim > data_hora_inicio)),
    CONSTRAINT chk_reservas_status CHECK (((status)::text = ANY ((ARRAY['PENDENTE'::character varying, 'CONFIRMADA'::character varying, 'RETIRADO'::character varying, 'FINALIZADA'::character varying, 'CANCELADA'::character varying, 'EXPIRADA'::character varying])::text[]))),
    CONSTRAINT chk_reservas_tipo_item CHECK (((tipo_item)::text = ANY ((ARRAY['LOCAL'::character varying, 'ATIVO'::character varying])::text[])))
);
COMMENT ON TABLE calendario.reservas IS 'Agendamento e alocação de dependências físicas e bens patrimoniais avulsos ou vinculados a eventos paroquiais.';
COMMENT ON COLUMN calendario.reservas.id IS 'Identificador primário único da reserva (UUID).';
COMMENT ON COLUMN calendario.reservas.tipo_item IS 'Tipo do recurso reservado: LOCAL (sala/salão) ou ATIVO (equipamento/veículo).';
COMMENT ON COLUMN calendario.reservas.item_id IS 'Identificador de chave estrangeira do Local ou Ativo patrimonial alocado.';
COMMENT ON COLUMN calendario.reservas.data_hora_inicio IS 'Instante de início de uso do recurso.';
COMMENT ON COLUMN calendario.reservas.data_hora_fim IS 'Instante de término de uso do recurso.';
COMMENT ON COLUMN calendario.reservas.buffer_minutos IS 'Margem de tempo pós-uso destinada a limpeza, organização e logística.';
COMMENT ON COLUMN calendario.reservas.descricao IS 'Finalidade, objetivo e pauta do uso do espaço ou recurso.';
COMMENT ON COLUMN calendario.reservas.solicitante_id IS 'Usuário autor do pedido de reserva.';
COMMENT ON COLUMN calendario.reservas.pastoral_movimento_id IS 'Pastoral ou movimento vinculado ao agendamento (define alçada de aprovação).';
COMMENT ON COLUMN calendario.reservas.evento_id IS 'Identificador do evento paroquial ao qual a reserva está atrelada, quando aplicável.';
COMMENT ON COLUMN calendario.reservas.grupo_recorrencia_id IS 'Identificador de série compartilhada para agendamentos recorrentes.';
COMMENT ON COLUMN calendario.reservas.status IS 'Estado da reserva: PENDENTE, CONFIRMADA, RETIRADO, FINALIZADA, CANCELADA ou EXPIRADA.';
COMMENT ON COLUMN calendario.reservas.aprovador_id IS 'Usuário responsável pelo deferimento da reserva.';
COMMENT ON COLUMN calendario.reservas.aprovado_em IS 'Data e hora do deferimento da reserva.';
COMMENT ON COLUMN calendario.reservas.motivo_cancelamento IS 'Justificativa descritiva obrigatória em caso de cancelamento da reserva.';
COMMENT ON COLUMN calendario.reservas.cancelado_por_id IS 'Usuário operador que executou o cancelamento.';
COMMENT ON COLUMN calendario.reservas.check_avarias IS 'Registro de conferência de integridade ou danos na devolução de ativos.';
COMMENT ON COLUMN calendario.reservas.criado_em_utc IS 'Timestamp UTC de criacao do registro gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.reservas.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao do registro gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.reservas.deleted_at IS 'Data de exclusão lógica (soft delete).';
COMMENT ON COLUMN calendario.reservas.version IS 'Controle de versão para concorrência otimista (JPA).';
COMMENT ON COLUMN calendario.reservas.criado_por IS 'Identificador do usuario/sistema que criou a reserva.';
COMMENT ON COLUMN calendario.reservas.atualizado_por IS 'Identificador do usuario/sistema que modificou a reserva.';
ALTER TABLE ONLY calendario.reservas
    ADD CONSTRAINT reservas_pkey PRIMARY KEY (id);
CREATE INDEX idx_reservas_concorrencia ON calendario.reservas USING btree (tipo_item, item_id, status, data_hora_inicio, data_hora_fim);
CREATE INDEX idx_reservas_evento_id ON calendario.reservas USING btree (evento_id);
CREATE INDEX idx_reservas_expiracao ON calendario.reservas USING btree (status, data_hora_inicio);
CREATE INDEX idx_reservas_pastoral ON calendario.reservas USING btree (pastoral_movimento_id, status);
CREATE INDEX idx_reservas_recorrencia_id ON calendario.reservas USING btree (grupo_recorrencia_id);
CREATE INDEX idx_reservas_solicitante ON calendario.reservas USING btree (solicitante_id, status);
ALTER TABLE ONLY calendario.reservas
    ADD CONSTRAINT fk_reservas_evento FOREIGN KEY (evento_id) REFERENCES calendario.eventos(id);
ALTER TABLE ONLY calendario.reservas
    ADD CONSTRAINT fk_reservas_pastoral FOREIGN KEY (pastoral_movimento_id) REFERENCES calendario.grupo_paroquial(id);
ALTER TABLE ONLY calendario.reservas
    ADD CONSTRAINT fk_reservas_solicitante FOREIGN KEY (solicitante_id) REFERENCES calendario.usuarios(id);


