ALTER TABLE calendario.usuarios ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE calendario.membros_organizacao ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
