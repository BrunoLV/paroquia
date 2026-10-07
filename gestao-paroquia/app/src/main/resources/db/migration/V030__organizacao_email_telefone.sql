-- Contato de organização separado em e-mail e telefone; a coluna contato permanece como espelho legado de leitura.
ALTER TABLE calendario.organizacao ADD COLUMN email character varying(255);
ALTER TABLE calendario.organizacao ADD COLUMN telefone character varying(32);

UPDATE calendario.organizacao SET email = contato WHERE contato LIKE '%@%' AND email IS NULL;
UPDATE calendario.organizacao SET telefone = contato WHERE contato IS NOT NULL AND contato NOT LIKE '%@%' AND telefone IS NULL;

COMMENT ON COLUMN calendario.organizacao.email IS 'E-mail institucional da organização.';
COMMENT ON COLUMN calendario.organizacao.telefone IS 'Telefone institucional da organização.';
COMMENT ON COLUMN calendario.organizacao.contato IS 'Espelho legado de leitura: e-mail ou, na falta dele, telefone. A fonte da verdade são as colunas email e telefone.';
