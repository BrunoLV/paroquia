ALTER TABLE calendario.organizacoes 
ADD COLUMN parent_id UUID REFERENCES calendario.organizacoes(id);

ALTER TABLE calendario.membros_organizacao 
ADD COLUMN igreja_vinculo_id UUID REFERENCES calendario.organizacoes(id);
