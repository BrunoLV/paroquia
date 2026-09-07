-- V071__adicionar_colunas_sacramentos_fiel.sql
-- Adição de colunas booleanas explícitas para sacramentos recebidos pelo Fiel

ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS batizado BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS primeira_comunhao BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS crismado BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE calendario.fieis ADD COLUMN IF NOT EXISTS casado_igreja_catolica BOOLEAN NOT NULL DEFAULT FALSE;

-- Atualizar valores iniciais com base nos registros existentes que possuem dados ou datas
UPDATE calendario.fieis SET batizado = TRUE WHERE data_batismo IS NOT NULL OR paroquia_batismo IS NOT NULL OR livro_batismo IS NOT NULL;
UPDATE calendario.fieis SET primeira_comunhao = TRUE WHERE data_primeira_comunhao IS NOT NULL OR paroquia_primeira_comunhao IS NOT NULL OR turma_catequese IS NOT NULL;
UPDATE calendario.fieis SET crismado = TRUE WHERE data_crisma IS NOT NULL OR paroquia_crisma IS NOT NULL OR celebrante_crisma IS NOT NULL;
UPDATE calendario.fieis SET casado_igreja_catolica = TRUE WHERE data_matrimonio IS NOT NULL OR paroquia_matrimonio IS NOT NULL OR certidao_matrimonio IS NOT NULL;
