CREATE TABLE IF NOT EXISTS calendario.membros (
    id UUID PRIMARY KEY,
    nome_completo VARCHAR(255) NOT NULL,
    data_nascimento DATE,
    email VARCHAR(255),
    telefone VARCHAR(20),
    endereco VARCHAR(500),
    usuario_id UUID,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    data_batismo DATE,
    local_batismo VARCHAR(255),
    data_crisma DATE,
    data_matrimonio DATE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_membros_usuario FOREIGN KEY (usuario_id) REFERENCES calendario.usuarios(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_membros_nome ON calendario.membros (nome_completo);
CREATE INDEX IF NOT EXISTS idx_membros_ativo ON calendario.membros (ativo);
