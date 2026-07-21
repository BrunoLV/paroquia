-- Create table for official pastorals and movements
CREATE TABLE calendario.pastorais_movimentos_oficiais (
    id UUID PRIMARY KEY,
    nome VARCHAR(150) NOT NULL UNIQUE,
    tipo VARCHAR(50) NOT NULL,
    version INTEGER NOT NULL DEFAULT 0
);

-- Add foreign key to organizacoes
ALTER TABLE calendario.organizacoes ADD COLUMN pastoral_oficial_id UUID;
ALTER TABLE calendario.organizacoes ADD CONSTRAINT fk_organizacoes_pastoral_oficial 
    FOREIGN KEY (pastoral_oficial_id) REFERENCES calendario.pastorais_movimentos_oficiais (id);

-- Update enum/type values in organizacoes
UPDATE calendario.organizacoes SET tipo = 'CONSELHO_PASTORAL' WHERE tipo = 'CONSELHO';
DELETE FROM calendario.organizacoes WHERE tipo = 'LAICATO';
