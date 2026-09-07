-- Migração de compatibilidade para valores legados de grau_ordem no módulo do Clero
UPDATE calendario.clerigos
SET grau_ordem = 'PADRE_DIOCESANO'
WHERE grau_ordem = 'PRESBITERO';

UPDATE calendario.clerigos
SET grau_ordem = 'DIACONO_PERMANENTE'
WHERE grau_ordem = 'DIACONO';
