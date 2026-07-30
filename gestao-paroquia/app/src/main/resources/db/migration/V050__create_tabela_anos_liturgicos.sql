CREATE TABLE IF NOT EXISTS calendario.anos_liturgicos (
    ano INT PRIMARY KEY,
    ciclo_letra CHAR(1) NOT NULL,
    data_inicio_advento TIMESTAMP WITH TIME ZONE NOT NULL,
    data_fim_comum TIMESTAMP WITH TIME ZONE NOT NULL,
    atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE calendario.tb_ciclo_liturgico ADD COLUMN IF NOT EXISTS ano_liturgico_ref INT;

ALTER TABLE calendario.tb_ciclo_liturgico ADD CONSTRAINT fk_tb_ciclo_liturgico_ano_ref FOREIGN KEY (ano_liturgico_ref) REFERENCES calendario.anos_liturgicos(ano);

ALTER TABLE calendario.eventos ADD COLUMN IF NOT EXISTS ano_liturgico_ref INT;
ALTER TABLE calendario.eventos ADD COLUMN IF NOT EXISTS ciclo_liturgico_id UUID;

ALTER TABLE calendario.eventos ADD CONSTRAINT fk_eventos_ano_liturgico FOREIGN KEY (ano_liturgico_ref) REFERENCES calendario.anos_liturgicos(ano);
ALTER TABLE calendario.eventos ADD CONSTRAINT fk_eventos_ciclo_liturgico FOREIGN KEY (ciclo_liturgico_id) REFERENCES calendario.tb_ciclo_liturgico(id);

-- Seed 10 Anos Litúrgicos (2026 - 2035)
INSERT INTO calendario.anos_liturgicos (ano, ciclo_letra, data_inicio_advento, data_fim_comum, atualizado_em_utc)
VALUES
(2026, 'C', '2025-11-30 00:00:00+00', '2026-11-28 23:59:59+00', CURRENT_TIMESTAMP),
(2027, 'A', '2026-11-29 00:00:00+00', '2027-11-27 23:59:59+00', CURRENT_TIMESTAMP),
(2028, 'B', '2027-11-28 00:00:00+00', '2028-12-02 23:59:59+00', CURRENT_TIMESTAMP),
(2029, 'C', '2028-12-03 00:00:00+00', '2029-12-01 23:59:59+00', CURRENT_TIMESTAMP),
(2030, 'A', '2029-12-02 00:00:00+00', '2030-11-30 23:59:59+00', CURRENT_TIMESTAMP),
(2031, 'B', '2030-12-01 00:00:00+00', '2031-11-29 23:59:59+00', CURRENT_TIMESTAMP),
(2032, 'C', '2031-11-30 00:00:00+00', '2032-11-27 23:59:59+00', CURRENT_TIMESTAMP),
(2033, 'A', '2032-11-28 00:00:00+00', '2033-11-26 23:59:59+00', CURRENT_TIMESTAMP),
(2034, 'B', '2033-11-27 00:00:00+00', '2034-12-02 23:59:59+00', CURRENT_TIMESTAMP),
(2035, 'C', '2034-12-03 00:00:00+00', '2035-12-01 23:59:59+00', CURRENT_TIMESTAMP);

-- Associar os ciclos existentes ao Ano Litúrgico 2026
UPDATE calendario.tb_ciclo_liturgico
SET ano_liturgico_ref = 2026
WHERE ano_liturgico_ref IS NULL;
