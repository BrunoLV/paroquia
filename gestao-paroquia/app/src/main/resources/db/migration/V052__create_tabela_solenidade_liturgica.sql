CREATE TABLE IF NOT EXISTS calendario.tb_solenidade_liturgica (
    id UUID PRIMARY KEY,
    chave VARCHAR(64) NOT NULL UNIQUE,
    nome VARCHAR(160) NOT NULL,
    tipo_solenidade VARCHAR(32) NOT NULL,
    mes_fixo INT,
    dia_fixo INT,
    offset_dias_pascoa INT,
    cor_liturgica VARCHAR(32) NOT NULL,
    fator_demanda DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    nivel_prioridade INT NOT NULL DEFAULT 2,
    descricao VARCHAR(500),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_solenidade_liturgica_chave ON calendario.tb_solenidade_liturgica(chave);
CREATE INDEX IF NOT EXISTS idx_solenidade_liturgica_tipo ON calendario.tb_solenidade_liturgica(tipo_solenidade);

INSERT INTO calendario.tb_solenidade_liturgica (id, chave, nome, tipo_solenidade, mes_fixo, dia_fixo, offset_dias_pascoa, cor_liturgica, fator_demanda, nivel_prioridade, descricao, ativo)
VALUES
('b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d01', 'MAE_DE_DEUS', 'Santa Maria, Mãe de Deus', 'FIXA', 1, 1, NULL, 'BRANCO', 3.0, 2, 'Solenidade de Santa Maria, Mãe de Deus', TRUE),
('b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d02', 'SAO_JOSE', 'São José, Esposo de Maria', 'FIXA', 3, 19, NULL, 'BRANCO', 3.0, 2, 'Solenidade de São José, Patrono da Igreja', TRUE),
('b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d03', 'ANUNCIACAO', 'Anunciação do Senhor', 'FIXA', 3, 25, NULL, 'BRANCO', 3.0, 2, 'Solenidade da Anunciação do Senhor', TRUE),
('b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d04', 'NATIVIDADE_JOAO_BATISTA', 'Natividade de S. João Batista', 'FIXA', 6, 24, NULL, 'BRANCO', 3.0, 2, 'Solenidade da Natividade de São João Batista', TRUE),
('b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d05', 'SAO_PEDRO_SAO_PAULO', 'São Pedro e São Paulo', 'FIXA', 6, 29, NULL, 'VERMELHO', 3.0, 2, 'Solenidade dos Apóstolos São Pedro e São Paulo', TRUE),
('b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d06', 'ASSUNCAO_NOSSA_SENHORA', 'Assunção de Nossa Senhora', 'FIXA', 8, 15, NULL, 'BRANCO', 3.0, 2, 'Solenidade da Assunção de Nossa Senhora', TRUE),
('b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d07', 'TODOS_OS_SANTOS', 'Todos os Santos', 'FIXA', 11, 1, NULL, 'BRANCO', 3.0, 2, 'Solenidade de Todos os Santos', TRUE),
('b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d08', 'IMACULADA_CONCEICAO', 'Imaculada Conceição', 'FIXA', 12, 8, NULL, 'BRANCO', 3.0, 2, 'Solenidade da Imaculada Conceição de Nossa Senhora', TRUE),
('b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d09', 'NATAL_DO_SENHOR', 'Natal do Senhor', 'FIXA', 12, 25, NULL, 'BRANCO', 3.0, 2, 'Solenidade do Nascimento de Nosso Senhor Jesus Cristo', TRUE),
('b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d10', 'QUARTA_CINZAS', 'Quarta-feira de Cinzas', 'MOVEL', NULL, NULL, -46, 'ROXO', 2.2, 2, 'Início da Quaresma', TRUE),
('b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d11', 'QUARTA_SANTA', 'Quarta-feira Santa', 'MOVEL', NULL, NULL, -4, 'ROXO', 2.0, 2, 'Quarta-feira da Semana Santa', TRUE),
('b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d12', 'QUINTA_SANTA', 'Quinta-feira Santa (Tríduo)', 'MOVEL', NULL, NULL, -3, 'BRANCO', 2.5, 1, 'Missa da Ceia do Senhor - Tríduo Pascal', TRUE),
('b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d13', 'SEXTA_SANTA', 'Sexta-feira da Paixão', 'MOVEL', NULL, NULL, -2, 'VERMELHO', 2.5, 1, 'Celebração da Paixão do Senhor - Tríduo Pascal', TRUE),
('b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d14', 'SABADO_SANTO', 'Sábado Santo / Vigília Pascal', 'MOVEL', NULL, NULL, -1, 'BRANCO', 2.5, 1, 'Vigília Pascal no Sábado Santo - Tríduo Pascal', TRUE),
('b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d15', 'PASCOA', 'Domingo da Ressurreição (Páscoa)', 'MOVEL', NULL, NULL, 0, 'BRANCO', 3.0, 1, 'Domingo da Ressurreição do Senhor', TRUE),
('b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d16', 'ASCENSAO', 'Ascensão do Senhor', 'MOVEL', NULL, NULL, 42, 'BRANCO', 2.0, 2, 'Solenidade da Ascensão do Senhor', TRUE),
('b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d17', 'PENTECOSTES', 'Pentecostes', 'MOVEL', NULL, NULL, 49, 'VERMELHO', 2.5, 2, 'Solenidade de Pentecostes', TRUE),
('b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d18', 'TRINDADE', 'Santíssima Trindade', 'MOVEL', NULL, NULL, 56, 'BRANCO', 2.0, 2, 'Solenidade da Santíssima Trindade', TRUE),
('b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d19', 'CORPUS_CHRISTI', 'Corpus Christi', 'MOVEL', NULL, NULL, 60, 'BRANCO', 2.5, 2, 'Solenidade do Santíssimo Corpo e Sangue de Cristo', TRUE),
('b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d20', 'SAGRADO_CORACAO', 'Sagrado Coração de Jesus', 'MOVEL', NULL, NULL, 68, 'BRANCO', 2.0, 2, 'Solenidade do Sagrado Coração de Jesus', TRUE);
