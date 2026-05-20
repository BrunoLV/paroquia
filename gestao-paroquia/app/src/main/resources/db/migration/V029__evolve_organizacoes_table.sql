ALTER TABLE calendario.organizacoes ADD COLUMN tipo VARCHAR(32) NOT NULL DEFAULT 'OUTRO';
ALTER TABLE calendario.organizacoes ADD COLUMN contato VARCHAR(255);
ALTER TABLE calendario.organizacoes ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE calendario.organizacoes ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE calendario.organizacoes ADD CONSTRAINT uk_organizacoes_nome UNIQUE (nome);
