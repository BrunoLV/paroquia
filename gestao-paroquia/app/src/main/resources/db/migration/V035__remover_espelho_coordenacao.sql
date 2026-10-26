-- A coordenação deixa de ser copiada em texto na organização: o coordenador e o vice-coordenador são
-- derivados, na leitura, dos vínculos de membro ativos com os papéis de coordenação.
ALTER TABLE calendario.organizacao DROP COLUMN IF EXISTS coordenador;
ALTER TABLE calendario.organizacao DROP COLUMN IF EXISTS vice_coordenador;
