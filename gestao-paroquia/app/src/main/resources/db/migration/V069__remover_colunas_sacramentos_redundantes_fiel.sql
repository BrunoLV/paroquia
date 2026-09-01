-- V069__remover_colunas_sacramentos_redundantes_fiel.sql
-- Remoção de colunas booleanas de sacramentos redundantes da tabela fieis

ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS batizado;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS primeira_comunhao;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS crismado;
ALTER TABLE calendario.fieis DROP COLUMN IF EXISTS casado_igreja_catolica;
