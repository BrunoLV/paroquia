ALTER TABLE calendario.locais ADD COLUMN endereco VARCHAR(500);
ALTER TABLE calendario.locais ADD COLUMN caracteristicas TEXT;
ALTER TABLE calendario.locais ADD CONSTRAINT uk_locais_nome UNIQUE (nome);

ALTER TABLE calendario.eventos ADD COLUMN local_id UUID;
ALTER TABLE calendario.eventos ADD CONSTRAINT fk_eventos_local FOREIGN KEY (local_id) REFERENCES calendario.locais(id);
