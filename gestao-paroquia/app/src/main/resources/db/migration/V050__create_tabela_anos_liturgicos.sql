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
(2034, 'B', '2033-11-27 00:00:00+00', '2034-12-02 23:59:59+00', CURRENT_TIMESTAMP),(2035, 'C', '2034-12-03 00:00:00+00', '2035-12-01 23:59:59+00', CURRENT_TIMESTAMP);

-- Associar os ciclos existentes ao Ano Litúrgico 2026
UPDATE calendario.tb_ciclo_liturgico
SET ano_liturgico_ref = 2026
WHERE ano_liturgico_ref IS NULL;

-- Seed de Ciclos Litúrgicos completos para os 10 anos (2026-2035)
INSERT INTO calendario.tb_ciclo_liturgico (id, nome, tipo_ciclo, cor_liturgica, mes_inicio, dia_inicio, mes_fim, dia_fim, fator_demanda, descricao, ano_liturgico_ref)
VALUES
-- 2027
('71000000-0000-0000-0000-000000002027', 'Advento 2027', 'ADVENTO', 'ROXO', 11, 29, 12, 24, 1.8, 'Tempo de preparação e expectativa do nascimento de Cristo', 2027),
('72000000-0000-0000-0000-000000002027', 'Tempo do Natal 2027', 'NATAL', 'BRANCO', 12, 25, 1, 10, 2.0, 'Celebração do Nascimento do Senhor e Epifania', 2027),
('73000000-0000-0000-0000-000000002027', 'Quaresma 2027', 'QUARESMA', 'ROXO', 2, 10, 3, 27, 2.2, 'Tempo de penitência, conversão e Oração', 2027),
('74000000-0000-0000-0000-000000002027', 'Tempo Pascal 2027', 'PASCOA', 'BRANCO', 3, 28, 5, 16, 2.5, 'Cinquenta dias de alegria da Ressurreição até Pentecostes', 2027),
('75000000-0000-0000-0000-000000002027', 'Tempo Comum 2027', 'TEMPO_COMUM', 'VERDE', 1, 11, 11, 27, 1.0, 'Caminhada ordinária da Igreja', 2027),
('76000000-0000-0000-0000-000000002027', 'Festa Padroeira 2027', 'FESTA_PADROEIRA', 'BRANCO', 5, 4, 5, 13, 3.0, 'Novena e Festa da Padroeira Nossa Senhora de Fátima', 2027),

-- 2028
('71000000-0000-0000-0000-000000002028', 'Advento 2028', 'ADVENTO', 'ROXO', 11, 28, 12, 24, 1.8, 'Tempo de expectativa messiânica', 2028),
('72000000-0000-0000-0000-000000002028', 'Tempo do Natal 2028', 'NATAL', 'BRANCO', 12, 25, 1, 8, 2.0, 'Ciclo do Natal', 2028),
('73000000-0000-0000-0000-000000002028', 'Quaresma 2028', 'QUARESMA', 'ROXO', 3, 1, 4, 15, 2.2, 'Caminhada quaresmal', 2028),
('74000000-0000-0000-0000-000000002028', 'Tempo Pascal 2028', 'PASCOA', 'BRANCO', 4, 16, 6, 4, 2.5, 'Tempo da Ressurreição', 2028),
('75000000-0000-0000-0000-000000002028', 'Tempo Comum 2028', 'TEMPO_COMUM', 'VERDE', 1, 9, 11, 27, 1.0, 'Tempo Comum', 2028),
('76000000-0000-0000-0000-000000002028', 'Festa Padroeira 2028', 'FESTA_PADROEIRA', 'BRANCO', 5, 4, 5, 13, 3.0, 'Festividades da Padroeira', 2028),

-- 2029
('71000000-0000-0000-0000-000000002029', 'Advento 2029', 'ADVENTO', 'ROXO', 12, 3, 12, 24, 1.8, 'Advento 2029', 2029),
('72000000-0000-0000-0000-000000002029', 'Tempo do Natal 2029', 'NATAL', 'BRANCO', 12, 25, 1, 13, 2.0, 'Natal 2029', 2029),
('73000000-0000-0000-0000-000000002029', 'Quaresma 2029', 'QUARESMA', 'ROXO', 2, 14, 3, 31, 2.2, 'Quaresma 2029', 2029),
('74000000-0000-0000-0000-000000002029', 'Tempo Pascal 2029', 'PASCOA', 'BRANCO', 4, 1, 5, 20, 2.5, 'Páscoa 2029', 2029),
('75000000-0000-0000-0000-000000002029', 'Tempo Comum 2029', 'TEMPO_COMUM', 'VERDE', 1, 14, 12, 1, 1.0, 'Tempo Comum 2029', 2029),

-- 2030
('71000000-0000-0000-0000-000000002030', 'Advento 2030', 'ADVENTO', 'ROXO', 12, 2, 12, 24, 1.8, 'Advento 2030', 2030),
('72000000-0000-0000-0000-000000002030', 'Tempo do Natal 2030', 'NATAL', 'BRANCO', 12, 25, 1, 12, 2.0, 'Natal 2030', 2030),
('73000000-0000-0000-0000-000000002030', 'Quaresma 2030', 'QUARESMA', 'ROXO', 3, 6, 4, 20, 2.2, 'Quaresma 2030', 2030),
('74000000-0000-0000-0000-000000002030', 'Tempo Pascal 2030', 'PASCOA', 'BRANCO', 4, 21, 6, 9, 2.5, 'Páscoa 2030', 2030),
('75000000-0000-0000-0000-000000002030', 'Tempo Comum 2030', 'TEMPO_COMUM', 'VERDE', 1, 13, 11, 30, 1.0, 'Tempo Comum 2030', 2030),

-- 2031
('71000000-0000-0000-0000-000000002031', 'Advento 2031', 'ADVENTO', 'ROXO', 12, 1, 12, 24, 1.8, 'Advento 2031', 2031),
('72000000-0000-0000-0000-000000002031', 'Tempo do Natal 2031', 'NATAL', 'BRANCO', 12, 25, 1, 11, 2.0, 'Natal 2031', 2031),
('73000000-0000-0000-0000-000000002031', 'Quaresma 2031', 'QUARESMA', 'ROXO', 2, 26, 4, 12, 2.2, 'Quaresma 2031', 2031),
('74000000-0000-0000-0000-000000002031', 'Tempo Pascal 2031', 'PASCOA', 'BRANCO', 4, 13, 6, 1, 2.5, 'Páscoa 2031', 2031),
('75000000-0000-0000-0000-000000002031', 'Tempo Comum 2031', 'TEMPO_COMUM', 'VERDE', 1, 12, 11, 29, 1.0, 'Tempo Comum 2031', 2031),

-- 2032
('71000000-0000-0000-0000-000000002032', 'Advento 2032', 'ADVENTO', 'ROXO', 11, 30, 12, 24, 1.8, 'Advento 2032', 2032),
('72000000-0000-0000-0000-000000002032', 'Tempo do Natal 2032', 'NATAL', 'BRANCO', 12, 25, 1, 10, 2.0, 'Natal 2032', 2032),
('73000000-0000-0000-0000-000000002032', 'Quaresma 2032', 'QUARESMA', 'ROXO', 2, 11, 3, 27, 2.2, 'Quaresma 2032', 2032),
('74000000-0000-0000-0000-000000002032', 'Tempo Pascal 2032', 'PASCOA', 'BRANCO', 3, 28, 5, 16, 2.5, 'Páscoa 2032', 2032),
('75000000-0000-0000-0000-000000002032', 'Tempo Comum 2032', 'TEMPO_COMUM', 'VERDE', 1, 11, 11, 27, 1.0, 'Tempo Comum 2032', 2032),

-- 2033
('71000000-0000-0000-0000-000000002033', 'Advento 2033', 'ADVENTO', 'ROXO', 11, 28, 12, 24, 1.8, 'Advento 2033', 2033),
('72000000-0000-0000-0000-000000002033', 'Tempo do Natal 2033', 'NATAL', 'BRANCO', 12, 25, 1, 9, 2.0, 'Natal 2033', 2033),
('73000000-0000-0000-0000-000000002033', 'Quaresma 2033', 'QUARESMA', 'ROXO', 3, 2, 4, 16, 2.2, 'Quaresma 2033', 2033),
('74000000-0000-0000-0000-000000002033', 'Tempo Pascal 2033', 'PASCOA', 'BRANCO', 4, 17, 6, 5, 2.5, 'Páscoa 2033', 2033),
('75000000-0000-0000-0000-000000002033', 'Tempo Comum 2033', 'TEMPO_COMUM', 'VERDE', 1, 10, 11, 26, 1.0, 'Tempo Comum 2033', 2033),

-- 2034
('71000000-0000-0000-0000-000000002034', 'Advento 2034', 'ADVENTO', 'ROXO', 11, 27, 12, 24, 1.8, 'Advento 2034', 2034),
('72000000-0000-0000-0000-000000002034', 'Tempo do Natal 2034', 'NATAL', 'BRANCO', 12, 25, 1, 8, 2.0, 'Natal 2034', 2034),
('73000000-0000-0000-0000-000000002034', 'Quaresma 2034', 'QUARESMA', 'ROXO', 2, 22, 4, 8, 2.2, 'Quaresma 2034', 2034),
('74000000-0000-0000-0000-000000002034', 'Tempo Pascal 2034', 'PASCOA', 'BRANCO', 4, 9, 5, 28, 2.5, 'Páscoa 2034', 2034),
('75000000-0000-0000-0000-000000002034', 'Tempo Comum 2034', 'TEMPO_COMUM', 'VERDE', 1, 9, 12, 2, 1.0, 'Tempo Comum 2034', 2034),

-- 2035
('71000000-0000-0000-0000-000000002035', 'Advento 2035', 'ADVENTO', 'ROXO', 12, 3, 12, 24, 1.8, 'Advento 2035', 2035),
('72000000-0000-0000-0000-000000002035', 'Tempo do Natal 2035', 'NATAL', 'BRANCO', 12, 25, 1, 13, 2.0, 'Natal 2035', 2035),
('73000000-0000-0000-0000-000000002035', 'Quaresma 2035', 'QUARESMA', 'ROXO', 2, 7, 3, 24, 2.2, 'Quaresma 2035', 2035),
('74000000-0000-0000-0000-000000002035', 'Tempo Pascal 2035', 'PASCOA', 'BRANCO', 3, 25, 5, 13, 2.5, 'Páscoa 2035', 2035),
('75000000-0000-0000-0000-000000002035', 'Tempo Comum 2035', 'TEMPO_COMUM', 'VERDE', 1, 14, 12, 1, 1.0, 'Tempo Comum 2035', 2035);
