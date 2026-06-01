UPDATE calendario.projetos_eventos SET status = 'EM_ANDAMENTO' WHERE status = 'ATIVO';
UPDATE calendario.projetos_eventos SET status = 'CONCLUIDO' WHERE status = 'INATIVO';

CREATE TABLE IF NOT EXISTS calendario.projeto_solicitacoes_aprovacao (
    id UUID PRIMARY KEY,
    projeto_id UUID,
    tipo_solicitacao VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL,
    solicitante_id VARCHAR(255) NOT NULL,
    solicitante_papel VARCHAR(64) NOT NULL,
    solicitante_tipo_organizacao VARCHAR(64) NOT NULL,
    action_payload_json VARCHAR(4000),
    cancelar_eventos_vinculados BOOLEAN DEFAULT FALSE,
    criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL,
    decidido_em_utc TIMESTAMP WITH TIME ZONE,
    aprovador_id VARCHAR(255),
    decision_observacao VARCHAR(2000),
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_proj_aprov_status ON calendario.projeto_solicitacoes_aprovacao (status);
