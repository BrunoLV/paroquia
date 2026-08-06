-- Remove as linhas territoriais já migradas para unidade_territorial em V063 — o que resta em
-- organizacoes são apenas grupos paroquiais de fato (Pastoral, Movimento, Conselho, Ministério).
DELETE FROM calendario.organizacoes WHERE tipo IN ('CLERO', 'MATRIZ', 'COMUNIDADE');

-- Colapsa os 4 tipos de conselho legados em CONSELHO, e OUTRO em MINISTERIO.
UPDATE calendario.organizacoes
    SET tipo = 'CONSELHO'
    WHERE tipo IN ('CONSELHO_PASTORAL', 'CONSELHO_ECONOMICO', 'CPP', 'CPC');
UPDATE calendario.organizacoes
    SET tipo = 'MINISTERIO'
    WHERE tipo = 'OUTRO';

ALTER TABLE calendario.organizacoes RENAME TO grupo_paroquial;
