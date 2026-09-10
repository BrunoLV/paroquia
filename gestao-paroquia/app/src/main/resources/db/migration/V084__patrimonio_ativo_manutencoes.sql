-- V084: Tabela dedicada de manutenções de ativos patrimoniais.
--   Substitui a modelagem anterior (linhas com tipo=MANUTENCAO em alocacoes_ativos, removida na V085).
--   Cada linha é um período de indisponibilidade técnica; data_fim_real nula indica manutenção em aberto.

CREATE TABLE IF NOT EXISTS calendario.ativo_manutencoes (
    id UUID PRIMARY KEY,
    ativo_id UUID NOT NULL,
    descricao_reparo TEXT NOT NULL,
    data_inicio TIMESTAMP WITH TIME ZONE NOT NULL,
    data_fim_prevista TIMESTAMP WITH TIME ZONE NOT NULL,
    data_fim_real TIMESTAMP WITH TIME ZONE,
    registrado_por_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_ativo_manutencoes_ativo FOREIGN KEY (ativo_id)
        REFERENCES calendario.ativos_patrimoniais (id),
    CONSTRAINT chk_ativo_manutencoes_periodo CHECK (data_fim_prevista > data_inicio)
);

CREATE INDEX IF NOT EXISTS idx_ativo_manutencoes_ativo
    ON calendario.ativo_manutencoes (ativo_id, data_inicio);

CREATE INDEX IF NOT EXISTS idx_ativo_manutencoes_abertas
    ON calendario.ativo_manutencoes (ativo_id, data_fim_real);

COMMENT ON TABLE calendario.ativo_manutencoes IS 'Períodos de manutenção (conserto, revisão, reparo) que retiram um ativo patrimonial de circulação e bloqueiam o calendário de reservas no intervalo.';
COMMENT ON COLUMN calendario.ativo_manutencoes.id IS 'Identificador primário único da manutenção (UUID).';
COMMENT ON COLUMN calendario.ativo_manutencoes.ativo_id IS 'Ativo patrimonial sob manutenção.';
COMMENT ON COLUMN calendario.ativo_manutencoes.descricao_reparo IS 'Relatório do problema e do serviço a ser executado.';
COMMENT ON COLUMN calendario.ativo_manutencoes.data_inicio IS 'Início do período de indisponibilidade.';
COMMENT ON COLUMN calendario.ativo_manutencoes.data_fim_prevista IS 'Previsão de retorno do bem; sempre posterior a data_inicio.';
COMMENT ON COLUMN calendario.ativo_manutencoes.data_fim_real IS 'Registro efetivo da conclusão do reparo; nulo enquanto a manutenção está em aberto.';
COMMENT ON COLUMN calendario.ativo_manutencoes.registrado_por_id IS 'Usuário que enviou o item para manutenção (auditoria).';
COMMENT ON COLUMN calendario.ativo_manutencoes.created_at IS 'Instante de criação do registro.';
COMMENT ON COLUMN calendario.ativo_manutencoes.updated_at IS 'Instante da última alteração do registro.';
