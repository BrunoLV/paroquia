CREATE TABLE IF NOT EXISTS calendario.evento_idempotency (
    idempotency_key VARCHAR(128) PRIMARY KEY,
    request_hash VARCHAR(64) NOT NULL,
    evento_id UUID NOT NULL,
    response_status INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_evento_idempotency_evento_id
    ON calendario.evento_idempotency (evento_id);
