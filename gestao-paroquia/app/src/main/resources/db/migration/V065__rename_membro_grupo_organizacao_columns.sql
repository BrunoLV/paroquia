-- membro_grupo só referencia grupo_paroquial (nunca unidade_territorial) — remove eventuais vínculos
-- legados com tipo territorial (ex.: PAROCO/VIGARIO vinculados a CLERO antes da track roles_globais_vs_escopo
-- ter movido esses papéis para papel_global). Dev only, sem dado de produção a preservar (NFR1).
DELETE FROM calendario.membro_grupo WHERE tipo_organizacao IN ('CLERO', 'MATRIZ', 'COMUNIDADE');

ALTER TABLE calendario.membro_grupo RENAME COLUMN organizacao_id TO grupo_paroquial_id;
ALTER TABLE calendario.membro_grupo RENAME COLUMN tipo_organizacao TO tipo_grupo;

UPDATE calendario.membro_grupo
    SET tipo_grupo = 'CONSELHO'
    WHERE tipo_grupo IN ('CONSELHO_PASTORAL', 'CONSELHO_ECONOMICO', 'CPP', 'CPC');
UPDATE calendario.membro_grupo
    SET tipo_grupo = 'MINISTERIO'
    WHERE tipo_grupo = 'OUTRO';
