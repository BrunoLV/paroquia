ALTER TABLE calendario.eventos 
ADD COLUMN igreja_vinculo_id UUID REFERENCES calendario.organizacoes(id);
