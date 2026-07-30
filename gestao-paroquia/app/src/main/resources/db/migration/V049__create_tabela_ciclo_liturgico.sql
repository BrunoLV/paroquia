CREATE TABLE IF NOT EXISTS calendario.tb_ciclo_liturgico (
    id UUID PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    tipo_ciclo VARCHAR(40) NOT NULL,
    cor_liturgica VARCHAR(30) NOT NULL,
    mes_inicio INT NOT NULL,
    dia_inicio INT NOT NULL,
    mes_fim INT NOT NULL,
    dia_fim INT NOT NULL,
    fator_demanda DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    descricao VARCHAR(500),
    criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_ciclo_liturgico_tipo ON calendario.tb_ciclo_liturgico(tipo_ciclo);

INSERT INTO calendario.tb_ciclo_liturgico (id, nome, tipo_ciclo, cor_liturgica, mes_inicio, dia_inicio, mes_fim, dia_fim, fator_demanda, descricao, criado_em_utc, atualizado_em_utc, version)
VALUES
('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c61', 'Advento', 'ADVENTO', 'ROXO', 11, 27, 12, 24, 1.8, 'Preparação para o Nascimento de Jesus', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c62', 'Tempo do Natal', 'NATAL', 'BRANCO', 12, 25, 1, 13, 2.2, 'Celebração do Nascimento de Nosso Senhor', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c63', 'Quaresma', 'QUARESMA', 'ROXO', 2, 10, 3, 31, 2.5, 'Período de penitência e conversão', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c64', 'Semana Santa e Páscoa', 'PASCOA', 'BRANCO', 4, 1, 5, 20, 3.0, 'Celebração da Paixão, Morte e Ressurreição de Cristo', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c65', 'Festa Padroeira N. Sra. de Fátima', 'FESTA_PADROEIRA', 'DOURADO', 5, 1, 5, 31, 2.8, 'Festividades da Padroeira Paroquial', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
('a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c66', 'Tempo Comum', 'TEMPO_COMUM', 'VERDE', 6, 1, 11, 26, 1.0, 'Tempo Ordinário da Igreja', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);
