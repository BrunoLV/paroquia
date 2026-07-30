ALTER TABLE calendario.locais 
ADD COLUMN organizacao_id UUID REFERENCES calendario.organizacoes(id);
