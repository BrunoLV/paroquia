-- Nome civil reproduzido no livro/certidão: retido somente para a finalidade sacramental.
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS nome_certidao VARCHAR(255) NULL;
UPDATE calendario.fieis SET nome_certidao = nome WHERE nome_certidao IS NULL;
