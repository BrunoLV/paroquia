-- Rename existing 'PLANEJAMENTO' status in anos_paroquiais to 'EM_PLANEJAMENTO'
UPDATE calendario.anos_paroquiais
SET status = 'EM_PLANEJAMENTO'
WHERE status = 'PLANEJAMENTO';
