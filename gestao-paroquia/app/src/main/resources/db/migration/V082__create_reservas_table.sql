-- V082: Criação da tabela de reservas para dependências físicas (locais) e bens móveis (ativos patrimoniais)

CREATE TABLE IF NOT EXISTS calendario.reservas (
    id UUID PRIMARY KEY,
    tipo_item VARCHAR(20) NOT NULL,
    item_id UUID NOT NULL,
    data_hora_inicio TIMESTAMP WITH TIME ZONE NOT NULL,
    data_hora_fim TIMESTAMP WITH TIME ZONE NOT NULL,
    buffer_minutos INTEGER NOT NULL DEFAULT 30,
    descricao TEXT NOT NULL,
    solicitante_id UUID NOT NULL,
    pastoral_movimento_id UUID NOT NULL,
    evento_id UUID,
    grupo_recorrencia_id UUID,
    status VARCHAR(32) NOT NULL,
    aprovador_id UUID,
    aprovado_em TIMESTAMP WITH TIME ZONE,
    motivo_cancelamento TEXT,
    cancelado_por_id UUID,
    check_avarias TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_reservas_periodo CHECK (data_hora_fim > data_hora_inicio),
    CONSTRAINT chk_reservas_tipo_item CHECK (tipo_item IN ('LOCAL', 'ATIVO')),
    CONSTRAINT chk_reservas_status CHECK (status IN ('PENDENTE', 'CONFIRMADA', 'RETIRADO', 'FINALIZADA', 'CANCELADA', 'EXPIRADA')),
    CONSTRAINT fk_reservas_solicitante FOREIGN KEY (solicitante_id) REFERENCES calendario.usuarios (id),
    CONSTRAINT fk_reservas_pastoral FOREIGN KEY (pastoral_movimento_id) REFERENCES calendario.organizacoes (id),
    CONSTRAINT fk_reservas_evento FOREIGN KEY (evento_id) REFERENCES calendario.eventos (id)
);

-- Índices de concorrência e busca operacional
CREATE INDEX IF NOT EXISTS idx_reservas_concorrencia 
    ON calendario.reservas (tipo_item, item_id, status, data_hora_inicio, data_hora_fim);

CREATE INDEX IF NOT EXISTS idx_reservas_expiracao 
    ON calendario.reservas (status, data_hora_inicio) 
    WHERE status = 'PENDENTE' AND deleted_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_reservas_evento_id 
    ON calendario.reservas (evento_id);

CREATE INDEX IF NOT EXISTS idx_reservas_recorrencia_id 
    ON calendario.reservas (grupo_recorrencia_id);

CREATE INDEX IF NOT EXISTS idx_reservas_solicitante 
    ON calendario.reservas (solicitante_id, status);

CREATE INDEX IF NOT EXISTS idx_reservas_pastoral 
    ON calendario.reservas (pastoral_movimento_id, status);

-- Metadados conceituais DDL
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
COMMENT ON COLUMN calendario.reservas.created_at IS 'Data e hora de criação do registro.';
COMMENT ON COLUMN calendario.reservas.updated_at IS 'Data e hora da última modificação.';
COMMENT ON COLUMN calendario.reservas.deleted_at IS 'Data de exclusão lógica (soft delete).';
COMMENT ON COLUMN calendario.reservas.version IS 'Controle de versão para concorrência otimista (JPA).';
