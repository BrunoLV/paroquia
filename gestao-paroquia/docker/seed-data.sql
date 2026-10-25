-- NS Fatima Calendar MVP - Seed Data
-- Este script deve ser executado APÓS as migrações do Flyway.

-- Limpa todas as tabelas do esquema calendario para garantir execução limpa e sem conflitos em re-execuções
DO $$ 
DECLARE 
    r RECORD;
BEGIN
    FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'calendario' AND tablename != 'flyway_schema_history') LOOP
        EXECUTE 'TRUNCATE TABLE calendario.' || quote_ident(r.tablename) || ' RESTART IDENTITY CASCADE;';
    END LOOP;
END $$;

-- ============================================================================
-- 1. SEED DATA
-- ============================================================================

-- Pastorais e Movimentos Oficiais
INSERT INTO calendario.pastorais_movimentos_oficiais (id, nome, tipo, version)
VALUES
    -- Ação Sociotransformadora
    ('50000000-0000-0000-0000-000000000001', 'Pastoral da Criança', 'PASTORAL', 0),
    ('50000000-0000-0000-0000-000000000002', 'Pastoral da Pessoa Idosa', 'PASTORAL', 0),
    ('50000000-0000-0000-0000-000000000003', 'Pastoral Carcerária', 'PASTORAL', 0),
    ('50000000-0000-0000-0000-000000000004', 'Pastoral da Saúde', 'PASTORAL', 0),
    ('50000000-0000-0000-0000-000000000005', 'Pastoral da Sobriedade', 'PASTORAL', 0),
    ('50000000-0000-0000-0000-000000000006', 'Pastoral Afro-Brasileira', 'PASTORAL', 0),
    ('50000000-0000-0000-0000-000000000007', 'Pastoral do Migrante', 'PASTORAL', 0),
    ('50000000-0000-0000-0000-000000000008', 'Pastoral dos Brasileiros no Exterior', 'PASTORAL', 0),
    ('50000000-0000-0000-0000-000000000009', 'Pastoral da Aids', 'PASTORAL', 0),
    ('50000000-0000-0000-0000-000000000010', 'Pastoral da Mulher Marginalizada', 'PASTORAL', 0),
    -- Ação Evangelizadora (Setores Específicos)
    ('50000000-0000-0000-0000-000000000011', 'Pastoral Familiar', 'PASTORAL', 0),
    ('50000000-0000-0000-0000-000000000012', 'Pastoral da Juventude (PJ)', 'PASTORAL', 0),
    ('50000000-0000-0000-0000-000000000013', 'Pastoral da Educação', 'PASTORAL', 0),
    ('50000000-0000-0000-0000-000000000014', 'Pastoral da Comunicação (PASCOM)', 'PASTORAL', 0),
    ('50000000-0000-0000-0000-000000000015', 'Pastoral da Cultura', 'PASTORAL', 0),
    ('50000000-0000-0000-0000-000000000016', 'Pastoral Universitária', 'PASTORAL', 0),
    ('50000000-0000-0000-0000-000000000017', 'Pastoral do Menor', 'PASTORAL', 0),
    -- Ação Litúrgica e Espiritual
    ('50000000-0000-0000-0000-000000000018', 'Pastoral Litúrgica', 'PASTORAL', 0),
    ('50000000-0000-0000-0000-000000000019', 'Pastoral da Música', 'PASTORAL', 0),
    ('50000000-0000-0000-0000-000000000020', 'Pastoral do Batismo', 'PASTORAL', 0),
    ('50000000-0000-0000-0000-000000000021', 'Pastoral da Catequese (Iniciação à Vida Cristã)', 'PASTORAL', 0),
    ('50000000-0000-0000-0000-000000000022', 'Pastoral da Acolhida', 'PASTORAL', 0),
    ('50000000-0000-0000-0000-000000000023', 'Pastoral dos Coroinhas e Cerimoniários', 'PASTORAL', 0),
    ('50000000-0000-0000-0000-000000000024', 'Pastoral dos Noivos', 'PASTORAL', 0),
    -- Serviços de Apoio e Espiritualidade
    ('50000000-0000-0000-0000-000000000025', 'Pastoral do Dízimo', 'PASTORAL', 0),
    ('50000000-0000-0000-0000-000000000026', 'Pastoral Vocacional', 'PASTORAL', 0),
    ('50000000-0000-0000-0000-000000000027', 'Pastoral da Esperança', 'PASTORAL', 0),
    ('50000000-0000-0000-0000-000000000028', 'Pastoral Social', 'PASTORAL', 0),
    -- Movimentos (Espiritualidade e Devoção)
    ('50000000-0000-0000-0000-000000000101', 'Renovação Carismática Católica (RCC)', 'MOVIMENTO', 0),
    ('50000000-0000-0000-0000-000000000102', 'Apostolado da Oração', 'MOVIMENTO', 0),
    ('50000000-0000-0000-0000-000000000103', 'Movimento de Schoenstatt', 'MOVIMENTO', 0),
    ('50000000-0000-0000-0000-000000000104', 'Legião de Maria', 'MOVIMENTO', 0),
    -- Família e Casais
    ('50000000-0000-0000-0000-000000000105', 'Encontro de Casais com Cristo (ECC)', 'MOVIMENTO', 0),
    ('50000000-0000-0000-0000-000000000106', 'Movimento Familiar Cristão (MFC)', 'MOVIMENTO', 0),
    -- Formação e Evangelização
    ('50000000-0000-0000-0000-000000000107', 'Movimento de Cursilhos de Cristandade (MCC)', 'MOVIMENTO', 0),
    ('50000000-0000-0000-0000-000000000108', 'Movimento dos Focolares (Obra de Maria)', 'MOVIMENTO', 0),
    -- Oração e Engajamento de Homens e Mulheres
    ('50000000-0000-0000-0000-000000000109', 'Terço dos Homens', 'MOVIMENTO', 0),
    ('50000000-0000-0000-0000-000000000110', 'Oficinas de Oração e Vida', 'MOVIMENTO', 0)
ON CONFLICT (id) DO NOTHING;


-- ============================================================================
-- 1. ESTRUTURA CANÔNICA, CLERO & UNIDADES TERRITORIAIS
-- ============================================================================

-- Circunscrições Eclesiásticas (Dioceses / Cúrias)
INSERT INTO calendario.dioceses (
    id, nome, bispo_diocesano, cnpj_mitra, cidade, uf, contato, ativo, version,
    criado_em_utc, atualizado_em_utc, criado_por, atualizado_por
) VALUES (
    '1d000000-0000-0000-0000-000000000001',
    'Diocese de Campo Limpo',
    'Dom Valdir José de Castro, SSP',
    '61.378.766/0001-45',
    'São Paulo',
    'SP',
    'curia@diocesedecampolimpo.org.br',
    TRUE,
    0,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed'
) ON CONFLICT (id) DO UPDATE SET
    nome = EXCLUDED.nome,
    bispo_diocesano = EXCLUDED.bispo_diocesano,
    cnpj_mitra = EXCLUDED.cnpj_mitra,
    cidade = EXCLUDED.cidade,
    uf = EXCLUDED.uf,
    contato = EXCLUDED.contato,
    ativo = EXCLUDED.ativo;

-- Clérigos (Ministros Ordenados / Membros do Clero)
INSERT INTO calendario.clerigos (
    id, nome_civil, nome_eclesiastico, data_nascimento, pais_origem, cpf, rg, documento_estrangeiro,
    email, celular, telefone_fixo, whatsapp, endereco, grau_ordem, diocese_incardinacao,
    data_ordenacao_diaconal, data_ordenacao_presbiteral, data_ordenacao_episcopal,
    ativo, criado_em_utc, atualizado_em_utc, criado_por, atualizado_por, version
) VALUES
    ('1c000000-0000-0000-0000-000000000001', 'Pedro da Silva', 'Pe. Pedro da Silva', '1980-01-01', 'BRA', '00000000001', '111111111', NULL, 'padre.pedro@example.com', '11999990001', NULL, TRUE, 'Rua da Matriz, 10, Centro, São Paulo - SP', 'PADRE_DIOCESANO', 'Diocese de Campo Limpo', '2009-12-08', '2010-12-08', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 0),
    ('1c000000-0000-0000-0000-000000000006', 'João Batista', 'Pe. João Batista', '1985-06-24', 'BRA', '00000000006', '666666666', NULL, 'padre.joao@example.com', '11999990007', NULL, TRUE, 'Rua da Matriz, 10, Centro, São Paulo - SP', 'PADRE_DIOCESANO', 'Diocese de Campo Limpo', '2014-12-08', '2015-12-08', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 0),
    ('1c000000-0000-0000-0000-000000000007', 'Francisco de Assis Costa', 'Diác. Francisco de Assis', '1976-10-04', 'BRA', '00000000026', '262626262', NULL, 'diacono.francisco@example.com', '11999990026', '1133332211', TRUE, 'Av. São José, 180, São José Allamano, São Paulo - SP', 'DIACONO_PERMANENTE', 'Diocese de Campo Limpo', '2018-05-13', NULL, NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 0),
    ('1c000000-0000-0000-0000-000000000008', 'Antônio Bento de Souza', 'Pe. Emérito Antônio Bento', '1945-02-15', 'BRA', '00000000027', '272727272', NULL, 'padre.antonio.emerito@example.com', '11999990027', NULL, FALSE, 'Rua da Paz, 50, Centro, São Paulo - SP', 'PADRE_DIOCESANO', 'Diocese de Campo Limpo', '1970-12-08', '1971-12-08', NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 0)
ON CONFLICT (id) DO UPDATE SET
    grau_ordem = EXCLUDED.grau_ordem,
    ativo = EXCLUDED.ativo;

-- Governo Eclesiástico Paroquial
INSERT INTO calendario.governo_eclesiastico (
    id, diocese_id, paroco_id, data_posse_paroco, decreto_erecao_paroquia,
    data_instalacao_paroquia, livro_tombo_numero, version,
    criado_em_utc, atualizado_em_utc, criado_por, atualizado_por
) VALUES (
    '2d000000-0000-0000-0000-000000000001',
    '1d000000-0000-0000-0000-000000000001',
    '1c000000-0000-0000-0000-000000000001',
    '2018-02-10',
    'Decreto Episcopal nº 12/1985',
    '1985-05-13',
    'Livro 01, fls. 12-14',
    0,
    CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed'
) ON CONFLICT (id) DO UPDATE SET
    diocese_id = EXCLUDED.diocese_id,
    paroco_id = EXCLUDED.paroco_id,
    data_posse_paroco = EXCLUDED.data_posse_paroco,
    decreto_erecao_paroquia = EXCLUDED.decreto_erecao_paroquia,
    data_instalacao_paroquia = EXCLUDED.data_instalacao_paroquia,
    livro_tombo_numero = EXCLUDED.livro_tombo_numero;

-- Hierarquia territorial (UnidadeTerritorial): Matriz -> Comunidades.
INSERT INTO calendario.unidade_territorial (
    id, nome, tipo, contato, ativo, parent_id,
    padroeiro, data_festa_padroeira, data_dedicacao_altar, possui_sacrario, possui_pia_batismal, clerigo_responsavel_id,
    cnpj, inscricao_municipal, inscricao_imobiliaria_iptu, processo_imunidade_iptu,
    cartorio_rgi, matricula_imovel, tipo_aquisicao, area_terreno_m2, area_construida_m2,
    avcb_numero, avcb_validade, lotacao_maxima_bombeiros, codigo_consumidor_energia, codigo_consumidor_agua,
    telefone, email, endereco_cep, endereco_logradouro, endereco_numero, endereco_complemento, endereco_bairro, endereco_cidade, endereco_uf,
    version, criado_em_utc, atualizado_em_utc, criado_por, atualizado_por
) VALUES
    (
        '00000000-0000-0000-0000-000000000100', 'Igreja Matriz Nossa Senhora de Fátima', 'MATRIZ', 'matriz@nsfatima.com', TRUE, NULL,
        'Nossa Senhora de Fátima', '05-13', '1986-10-12', TRUE, TRUE, '1c000000-0000-0000-0000-000000000001',
        '61.378.766/0002-26', '1.234.567-8', '098.765.4321-0', 'Proc. 2015-0.123.456-7 (Deferido Art. 150 CF)',
        '11º Oficial de Registro de Imóveis da Comarca da Capital', 'Matrícula nº 45.892', 'DOACAO', 1500.00, 1100.00,
        'AVCB-2024-SP-789012', '2027-12-31', 650, '003456789-01 (Enel)', '09876543/21 (Sabesp)',
        '11988887777', 'matriz@nsfatima.org.br', '08000-000', 'Praça da Matriz', '100', NULL, 'Centro', 'São Paulo', 'SP',
        0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed'
    ),
    (
        '00000000-0000-0000-0000-000000000200', 'Comunidade São José Allamano', 'COMUNIDADE', 'saojose@nsfatima.com', TRUE, '00000000-0000-0000-0000-000000000100',
        'São José Allamano', '02-16', NULL, TRUE, FALSE, '1c000000-0000-0000-0000-000000000006',
        '61.378.766/0002-26', '1.234.567-8', '098.765.4322-9', 'Proc. Mun. 2017-0.987.654-1 (Deferido)',
        '11º Oficial de Registro de Imóveis da Comarca da Capital', 'Matrícula nº 62.104', 'COMPRA_E_VENDA', 800.00, 450.00,
        'AVCB-2023-SP-345678', '2026-11-30', 250, '003456789-02', '09876543/22',
        '11988887702', 'saojose@nsfatima.org.br', '08000-003', 'Av. São José', '180', NULL, 'São José Allamano', 'São Paulo', 'SP',
        0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed'
    ),
    (
        '00000000-0000-0000-0000-000000000300', 'Comunidade Santo Agostinho', 'COMUNIDADE', 'santoagostinho@nsfatima.com', TRUE, '00000000-0000-0000-0000-000000000100',
        'Santo Agostinho', '08-28', NULL, TRUE, FALSE, '1c000000-0000-0000-0000-000000000007',
        '61.378.766/0002-26', '1.234.567-8', '098.765.4323-7', 'Proc. Mun. 2019-0.555.444-3 (Deferido)',
        '11º Oficial de Registro de Imóveis da Comarca da Capital', 'Matrícula nº 78.331', 'DOACAO', 600.00, 380.00,
        'AVCB-2022-SP-112233', '2025-05-15', 180, '003456789-03', '09876543/23',
        '11988887703', 'santoagostinho@nsfatima.org.br', '08000-002', 'Rua Santo Agostinho', '80', NULL, 'Santo Agostinho', 'São Paulo', 'SP',
        0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed'
    ),
    (
        '00000000-0000-0000-0000-0000000000dd', 'Comunidade Santana', 'COMUNIDADE', 'santana@nsfatima.com', TRUE, '00000000-0000-0000-0000-000000000100',
        'Santana', '07-26', NULL, TRUE, FALSE, '1c000000-0000-0000-0000-000000000001',
        NULL, NULL, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL,
        NULL, NULL, NULL, NULL, NULL,
        '11988887704', 'santana@nsfatima.org.br', '08000-004', 'Rua Santana', '20', NULL, 'Santana', 'São Paulo', 'SP',
        0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed'
    )
ON CONFLICT (id) DO UPDATE SET
    nome = EXCLUDED.nome,
    tipo = EXCLUDED.tipo,
    contato = EXCLUDED.contato,
    ativo = EXCLUDED.ativo,
    parent_id = EXCLUDED.parent_id,
    padroeiro = EXCLUDED.padroeiro,
    data_festa_padroeira = EXCLUDED.data_festa_padroeira,
    data_dedicacao_altar = EXCLUDED.data_dedicacao_altar,
    possui_sacrario = EXCLUDED.possui_sacrario,
    possui_pia_batismal = EXCLUDED.possui_pia_batismal,
    clerigo_responsavel_id = EXCLUDED.clerigo_responsavel_id,
    cnpj = EXCLUDED.cnpj,
    inscricao_municipal = EXCLUDED.inscricao_municipal,
    inscricao_imobiliaria_iptu = EXCLUDED.inscricao_imobiliaria_iptu,
    processo_imunidade_iptu = EXCLUDED.processo_imunidade_iptu,
    cartorio_rgi = EXCLUDED.cartorio_rgi,
    matricula_imovel = EXCLUDED.matricula_imovel,
    tipo_aquisicao = EXCLUDED.tipo_aquisicao,
    area_terreno_m2 = EXCLUDED.area_terreno_m2,
    area_construida_m2 = EXCLUDED.area_construida_m2,
    avcb_numero = EXCLUDED.avcb_numero,
    avcb_validade = EXCLUDED.avcb_validade,
    lotacao_maxima_bombeiros = EXCLUDED.lotacao_maxima_bombeiros,
    codigo_consumidor_energia = EXCLUDED.codigo_consumidor_energia,
    codigo_consumidor_agua = EXCLUDED.codigo_consumidor_agua,
    telefone = EXCLUDED.telefone,
    email = EXCLUDED.email,
    endereco_cep = EXCLUDED.endereco_cep,
    endereco_logradouro = EXCLUDED.endereco_logradouro,
    endereco_numero = EXCLUDED.endereco_numero,
    endereco_complemento = EXCLUDED.endereco_complemento,
    endereco_bairro = EXCLUDED.endereco_bairro,
    endereco_cidade = EXCLUDED.endereco_cidade,
    endereco_uf = EXCLUDED.endereco_uf;

-- Grupos Paroquiais (Conselhos/Pastorais/Movimentos). Conselhos têm organização-mãe diretamente na
-- UnidadeTerritorial (Matriz/Comunidade). Pastorais têm organização-mãe um Conselho. Os antigos 4 tipos de
-- conselho (CONSELHO_PASTORAL/CONSELHO_ECONOMICO/CPP/CPC) colapsaram em um único tipo CONSELHO.
INSERT INTO calendario.organizacao (id, nome, tipo, contato, coordenador, vice_coordenador, ativo, parent_id, pastoral_oficial_id, version)
VALUES
    -- Conselhos Paroquiais (organização-mãe: Igreja Matriz)
    ('00000000-0000-0000-0000-0000000000cc', 'Conselho Pastoral Paroquial', 'CONSELHO', 'cpp@nsfatima.com', 'Maria do Conselho', 'José do Conselho', TRUE, '00000000-0000-0000-0000-000000000100', NULL, 0),
    ('00000000-0000-0000-0000-000000000099', 'Conselho Econômico Paroquial', 'CONSELHO', 'economico@nsfatima.com', 'Pedro Finanças', 'Ana Administração', TRUE, '00000000-0000-0000-0000-000000000100', NULL, 0),
    -- Conselhos Comunitários (organização-mãe: cada Comunidade)
    ('00000000-0000-0000-0000-0000000002c0', 'Conselho Pastoral Comunitário - São José Allamano', 'CONSELHO', 'cpc.saojose@nsfatima.com', 'Gabriel Coordenador São José', 'Rafael Vice São José', TRUE, '00000000-0000-0000-0000-000000000200', NULL, 0),
    ('00000000-0000-0000-0000-0000000002e0', 'Conselho Econômico Comunitário - São José Allamano', 'CONSELHO', 'cec.saojose@nsfatima.com', 'Marcos Tesoureiro São José', NULL, TRUE, '00000000-0000-0000-0000-000000000200', NULL, 0),
    ('00000000-0000-0000-0000-0000000003c0', 'Conselho Pastoral Comunitário - Santo Agostinho', 'CONSELHO', 'cpc.santoagostinho@nsfatima.com', 'Lúcia Fernandes (Presidente CPC Santo Agostinho)', NULL, TRUE, '00000000-0000-0000-0000-000000000300', NULL, 0),
    ('00000000-0000-0000-0000-0000000003e0', 'Conselho Econômico Comunitário - Santo Agostinho', 'CONSELHO', 'cec.santoagostinho@nsfatima.com', 'Mateus Tesoureiro Santo Agostinho', NULL, TRUE, '00000000-0000-0000-0000-000000000300', NULL, 0),
    -- Pastorais Paroquiais (organização-mãe: CPP)
    ('00000000-0000-0000-0000-0000000000aa', 'Pastoral da Comunicação', 'PASTORAL', 'pascom@nsfatima.com', 'Tiago Pascom', NULL, TRUE, '00000000-0000-0000-0000-0000000000cc', '50000000-0000-0000-0000-000000000014', 0),
    ('00000000-0000-0000-0000-0000000000bb', 'Pastoral da Juventude', 'PASTORAL', 'juventude@nsfatima.com', 'Pedro Junior', 'Lucas dos Santos', TRUE, '00000000-0000-0000-0000-0000000000cc', '50000000-0000-0000-0000-000000000012', 0),
    ('00000000-0000-0000-0000-0000000000ee', 'Pastoral da Catequese', 'PASTORAL', 'catequese@nsfatima.com', 'Maria Oliveira', 'Lúcia de Oliveira', TRUE, '00000000-0000-0000-0000-0000000000cc', '50000000-0000-0000-0000-000000000021', 0),
    ('00000000-0000-0000-0000-0000000000ff', 'Pastoral do Batismo', 'PASTORAL', 'batismo@nsfatima.com', 'Isabel Batismo', NULL, TRUE, '00000000-0000-0000-0000-0000000000cc', '50000000-0000-0000-0000-000000000020', 0),
    -- Pastorais Comunitárias (filhas dos CPCs)
    ('00000000-0000-0000-0000-0000000002a1', 'Pastoral da Acolhida - São José Allamano', 'PASTORAL', 'acolhida.saojose@nsfatima.com', 'Marta Acolhida São José', NULL, TRUE, '00000000-0000-0000-0000-0000000002c0', '50000000-0000-0000-0000-000000000022', 0),
    ('00000000-0000-0000-0000-0000000002d1', 'Pastoral do Dízimo - São José Allamano', 'PASTORAL', 'dizimo.saojose@nsfatima.com', 'Paulo Dízimo São José', NULL, TRUE, '00000000-0000-0000-0000-0000000002c0', '50000000-0000-0000-0000-000000000025', 0),
    ('00000000-0000-0000-0000-0000000003a1', 'Pastoral da Liturgia - Santo Agostinho', 'PASTORAL', 'liturgia.santoagostinho@nsfatima.com', 'João Santos', NULL, TRUE, '00000000-0000-0000-0000-0000000003c0', '50000000-0000-0000-0000-000000000018', 0),
    ('00000000-0000-0000-0000-0000000003b1', 'Pastoral Social - Santo Agostinho', 'PASTORAL', 'social.santoagostinho@nsfatima.com', 'Teresa Social Santo Agostinho', NULL, TRUE, '00000000-0000-0000-0000-0000000003c0', '50000000-0000-0000-0000-000000000028', 0)
ON CONFLICT (id) DO NOTHING;

-- Classificação eclesial das organizações semeadas (subtipo de conselho e múnus das pastorais).
UPDATE calendario.organizacao SET subtipo_conselho = CASE
        WHEN nome ILIKE '%econ%' THEN 'CAEP'
        WHEN nome ILIKE '%comunit%' THEN 'CPC'
        ELSE 'CPP' END
WHERE tipo = 'CONSELHO';
UPDATE calendario.organizacao SET setor_pastoral = CASE
        WHEN nome ILIKE '%Liturgia%' OR nome ILIKE '%Batismo%' OR nome ILIKE '%Acolhida%' THEN 'SANTIFICACAO_LITURGIA'
        WHEN nome ILIKE '%Dízimo%' OR nome ILIKE '%Social%' THEN 'SOCIAL_CARIDADE'
        ELSE 'FORMACAO_ENSINAR' END
WHERE tipo = 'PASTORAL';
-- Dados de exemplo: movimentos do catálogo com estatuto reconhecido (necessário para instanciar movimentos).
UPDATE calendario.pastorais_movimentos_oficiais
SET ambito = 'DIOCESANO', estatuto_autoridade = 'Bispo diocesano (dado de exemplo)',
    estatuto_data = DATE '2000-01-01', estatuto_referencia = 'Seed de desenvolvimento'
WHERE tipo = 'MOVIMENTO' AND estatuto_autoridade IS NULL;

-- Locations (Locais) — sempre vinculadas a uma UnidadeTerritorial (Matriz/Comunidade)
INSERT INTO calendario.locais (
    id, nome, tipo, capacidade, capacidade_maxima, cep, logradouro, numero, complemento, bairro, cidade, uf, descricao, ativo, organizacao_id, version
) VALUES
    ('00000000-0000-0000-0000-000000000001', 'Igreja Principal', 'IGREJA', 500, 500, '08000-000', 'Praça da Matriz', '100', NULL, 'Centro', 'São Paulo', 'SP', 'Nave principal climatizada com sistema de som e transmissão.', TRUE, '00000000-0000-0000-0000-000000000100', 0),
    ('00000000-0000-0000-0000-000000000002', 'Salão Paroquial', 'SALAO', 300, 300, '08000-000', 'Rua da Matriz', '120', NULL, 'Centro', 'São Paulo', 'SP', 'Salão de festas paroquiais com palco e mesas.', TRUE, '00000000-0000-0000-0000-000000000100', 0),
    ('00000000-0000-0000-0000-000000000003', 'Sala de Catequese 1', 'SALA', 30, 30, '08000-000', 'Rua da Matriz', '120', 'Sala 1', 'Centro', 'São Paulo', 'SP', 'Sala com lousa, carteiras e projetor fixo.', TRUE, '00000000-0000-0000-0000-000000000100', 0),
    ('00000000-0000-0000-0000-000000000004', 'Cozinha Paroquial', 'SALA', 15, 15, '08000-000', 'Rua da Matriz', '120', 'Anexo Salão', 'Centro', 'São Paulo', 'SP', 'Cozinha industrial equipada para quermesses e eventos.', TRUE, '00000000-0000-0000-0000-000000000100', 0),
    ('00000000-0000-0000-0000-000000000005', 'Salão Comunidade São José Allamano', 'SALAO', 150, 150, '08000-003', 'Av. São José', '50', NULL, 'São José Allamano', 'São Paulo', 'SP', 'Espaço comunitário com ventiladores e som portátil.', TRUE, '00000000-0000-0000-0000-000000000200', 0),
    ('00000000-0000-0000-0000-000000000006', 'Salão Comunidade Santo Agostinho', 'SALAO', 150, 150, '08000-002', 'Rua Santo Agostinho', '25', NULL, 'Santo Agostinho', 'São Paulo', 'SP', 'Salão para encontros e celebrações da comunidade.', TRUE, '00000000-0000-0000-0000-000000000300', 0),
    ('00000000-0000-0000-0000-000000000007', 'Capela Menino Jesus (Desativada)', 'CAPELA', 40, 40, '08000-004', 'Rua dos Anjos', '77', NULL, 'Vila Curuçá', 'São Paulo', 'SP', 'Capela histórica temporariamente desativada para restauro.', FALSE, '00000000-0000-0000-0000-000000000100', 0)
ON CONFLICT (id) DO UPDATE SET
    nome = EXCLUDED.nome,
    tipo = EXCLUDED.tipo,
    capacidade = EXCLUDED.capacidade,
    capacidade_maxima = EXCLUDED.capacidade_maxima,
    cep = EXCLUDED.cep,
    logradouro = EXCLUDED.logradouro,
    numero = EXCLUDED.numero,
    complemento = EXCLUDED.complemento,
    bairro = EXCLUDED.bairro,
    cidade = EXCLUDED.cidade,
    uf = EXCLUDED.uf,
    descricao = EXCLUDED.descricao,
    ativo = EXCLUDED.ativo;

-- Users (Usuarios)

-- Encargos Clericais (Posições institucionais do Clero)
INSERT INTO calendario.encargos_clericais (
    id, clerigo_id, unidade_territorial_id, funcao, atual, criado_em_utc, encerrado_em_utc
) VALUES
    ('2c000000-0000-0000-0000-000000000001', '1c000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000100', 'PAROCO', TRUE, CURRENT_TIMESTAMP, NULL),
    ('2c000000-0000-0000-0000-000000000006', '1c000000-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000100', 'VIGARIO_PAROQUIAL', TRUE, CURRENT_TIMESTAMP, NULL),
    ('2c000000-0000-0000-0000-000000000007', '1c000000-0000-0000-0000-000000000007', '00000000-0000-0000-0000-000000000200', 'DIACONO_AUXILIAR', TRUE, CURRENT_TIMESTAMP, NULL),
    ('2c000000-0000-0000-0000-000000000008', '1c000000-0000-0000-0000-000000000008', '00000000-0000-0000-0000-000000000100', 'PAROCO', FALSE, '2005-01-01T00:00:00Z', '2020-12-31T23:59:59Z')
ON CONFLICT (id) DO NOTHING;

-- Fieis (pessoas físicas correspondentes aos Usuarios leigos e membros de comunidades)
INSERT INTO calendario.fieis (
    id, nome, cpf, cpf_hash, data_nascimento, email, telefone,
    endereco_logradouro, endereco_numero, endereco_bairro, endereco_cidade, endereco_estado, endereco_cep,
    rg, sexo, profissao, celular_whatsapp, preferencia_canal_comunicacao, estado_civil,
    vocacao, ativo, criado_em_utc, atualizado_em_utc, criado_por, atualizado_por,
    habilidades_voluntariado, termo_consentimento_aceito, uso_imagem_autorizado, permitir_divulgacao_aniversario
)
VALUES
    ('1f000000-0000-0000-0000-000000000000', 'Administrador do Sistema', '00000000000', '0000000000000000000000000000000000000000000000000000000000000000', '1980-01-01', 'admin@example.com', '11999990000', 'Rua da Matriz', '1', 'Centro', 'São Paulo', 'SP', '08000-000', '000000000', 'NAO_INFORMADO', 'Administrador', '11999990000', 'EMAIL', 'SOLTEIRO', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 'Administração de Sistemas', TRUE, TRUE, FALSE),
    ('1f000000-0000-0000-0000-000000000002', 'Maria Oliveira', '00000000002', '83764048c38ff46c8bea9b07d9ff2d8f8ee759334e3c4040da075c6de4964ade', '1980-01-01', 'maria.catequese@example.com', '11999990002', 'Rua das Flores', '100', 'Vila Curuçá', 'São Paulo', 'SP', '08000-001', '222222222', 'FEMININO', 'Professora', '11999990002', 'WHATSAPP', 'CASADO_IGREJA', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 'Catequese, Canto', TRUE, TRUE, TRUE),
    ('1f000000-0000-0000-0000-000000000003', 'João Santos', '00000000003', '5fe5f4974dabb8ad6e16a663a5da789608363f7ef8d036e90a08a82e76725a7e', '1980-01-01', 'joao.liturgia@example.com', '11999990003', 'Rua Santo Agostinho', '25', 'Santo Agostinho', 'São Paulo', 'SP', '08000-002', '333333333', 'MASCULINO', 'Engenheiro', '11999990003', 'WHATSAPP', 'SOLTEIRO', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 'Som e Mídia, Liturgia', TRUE, FALSE, TRUE),
    ('1f000000-0000-0000-0000-000000000004', 'Pedro Junior', '00000000004', '8edd917ae34b0c41d9c5d1219601a98c5c24a6949334f89b8075cbcae32d6d14', '1980-01-01', 'pedro.liturgia@example.com', '11999990004', 'Av. São José', '50', 'São José Allamano', 'São Paulo', 'SP', '08000-003', '444444444', 'MASCULINO', 'Estudante', '11999990004', 'WHATSAPP', 'SOLTEIRO', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 'Juventude, Violão', TRUE, TRUE, TRUE),
    ('1f000000-0000-0000-0000-000000000005', 'Maria do Conselho', '00000000005', '575bb6dd332492ed89809e98c1eb300343e97898c12b09414560471242bed35a', '1980-01-01', 'maria.conselho@example.com', '11999990005', 'Rua Central', '12', 'Centro', 'São Paulo', 'SP', '08000-000', '555555555', 'FEMININO', 'Administradora', '11999990005', 'WHATSAPP', 'CASADO_IGREJA', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 'Gestão Pastoral', TRUE, TRUE, TRUE),
    ('1f000000-0000-0000-0000-000000000007', 'José do Conselho', '00000000007', '6b315f73399206b7b957093bc830705cf059be0d2c0bfdcf1afe52b704c1e7a9', '1980-01-01', 'jose.conselho@example.com', '11999990008', 'Rua Central', '12', 'Centro', 'São Paulo', 'SP', '08000-000', '777777777', 'MASCULINO', 'Contador', '11999990008', 'WHATSAPP', 'CASADO_IGREJA', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 'Conselho Pastoral', TRUE, TRUE, TRUE),
    ('1f000000-0000-0000-0000-000000000008', 'Ana do Conselho', '00000000008', '46026a0184154dba96ac2cf6ef69aa6a57dcc07d768d8354b3ddd16008d355ea', '1980-01-01', 'ana.conselho@example.com', '11999990009', 'Rua Nova', '45', 'Vila Curuçá', 'São Paulo', 'SP', '08000-001', '888888888', 'FEMININO', 'Advogada', '11999990009', 'WHATSAPP', 'CASADO_IGREJA', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 'Apoio Jurídico', TRUE, TRUE, TRUE),
    ('1f000000-0000-0000-0000-000000000009', 'Lúcia de Oliveira', '00000000009', 'cb10f8c68fbb2772eb8c11b4e4f0ee27937db6f3347d9696f709df9e94a5c487', '1980-01-01', 'lucia.catequese@example.com', '11999990010', 'Rua das Flores', '100', 'Vila Curuçá', 'São Paulo', 'SP', '08000-001', '999999999', 'FEMININO', 'Pedagoga', '11999990010', 'WHATSAPP', 'SOLTEIRO', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 'Catequese Infantil', TRUE, TRUE, TRUE),
    ('1f000000-0000-0000-0000-000000000010', 'Carlos de Oliveira', '00000000010', 'bc40a6e499021d2b5a4b0b60e932d6b53053384218f81acdcb11fd83939be605', '1980-01-01', 'carlos.catequese@example.com', '11999990011', 'Rua das Flores', '100', 'Vila Curuçá', 'São Paulo', 'SP', '08000-001', '101010101', 'MASCULINO', 'Arquiteto', '11999990011', 'WHATSAPP', 'SOLTEIRO', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 'Apoio Eventos', TRUE, TRUE, TRUE),
    ('1f000000-0000-0000-0000-000000000011', 'Lucas dos Santos', '00000000011', 'ec95ca9a68d5e645068bff18a444f5035a9c474a6b44d186a20ed7e05a29689f', '1980-01-01', 'lucas.juventude@example.com', '11999990012', 'Av. São José', '55', 'São José Allamano', 'São Paulo', 'SP', '08000-003', '111011101', 'MASCULINO', 'Designer', '11999990012', 'WHATSAPP', 'SOLTEIRO', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 'Redes Sociais, Mídia', TRUE, TRUE, TRUE),
    ('1f000000-0000-0000-0000-000000000012', 'Pedro Finanças', '00000000012', 'a7bbab4eab5ae60a310a77448f83ecae88ce92dfd60157871d94db9a07fe185f', '1980-01-01', 'pedro.financas@example.com', '11999990091', 'Rua Principal', '200', 'Centro', 'São Paulo', 'SP', '08000-000', '121212121', 'MASCULINO', 'Economista', '11999990091', 'WHATSAPP', 'CASADO_IGREJA', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 'Finanças Paroquiais', TRUE, TRUE, TRUE),
    ('1f000000-0000-0000-0000-000000000013', 'Ana Administração', '00000000013', '82af09a26b663ccb5c486e3f39ae6c96edef701c29417c3003451839ce2a889b', '1980-01-01', 'ana.administracao@example.com', '11999990092', 'Rua Principal', '200', 'Centro', 'São Paulo', 'SP', '08000-000', '131313131', 'FEMININO', 'Administradora', '11999990092', 'WHATSAPP', 'CASADO_IGREJA', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 'Administração', TRUE, TRUE, TRUE),
    ('1f000000-0000-0000-0000-000000000014', 'Lúcia Fernandes (Conselheira CPC Santo Agostinho)', '00000000014', 'a2b86a5f7a87f04cc769393e90bbeef42e403ba38d565f2ada4cfd886096bb4b', '1980-01-01', 'lucia.conselhocpc@example.com', '11999990013', 'Rua Santo Agostinho', '80', 'Santo Agostinho', 'São Paulo', 'SP', '08000-002', '141414141', 'FEMININO', 'Assistente Social', '11999990013', 'WHATSAPP', 'SOLTEIRO', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 'Coordenação Comunitária', TRUE, TRUE, TRUE),

    -- Fiéis adicionais para cobertura de TODAS as Comunidades e Pastorais:
    ('1f000000-0000-0000-0000-000000000015', 'Gabriel Coordenador São José', '00000000015', 'f15hash000000000000000000000000000000000000000000000000000000001', '1985-03-15', 'gabriel.saojose@example.com', '11988880015', 'Rua das Palmeiras', '30', 'São José Allamano', 'São Paulo', 'SP', '08000-003', '151515151', 'MASCULINO', 'Comerciante', '11988880015', 'WHATSAPP', 'CASADO_IGREJA', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 'Coordenação Pastoral', TRUE, TRUE, TRUE),
    ('1f000000-0000-0000-0000-000000000016', 'Rafael Secretário São José', '00000000016', 'f16hash000000000000000000000000000000000000000000000000000000002', '1988-07-20', 'rafael.saojose@example.com', '11988880016', 'Rua das Palmeiras', '35', 'São José Allamano', 'São Paulo', 'SP', '08000-003', '161616161', 'MASCULINO', 'Professor', '11988880016', 'WHATSAPP', 'SOLTEIRO', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 'Apoio Comunitário', TRUE, TRUE, TRUE),
    ('1f000000-0000-0000-0000-000000000017', 'Marcos Tesoureiro São José', '00000000017', 'f17hash000000000000000000000000000000000000000000000000000000003', '1979-11-05', 'marcos.tesoureiro@example.com', '11988880017', 'Av. São José', '102', 'São José Allamano', 'São Paulo', 'SP', '08000-003', '171717171', 'MASCULINO', 'Bancário', '11988880017', 'WHATSAPP', 'CASADO_IGREJA', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 'Finanças e Caixa', TRUE, TRUE, TRUE),
    ('1f000000-0000-0000-0000-000000000018', 'Mateus Tesoureiro Santo Agostinho', '00000000018', 'f18hash000000000000000000000000000000000000000000000000000000004', '1982-04-12', 'mateus.santoagostinho@example.com', '11988880018', 'Rua Santo Agostinho', '150', 'Santo Agostinho', 'São Paulo', 'SP', '08000-002', '181818181', 'MASCULINO', 'Administrador', '11988880018', 'WHATSAPP', 'CASADO_IGREJA', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 'Gestão Financeira', TRUE, TRUE, TRUE),
    ('1f000000-0000-0000-0000-000000000019', 'Tiago Pascom', '00000000019', 'f19hash000000000000000000000000000000000000000000000000000000005', '1992-09-08', 'tiago.pascom@example.com', '11988880019', 'Rua Central', '88', 'Centro', 'São Paulo', 'SP', '08000-000', '191919191', 'MASCULINO', 'Jornalista', '11988880019', 'WHATSAPP', 'SOLTEIRO', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 'Fotografia, Transmissões', TRUE, TRUE, TRUE),
    ('1f000000-0000-0000-0000-000000000020', 'Isabel Batismo', '00000000020', 'f20hash000000000000000000000000000000000000000000000000000000006', '1975-01-25', 'isabel.batismo@example.com', '11988880020', 'Rua das Rosas', '14', 'Vila Curuçá', 'São Paulo', 'SP', '08000-001', '202020202', 'FEMININO', 'Enfermeira', '11988880020', 'WHATSAPP', 'CASADO_IGREJA', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 'Preparação do Batismo', TRUE, TRUE, TRUE),
    ('1f000000-0000-0000-0000-000000000021', 'Marta Acolhida São José', '00000000021', 'f21hash000000000000000000000000000000000000000000000000000000007', '1984-06-18', 'marta.acolhida@example.com', '11988880021', 'Rua das Palmeiras', '90', 'São José Allamano', 'São Paulo', 'SP', '08000-003', '212121212', 'FEMININO', 'Recepcionista', '11988880021', 'WHATSAPP', 'CASADO_IGREJA', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 'Acolhida nas Missas', TRUE, TRUE, TRUE),
    ('1f000000-0000-0000-0000-000000000022', 'Paulo Dízimo São José', '00000000022', 'f22hash000000000000000000000000000000000000000000000000000000008', '1978-12-02', 'paulo.dizimo@example.com', '11988880022', 'Av. São José', '210', 'São José Allamano', 'São Paulo', 'SP', '08000-003', '222222223', 'MASCULINO', 'Vendedor', '11988880022', 'WHATSAPP', 'CASADO_IGREJA', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 'Conscientização do Dízimo', TRUE, TRUE, TRUE),
    ('1f000000-0000-0000-0000-000000000023', 'Teresa Social Santo Agostinho', '00000000023', 'f23hash000000000000000000000000000000000000000000000000000000009', '1981-08-30', 'teresa.social@example.com', '11988880023', 'Rua Santo Agostinho', '215', 'Santo Agostinho', 'São Paulo', 'SP', '08000-002', '232323234', 'FEMININO', 'Assistente Social', '11988880023', 'WHATSAPP', 'SOLTEIRO', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 'Cestas Básicas e Vicentinos', TRUE, TRUE, TRUE),
    ('1f000000-0000-0000-0000-000000000024', 'Lucas Fiel São José', '00000000024', 'f24hash000000000000000000000000000000000000000000000000000000010', '1995-10-10', 'lucas.saojose@example.com', '11988880024', 'Av. São José', '300', 'São José Allamano', 'São Paulo', 'SP', '08000-003', '242424245', 'MASCULINO', 'Técnico em TI', '11988880024', 'WHATSAPP', 'SOLTEIRO', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 'Vicentinos, Acolhida', TRUE, TRUE, TRUE),
    ('1f000000-0000-0000-0000-000000000025', 'Clara Fiel Santo Agostinho', '00000000025', 'f25hash000000000000000000000000000000000000000000000000000000011', '1993-05-14', 'clara.santoagostinho@example.com', '11988880025', 'Rua Santo Agostinho', '310', 'Santo Agostinho', 'São Paulo', 'SP', '08000-002', '252525256', 'FEMININO', 'Bióloga', '11988880025', 'WHATSAPP', 'SOLTEIRO', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 'Decoração e Liturgia', TRUE, TRUE, TRUE),
    ('1f000000-0000-0000-0000-000000000026', 'Joaquim Falecido da Silva', '00000000028', 'f26hash000000000000000000000000000000000000000000000000000000012', '1935-08-10', 'joaquim.falecido@example.com', '11988880028', 'Rua da Paz', '12', 'Centro', 'São Paulo', 'SP', '08000-000', '282828282', 'MASCULINO', 'Aposentado', '11988880028', 'EMAIL', 'VIUVO', 'LEIGO', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 'Oração', TRUE, FALSE, FALSE),
    ('1f000000-0000-0000-0000-000000000027', 'Francisca Secretária', '00000000027', 'f27hash000000000000000000000000000000000000000000000000000000013', '1986-04-15', 'secretaria@nsfatima.com', '11988880027', 'Praça da Matriz', '10', 'Centro', 'São Paulo', 'SP', '08000-000', '272727272', 'FEMININO', 'Secretária Paroquial', '11988880027', 'WHATSAPP', 'SOLTEIRO', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed', 'Atendimento Paroquial, Documentação', TRUE, TRUE, TRUE)
ON CONFLICT (id) DO NOTHING;

-- Tabela de Dependentes / Filhos do Fiel
INSERT INTO calendario.fiel_dependentes (id, fiel_id, nome, data_nascimento, parentesco, batizado, primeira_comunhao, crismado, criado_em_utc)
VALUES
    ('95000000-0000-0000-0000-000000000001', '1f000000-0000-0000-0000-000000000002', 'Lucas Oliveira', '2015-05-12', 'FILHO', TRUE, TRUE, FALSE, CURRENT_TIMESTAMP),
    ('95000000-0000-0000-0000-000000000002', '1f000000-0000-0000-0000-000000000002', 'Sofia Oliveira', '2018-09-20', 'FILHO', TRUE, FALSE, FALSE, CURRENT_TIMESTAMP),
    ('95000000-0000-0000-0000-000000000003', '1f000000-0000-0000-0000-000000000007', 'Gabriel do Conselho', '2012-01-30', 'FILHO', TRUE, TRUE, TRUE, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO calendario.usuarios (id, username, password_hash, enabled, roles, igreja_id, version, fiel_id, clerigo_id)
VALUES 
    ('10000000-0000-0000-0000-000000000000', 'admin', '{noop}senha123', TRUE, 'ADMIN', '00000000-0000-0000-0000-000000000100', 0, '1f000000-0000-0000-0000-000000000000', NULL),
    ('10000000-0000-0000-0000-000000000001', 'padre.pedro', '{noop}senha123', TRUE, 'PAROCO', '00000000-0000-0000-0000-000000000100', 0, NULL, '1c000000-0000-0000-0000-000000000001'),
    ('10000000-0000-0000-0000-000000000002', 'maria.catequese', '{noop}senha123', TRUE, 'COORDENADOR', '00000000-0000-0000-0000-000000000100', 0, '1f000000-0000-0000-0000-000000000002', NULL),
    ('10000000-0000-0000-0000-000000000003', 'joao.liturgia', '{noop}senha123', TRUE, 'VOLUNTARIO', '00000000-0000-0000-0000-000000000300', 0, '1f000000-0000-0000-0000-000000000003', NULL),
    ('10000000-0000-0000-0000-000000000004', 'pedro.liturgia', '{noop}senha123', TRUE, 'COORDENADOR', '00000000-0000-0000-0000-000000000100', 0, '1f000000-0000-0000-0000-000000000004', NULL),
    ('10000000-0000-0000-0000-000000000005', 'maria.conselho', '{noop}senha123', TRUE, 'SECRETARIO_CONSELHO', '00000000-0000-0000-0000-000000000100', 0, '1f000000-0000-0000-0000-000000000005', NULL),
    ('10000000-0000-0000-0000-000000000007', 'padre.joao', '{noop}senha123', TRUE, 'VIGARIO', '00000000-0000-0000-0000-000000000100', 0, NULL, '1c000000-0000-0000-0000-000000000006'),
    ('10000000-0000-0000-0000-000000000008', 'jose.conselho', '{noop}senha123', TRUE, 'MEMBRO_CONSELHO_PASTORAL', '00000000-0000-0000-0000-000000000100', 0, '1f000000-0000-0000-0000-000000000007', NULL),
    ('10000000-0000-0000-0000-000000000009', 'ana.conselho', '{noop}senha123', TRUE, 'MEMBRO_CONSELHO_PASTORAL', '00000000-0000-0000-0000-000000000100', 0, '1f000000-0000-0000-0000-000000000008', NULL),
    ('10000000-0000-0000-0000-000000000010', 'lucia.catequese', '{noop}senha123', TRUE, 'COORDENADOR', '00000000-0000-0000-0000-000000000100', 0, '1f000000-0000-0000-0000-000000000009', NULL),
    ('10000000-0000-0000-0000-000000000011', 'carlos.catequese', '{noop}senha123', TRUE, 'VOLUNTARIO', '00000000-0000-0000-0000-000000000100', 0, '1f000000-0000-0000-0000-000000000010', NULL),
    ('10000000-0000-0000-0000-000000000012', 'lucas.juventude', '{noop}senha123', TRUE, 'COORDENADOR', '00000000-0000-0000-0000-000000000100', 0, '1f000000-0000-0000-0000-000000000011', NULL),
    ('10000000-0000-0000-0000-000000000091', 'pedro.financas', '{noop}senha123', TRUE, 'MEMBRO_CONSELHO_ECONOMICO', '00000000-0000-0000-0000-000000000100', 0, '1f000000-0000-0000-0000-000000000012', NULL),
    ('10000000-0000-0000-0000-000000000092', 'ana.administracao', '{noop}senha123', TRUE, 'MEMBRO_CONSELHO_ECONOMICO', '00000000-0000-0000-0000-000000000100', 0, '1f000000-0000-0000-0000-000000000013', NULL),
    ('10000000-0000-0000-0000-000000000013', 'lucia.conselhocpc', '{noop}senha123', TRUE, 'MEMBRO_CONSELHO_PASTORAL', '00000000-0000-0000-0000-000000000300', 0, '1f000000-0000-0000-0000-000000000014', NULL),
    ('10000000-0000-0000-0000-000000000026', 'diacono.francisco', '{noop}senha123', TRUE, 'DIACONO_PAROQUIAL', '00000000-0000-0000-0000-000000000200', 0, NULL, '1c000000-0000-0000-0000-000000000007'),
    ('10000000-0000-0000-0000-000000000027', 'secretaria.paroquial', '{noop}senha123', TRUE, 'SECRETARIA_PAROQUIAL', '00000000-0000-0000-0000-000000000100', 0, '1f000000-0000-0000-0000-000000000027', NULL)
ON CONFLICT (username) DO UPDATE SET
    password_hash = EXCLUDED.password_hash,
    enabled = EXCLUDED.enabled,
    roles = EXCLUDED.roles,
    igreja_id = EXCLUDED.igreja_id,
    fiel_id = EXCLUDED.fiel_id,
    clerigo_id = EXCLUDED.clerigo_id;

-- Vínculos Fiel-Organizacao (membro_grupo): chaveados por fiel_id, garantindo membros e coordenadores em TODAS as pastorais e conselhos.
INSERT INTO calendario.membro_grupo (id, fiel_id, organizacao_id, tipo_organizacao, papel, igreja_vinculo_id, ativo, criado_em_utc, atualizado_em_utc)
VALUES
    -- CPP e CAE (Matriz)
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-0000000000cc', 'CONSELHO', 'secretario-conselho', '00000000-0000-0000-0000-000000000100', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000007', '00000000-0000-0000-0000-0000000000cc', 'CONSELHO', 'membro-conselho-pastoral', '00000000-0000-0000-0000-000000000100', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000008', '00000000-0000-0000-0000-0000000000cc', 'CONSELHO', 'membro-conselho-pastoral', '00000000-0000-0000-0000-000000000100', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000012', '00000000-0000-0000-0000-000000000099', 'CONSELHO', 'membro-conselho-economico', '00000000-0000-0000-0000-000000000100', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000013', '00000000-0000-0000-0000-000000000099', 'CONSELHO', 'membro-conselho-economico', '00000000-0000-0000-0000-000000000100', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- CPC e CEC São José Allamano
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000015', '00000000-0000-0000-0000-0000000002c0', 'CONSELHO', 'secretario-conselho', '00000000-0000-0000-0000-000000000200', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000016', '00000000-0000-0000-0000-0000000002c0', 'CONSELHO', 'membro-conselho-pastoral', '00000000-0000-0000-0000-000000000200', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000017', '00000000-0000-0000-0000-0000000002e0', 'CONSELHO', 'membro-conselho-economico', '00000000-0000-0000-0000-000000000200', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- CPC e CEC Santo Agostinho
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000014', '00000000-0000-0000-0000-0000000003c0', 'CONSELHO', 'secretario-conselho', '00000000-0000-0000-0000-000000000300', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000018', '00000000-0000-0000-0000-0000000003e0', 'CONSELHO', 'membro-conselho-economico', '00000000-0000-0000-0000-000000000300', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000025', '00000000-0000-0000-0000-0000000003c0', 'CONSELHO', 'membro-conselho-pastoral', '00000000-0000-0000-0000-000000000300', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Pastorais Paroquiais (PASCOM, Juventude, Catequese, Batismo)
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000019', '00000000-0000-0000-0000-0000000000aa', 'PASTORAL', 'coordenador', '00000000-0000-0000-0000-000000000100', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-0000000000aa', 'PASTORAL', 'voluntario', '00000000-0000-0000-0000-000000000100', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-0000000000bb', 'PASTORAL', 'coordenador', '00000000-0000-0000-0000-000000000100', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-0000000000bb', 'PASTORAL', 'voluntario', '00000000-0000-0000-0000-000000000100', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-0000000000ee', 'PASTORAL', 'coordenador', '00000000-0000-0000-0000-000000000100', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000009', '00000000-0000-0000-0000-0000000000ee', 'PASTORAL', 'secretario-grupo', '00000000-0000-0000-0000-000000000100', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-0000000000ee', 'PASTORAL', 'voluntario', '00000000-0000-0000-0000-000000000100', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000020', '00000000-0000-0000-0000-0000000000ff', 'PASTORAL', 'coordenador', '00000000-0000-0000-0000-000000000100', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000008', '00000000-0000-0000-0000-0000000000ff', 'PASTORAL', 'voluntario', '00000000-0000-0000-0000-000000000100', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Pastorais Comunitárias (São José Allamano & Santo Agostinho)
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000021', '00000000-0000-0000-0000-0000000002a1', 'PASTORAL', 'coordenador', '00000000-0000-0000-0000-000000000200', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000024', '00000000-0000-0000-0000-0000000002a1', 'PASTORAL', 'voluntario', '00000000-0000-0000-0000-000000000200', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000022', '00000000-0000-0000-0000-0000000002d1', 'PASTORAL', 'coordenador', '00000000-0000-0000-0000-000000000200', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000015', '00000000-0000-0000-0000-0000000002d1', 'PASTORAL', 'voluntario', '00000000-0000-0000-0000-000000000200', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-0000000003a1', 'PASTORAL', 'coordenador', '00000000-0000-0000-0000-000000000300', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000025', '00000000-0000-0000-0000-0000000003a1', 'PASTORAL', 'voluntario', '00000000-0000-0000-0000-000000000300', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000023', '00000000-0000-0000-0000-0000000003b1', 'PASTORAL', 'coordenador', '00000000-0000-0000-0000-000000000300', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000014', '00000000-0000-0000-0000-0000000003b1', 'PASTORAL', 'voluntario', '00000000-0000-0000-0000-000000000300', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

    -- Escopo direto na Comunidade (organizacao_id = Santo Agostinho)
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000014', '00000000-0000-0000-0000-000000000300', 'CONSELHO', 'membro-conselho-pastoral', '00000000-0000-0000-0000-000000000300', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)

ON CONFLICT DO NOTHING;

-- Years (Anos Paroquiais)
INSERT INTO calendario.anos_paroquiais (ano, status, data_fechamento_utc)
VALUES 
    (2025, 'CONCLUIDO', '2025-01-01T00:00:00Z'),
    (2026, 'EM_PLANEJAMENTO', NULL),
    (2027, 'EM_PLANEJAMENTO', NULL)
ON CONFLICT (ano) DO UPDATE SET status = EXCLUDED.status, data_fechamento_utc = EXCLUDED.data_fechamento_utc;

-- Projects (Projetos)
INSERT INTO calendario.projetos_eventos (id, nome, descricao, status, organizacao_responsavel_id, inicio_utc, fim_utc, version)
VALUES 
    ('30000000-0000-0000-0000-000000000001', 'Catequese 2026', 'Ano letivo da catequese infantil', 'EM_ANDAMENTO', '00000000-0000-0000-0000-0000000000ee', '2026-02-01T00:00:00Z', '2026-12-15T23:59:59Z', 0),
    ('30000000-0000-0000-0000-000000000002', 'Festividades de Maio', 'Mês de Nossa Senhora', 'EM_ANDAMENTO', '00000000-0000-0000-0000-0000000000dd', '2026-05-01T00:00:00Z', '2026-05-31T23:59:59Z', 0),
    ('30000000-0000-0000-0000-000000000003', 'Reforma da Matriz', 'Reforma e pintura da Igreja Principal', 'EM_ANDAMENTO', '00000000-0000-0000-0000-0000000000dd', '2026-01-01T00:00:00Z', '2026-06-20T23:59:59Z', 0),
    ('30000000-0000-0000-0000-000000000004', 'Acampamento Juvenil 2026', 'Retiro de férias da juventude', 'ABERTO', '00000000-0000-0000-0000-0000000000bb', '2026-10-10T00:00:00Z', '2026-10-12T23:59:59Z', 0),
    ('30000000-0000-0000-0000-000000000005', 'Campanha da Fraternidade 2025', 'Projeto pastoral concluído', 'CONCLUIDO', '00000000-0000-0000-0000-0000000000cc', '2025-02-15T00:00:00Z', '2025-04-20T23:59:59Z', 0),
    ('30000000-0000-0000-0000-000000000006', 'Show de Prêmios Beneficente (Cancelado)', 'Evento cancelado por deliberação do conselho', 'CANCELADO', '00000000-0000-0000-0000-000000000099', '2026-03-01T00:00:00Z', '2026-03-02T23:59:59Z', 0);

-- Events (Eventos)
INSERT INTO calendario.eventos (id, titulo, descricao, organizacao_responsavel_id, local_id, categoria, status, inicio_utc, fim_utc, projeto_id, version, tipo_horario, formato)
VALUES 
    ('40000000-0000-0000-0000-000000000001', 'Missa de Abertura da Catequese', 'Missa com pais e alunos', '00000000-0000-0000-0000-0000000000ee', '00000000-0000-0000-0000-000000000001', 'LITURGICO', 'CONFIRMADO', '2026-09-07T18:00:00Z', '2026-09-07T19:30:00Z', '30000000-0000-0000-0000-000000000001', 0, 'PERSONALIZADO', 'PRESENCIAL'),
    ('40000000-0000-0000-0000-000000000003', 'Encontro de Formação - Catequistas', 'Formação litúrgica', '00000000-0000-0000-0000-0000000000ee', '00000000-0000-0000-0000-000000000002', 'FORMATIVO', 'CONFIRMADO', '2026-06-10T19:30:00Z', '2026-06-10T21:00:00Z', '30000000-0000-0000-0000-000000000001', 0, 'PERSONALIZADO', 'PRESENCIAL'),
    ('40000000-0000-0000-0000-000000000004', 'Missa de Corpus Christi', 'Celebração solene e procissão', '00000000-0000-0000-0000-0000000000dd', '00000000-0000-0000-0000-000000000001', 'LITURGICO', 'CONFIRMADO', '2026-06-04T08:00:00Z', '2026-06-04T10:00:00Z', NULL, 0, 'PERSONALIZADO', 'PRESENCIAL'),
    ('40000000-0000-0000-0000-000000000005', 'Inauguração da Pintura', 'Evento de entrega das obras', '00000000-0000-0000-0000-0000000000dd', '00000000-0000-0000-0000-000000000001', 'SOCIAL', 'CONFIRMADO', '2026-06-18T19:00:00Z', '2026-06-18T21:00:00Z', '30000000-0000-0000-0000-000000000003', 0, 'PERSONALIZADO', 'PRESENCIAL'),
    ('40000000-0000-0000-0000-000000000006', 'Missa da Padroeira', 'Missa festiva de Nossa Senhora de Fátima', '00000000-0000-0000-0000-0000000000dd', '00000000-0000-0000-0000-000000000001', 'LITURGICO', 'CONFIRMADO', '2026-06-13T18:00:00Z', '2026-06-13T19:30:00Z', NULL, 0, 'PERSONALIZADO', 'PRESENCIAL'),
    ('40000000-0000-0000-0000-000000000007', 'Encontro de Casais', 'Formação com famílias', '00000000-0000-0000-0000-0000000000ff', '00000000-0000-0000-0000-000000000002', 'FORMATIVO', 'CONFIRMADO', '2026-06-14T08:00:00Z', '2026-06-14T12:00:00Z', NULL, 0, 'PERSONALIZADO', 'PRESENCIAL'),
    ('40000000-0000-0000-0000-000000000008', 'Reunião Ordinária do CPP', 'Planejamento pastoral', '00000000-0000-0000-0000-0000000000cc', '00000000-0000-0000-0000-000000000002', 'ADMINISTRATIVO', 'CONFIRMADO', '2026-06-10T20:00:00Z', '2026-06-10T21:30:00Z', NULL, 0, 'PERSONALIZADO', 'PRESENCIAL'),
    ('40000000-0000-0000-0000-000000000009', 'Live de Pentecostes', 'Transmissão ao vivo direto do estúdio paroquial', '00000000-0000-0000-0000-0000000000aa', '00000000-0000-0000-0000-000000000001', 'SOCIAL', 'CONFIRMADO', '2026-06-05T19:00:00Z', '2026-06-05T20:30:00Z', NULL, 0, 'PERSONALIZADO', 'ONLINE'),
    ('40000000-0000-0000-0000-000000000010', 'Grupo de Jovens Sentinelas', 'Momento de partilha e oração', '00000000-0000-0000-0000-0000000000bb', '00000000-0000-0000-0000-000000000003', 'FORMATIVO', 'CONFIRMADO', '2026-06-06T17:00:00Z', '2026-06-06T18:30:00Z', NULL, 0, 'PERSONALIZADO', 'PRESENCIAL'),
    ('40000000-0000-0000-0000-000000000011', 'Missa dos Catequistas', 'Celebração com benção dos catequistas', '00000000-0000-0000-0000-0000000000ee', '00000000-0000-0000-0000-000000000001', 'LITURGICO', 'CONFIRMADO', '2026-06-07T10:00:00Z', '2026-06-07T11:30:00Z', NULL, 0, 'PERSONALIZADO', 'PRESENCIAL'),
    ('40000000-0000-0000-0000-000000000012', 'Missa Dominical de Julho', 'Missa com a comunidade paroquial', '00000000-0000-0000-0000-0000000000dd', '00000000-0000-0000-0000-000000000001', 'LITURGICO', 'CONFIRMADO', '2026-07-26T18:00:00Z', '2026-07-26T19:30:00Z', NULL, 0, 'PERSONALIZADO', 'PRESENCIAL'),
    ('40000000-0000-0000-0000-000000000013', 'Reunião de Planejamento Pastoral', 'Avaliando o semestre', '00000000-0000-0000-0000-0000000000cc', '00000000-0000-0000-0000-000000000002', 'ADMINISTRATIVO', 'CONFIRMADO', '2026-07-28T19:30:00Z', '2026-07-28T21:00:00Z', NULL, 0, 'PERSONALIZADO', 'PRESENCIAL'),
    ('40000000-0000-0000-0000-000000000014', 'Missa de São Pedro e São Paulo', 'Celebração solene', '00000000-0000-0000-0000-0000000000dd', '00000000-0000-0000-0000-000000000001', 'LITURGICO', 'CONFIRMADO', '2026-07-30T19:00:00Z', '2026-07-30T20:30:00Z', NULL, 0, 'PERSONALIZADO', 'PRESENCIAL'),
    ('40000000-0000-0000-0000-000000000015', 'Encontro de Jovens de Férias', 'Momento de oração e lazer', '00000000-0000-0000-0000-0000000000bb', '00000000-0000-0000-0000-000000000003', 'SOCIAL', 'CONFIRMADO', '2026-07-31T17:00:00Z', '2026-07-31T19:00:00Z', NULL, 0, 'PERSONALIZADO', 'PRESENCIAL'),
    ('40000000-0000-0000-0000-000000000016', 'Missa da Família - Agosto', 'Abertura da semana da família', '00000000-0000-0000-0000-0000000000dd', '00000000-0000-0000-0000-000000000001', 'LITURGICO', 'CONFIRMADO', '2026-08-02T10:00:00Z', '2026-08-02T11:30:00Z', NULL, 0, 'PERSONALIZADO', 'PRESENCIAL'),
    -- Eventos em locais comunitários (fora da Matriz) — usados para testar alocação/empréstimo de ativos patrimoniais
    ('40000000-0000-0000-0000-000000000201', 'Festa Junina Comunitária - São José', 'Arraiá da comunidade com quermesse', '00000000-0000-0000-0000-0000000002c0', '00000000-0000-0000-0000-000000000005', 'SOCIAL', 'CONFIRMADO', '2026-07-10T18:00:00Z', '2026-07-15T22:00:00Z', NULL, 0, 'PERSONALIZADO', 'PRESENCIAL'),
    ('40000000-0000-0000-0000-000000000202', 'Reunião do Conselho - Santo Agostinho', 'Planejamento pastoral comunitário', '00000000-0000-0000-0000-0000000003c0', '00000000-0000-0000-0000-000000000006', 'ADMINISTRATIVO', 'CONFIRMADO', '2026-08-12T19:30:00Z', '2026-08-12T21:00:00Z', NULL, 0, 'PERSONALIZADO', 'PRESENCIAL'),
    ('40000000-0000-0000-0000-000000000203', 'Retiro Paroquial de Agosto', 'Retiro espiritual aberto a todas as comunidades', '00000000-0000-0000-0000-0000000000dd', '00000000-0000-0000-0000-000000000001', 'FORMATIVO', 'CONFIRMADO', '2026-08-20T08:00:00Z', '2026-08-20T18:00:00Z', NULL, 0, 'PERSONALIZADO', 'PRESENCIAL'),
    ('40000000-0000-0000-0000-000000000204', 'Encontro Comunitário de Agosto - São José', 'Encontro mensal da comunidade', '00000000-0000-0000-0000-0000000002c0', '00000000-0000-0000-0000-000000000005', 'SOCIAL', 'CONFIRMADO', '2026-08-25T18:00:00Z', '2026-08-26T22:00:00Z', NULL, 0, 'PERSONALIZADO', 'PRESENCIAL'),
    -- Eventos com novos cenários de teste: Falha de Execução e Conflito Deliberativo
    ('40000000-0000-0000-0000-000000000301', 'Encontro Diocesano de Lideranças', 'Tentativa de reserva automática falhou por colisão impeditiva.', '00000000-0000-0000-0000-0000000000cc', '00000000-0000-0000-0000-000000000002', 'FORMATIVO', 'FALHA_EXECUCAO', '2026-10-18T08:00:00Z', '2026-10-18T17:00:00Z', NULL, 0, 'PERSONALIZADO', 'PRESENCIAL'),
    ('40000000-0000-0000-0000-000000000302', 'Vigília Jovem com Show Musical', 'Vigília paroquial com momentos de louvor e adoração.', '00000000-0000-0000-0000-0000000000bb', '00000000-0000-0000-0000-000000000002', 'SOCIAL', 'CONFIRMADO', '2026-10-12T19:00:00Z', '2026-10-12T22:30:00Z', NULL, 0, 'PERSONALIZADO', 'PRESENCIAL'),
    ('40000000-0000-0000-0000-000000000303', 'Missa Solene da Padroeira do Brasil', 'Celebração com procissão e coroação.', '00000000-0000-0000-0000-0000000000dd', '00000000-0000-0000-0000-000000000002', 'LITURGICO', 'CONFIRMADO', '2026-10-12T18:00:00Z', '2026-10-12T21:00:00Z', NULL, 0, 'PERSONALIZADO', 'PRESENCIAL'),
    -- Novos Cenários: Dia Inteiro e Períodos (Madrugada, Manhã, Tarde, Noite)
    ('40000000-0000-0000-0000-000000000401', 'Festa da Padroeira - Dia Festivo Paroquial', 'Celebração de Dia Inteiro com quermesse e programação contínua.', '00000000-0000-0000-0000-0000000000dd', '00000000-0000-0000-0000-000000000002', 'LITURGICO', 'CONFIRMADO', '2026-10-15T03:00:00Z', '2026-10-16T02:59:59Z', NULL, 0, 'DIA_INTEIRO', 'PRESENCIAL'),
    ('40000000-0000-0000-0000-000000000402', 'Vigília Eucarística da Madrugada', 'Adoração ao Santíssimo na madrugada.', '00000000-0000-0000-0000-0000000000dd', '00000000-0000-0000-0000-000000000001', 'LITURGICO', 'CONFIRMADO', '2026-10-16T03:00:00Z', '2026-10-16T08:59:59Z', NULL, 0, 'MADRUGADA', 'PRESENCIAL'),
    ('40000000-0000-0000-0000-000000000403', 'Círculo Bíblico Matutino', 'Estudo bíblico na Sala de Reunião 2 durante o período matutino.', '00000000-0000-0000-0000-0000000000cc', '00000000-0000-0000-0000-000000000003', 'PASTORAL', 'CONFIRMADO', '2026-10-17T09:00:00Z', '2026-10-17T14:59:59Z', NULL, 0, 'MANHA', 'PRESENCIAL'),
    ('40000000-0000-0000-0000-000000000404', 'Encontro Pastoral da Criança', 'Atividades pastorais na Sala de Reunião 2 à tarde sem choque com o turno matutino.', '00000000-0000-0000-0000-0000000000ff', '00000000-0000-0000-0000-000000000003', 'PASTORAL', 'CONFIRMADO', '2026-10-17T15:00:00Z', '2026-10-17T20:59:59Z', NULL, 0, 'TARDE', 'PRESENCIAL'),
    ('40000000-0000-0000-0000-000000000405', 'Terço dos Homens e Partilha Noturna', 'Oração e partilha no período noturno.', '00000000-0000-0000-0000-0000000000dd', '00000000-0000-0000-0000-000000000001', 'PASTORAL', 'CONFIRMADO', '2026-10-17T21:00:00Z', '2026-10-18T02:59:59Z', NULL, 0, 'NOITE', 'PRESENCIAL'),
    ('40000000-0000-0000-0000-000000000406', 'Almoço Beneficente dos Jovens', 'Almoço que colide com o evento de Dia Inteiro no Salão Paroquial.', '00000000-0000-0000-0000-0000000000bb', '00000000-0000-0000-0000-000000000002', 'SOCIAL', 'CONFIRMADO', '2026-10-15T15:00:00Z', '2026-10-15T18:00:00Z', NULL, 0, 'PERSONALIZADO', 'PRESENCIAL'),
    -- Eventos de Teste com Novos Formatos (Online e Híbrido)
    ('40000000-0000-0000-0000-000000000701', 'Webinar Teológico Paroquial', 'Seminário 100% online por videoconferência com palestrante transmitindo da sala com notebook.', '00000000-0000-0000-0000-0000000000cc', '00000000-0000-0000-0000-000000000003', 'FORMATIVO', 'CONFIRMADO', '2026-11-25T19:30:00Z', '2026-11-25T21:30:00Z', NULL, 0, 'PERSONALIZADO', 'ONLINE'),
    ('40000000-0000-0000-0000-000000000702', 'Assembleia Paroquial Híbrida 2026', 'Assembleia com participantes presenciais no Salão Paroquial e transmissão simultânea online.', '00000000-0000-0000-0000-0000000000cc', '00000000-0000-0000-0000-000000000002', 'ADMINISTRATIVO', 'CONFIRMADO', '2026-11-28T09:00:00Z', '2026-11-28T12:00:00Z', NULL, 0, 'PERSONALIZADO', 'HIBRIDO');

-- Atualiza evento com conflito deliberativo pendente
UPDATE calendario.eventos 
SET conflict_state = 'PENDENTE',
    conflict_reason = 'Conflito detectado no Salão Paroquial e Projetor com a Missa Solene da Padroeira do Brasil'
WHERE id = '40000000-0000-0000-0000-000000000302';

UPDATE calendario.eventos 
SET conflict_state = 'PENDENTE',
    conflict_reason = 'Conflito detectado no Salão Paroquial com o evento de Dia Inteiro Festa da Padroeira'
WHERE id = '40000000-0000-0000-0000-000000000406';

-- Event with Extra Status
INSERT INTO calendario.eventos (id, titulo, descricao, organizacao_responsavel_id, local_id, categoria, status, inicio_utc, fim_utc, adicionado_extra_justificativa, version, tipo_horario, formato)
VALUES 
    ('40000000-0000-0000-0000-000000000002', 'Reunião Extraordinária Pascom', 'Urgência na cobertura do evento x', '00000000-0000-0000-0000-0000000000aa', '00000000-0000-0000-0000-000000000003', 'ADMINISTRATIVO', 'ADICIONADO_EXTRA', '2026-09-10T20:00:00Z', '2026-09-10T21:00:00Z', 'Necessidade de planejar live de última hora', 0, 'PERSONALIZADO', 'PRESENCIAL');

-- Recurring Events instances (Insert base event instance first with NULL recorrencia_id to satisfy FK)
INSERT INTO calendario.eventos (id, titulo, descricao, organizacao_responsavel_id, local_id, categoria, status, inicio_utc, fim_utc, projeto_id, recorrencia_id, version, tipo_horario, formato)
VALUES 
    ('40000000-0000-0000-0000-000000000101', 'Catequese Infantil - Turma A', 'Encontro semanal', '00000000-0000-0000-0000-0000000000ee', '00000000-0000-0000-0000-000000000003', 'FORMATIVO', 'CONFIRMADO', '2026-09-12T09:00:00Z', '2026-09-12T10:30:00Z', '30000000-0000-0000-0000-000000000001', NULL, 0, 'PERSONALIZADO', 'PRESENCIAL'),
    ('40000000-0000-0000-0000-000000000501', 'Formação Teológica de Sábado - Manhã', 'Formação matutina em série semanal', '00000000-0000-0000-0000-0000000000dd', '00000000-0000-0000-0000-000000000003', 'FORMATIVO', 'CONFIRMADO', '2026-10-10T09:00:00Z', '2026-10-10T14:59:59Z', NULL, NULL, 0, 'MANHA', 'PRESENCIAL');

-- Recurring Event stub (now can reference '40000000-0000-0000-0000-000000000101' as evento_base_id)
INSERT INTO calendario.eventos_recorrencia (id, evento_base_id, frequencia, intervalo, regra_json, data_fim_utc, version)
VALUES 
    ('50000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000101', 'SEMANAL', 1, '{"frequencia": "SEMANAL", "intervalo": 1, "diasDaSemana": ["SATURDAY"], "dataLimite": "2026-11-28"}', '2026-11-28T23:59:59Z', 0),
    ('50000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000501', 'SEMANAL', 1, '{"frequencia": "SEMANAL", "intervalo": 1, "diasDaSemana": ["SATURDAY"], "dataLimite": "2026-11-28"}', '2026-11-28T23:59:59Z', 0);

-- Update base event instance to link to its recurrence
UPDATE calendario.eventos 
SET recorrencia_id = '50000000-0000-0000-0000-000000000001'
WHERE id = '40000000-0000-0000-0000-000000000101';

UPDATE calendario.eventos 
SET recorrencia_id = '50000000-0000-0000-0000-000000000002'
WHERE id = '40000000-0000-0000-0000-000000000501';

-- Chave idempotente já processada para exercitar a proteção contra duplicidade de comandos.
INSERT INTO calendario.evento_idempotency (idempotency_key, request_hash, evento_id, response_status)
VALUES (
    'seed-criacao-evento-catequese-2026',
    'f5b93fd637b3b2e9b8fdc4f5d48f445f1d55e7cbe69bbfdcb2afe9f1390c42a7',
    '40000000-0000-0000-0000-000000000101',
    201
)
ON CONFLICT (idempotency_key) DO NOTHING;

-- Insert remaining recurring instances referencing recorrencia_id
INSERT INTO calendario.eventos (id, titulo, descricao, organizacao_responsavel_id, local_id, categoria, status, inicio_utc, fim_utc, projeto_id, recorrencia_id, version, tipo_horario, formato)
VALUES 
    ('40000000-0000-0000-0000-000000000102', 'Catequese Infantil - Turma A', 'Encontro semanal', '00000000-0000-0000-0000-0000000000ee', '00000000-0000-0000-0000-000000000003', 'FORMATIVO', 'CONFIRMADO', '2026-09-19T09:00:00Z', '2026-09-19T10:30:00Z', '30000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', 0, 'PERSONALIZADO', 'PRESENCIAL'),
    ('40000000-0000-0000-0000-000000000502', 'Formação Teológica de Sábado - Manhã', 'Formação matutina em série semanal', '00000000-0000-0000-0000-0000000000dd', '00000000-0000-0000-0000-000000000003', 'FORMATIVO', 'CONFIRMADO', '2026-10-17T09:00:00Z', '2026-10-17T14:59:59Z', NULL, '50000000-0000-0000-0000-000000000002', 0, 'MANHA', 'PRESENCIAL'),
    -- Eventos com Múltiplas Localidades Simultâneas (Cenários ricos de teste)
    ('40000000-0000-0000-0000-000000000601', 'Retiro Paroquial de Jovens e Lideranças', 'Retiro intensivo utilizando Salão Paroquial, Sala de Catequese e Igreja Matriz.', '00000000-0000-0000-0000-0000000000bb', '00000000-0000-0000-0000-000000000002', 'FORMATIVO', 'CONFIRMADO', '2026-11-14T08:00:00Z', '2026-11-15T18:00:00Z', '30000000-0000-0000-0000-000000000004', NULL, 0, 'PERSONALIZADO', 'PRESENCIAL'),
    ('40000000-0000-0000-0000-000000000602', 'Assembleia Pastoral e Almoço Comunitário', 'Assembleia deliberativa e confraternização utilizando Salão Paroquial e Cozinha.', '00000000-0000-0000-0000-0000000000cc', '00000000-0000-0000-0000-000000000002', 'ADMINISTRATIVO', 'CONFIRMADO', '2026-11-21T09:00:00Z', '2026-11-21T15:00:00Z', NULL, NULL, 0, 'PERSONALIZADO', 'PRESENCIAL');

-- Vínculo de Localidades aos Eventos (calendario.eventos_locais)
-- 1. População base de compatibilidade de todos os eventos
INSERT INTO calendario.eventos_locais (evento_id, local_id, criado_em_utc, atualizado_em_utc, criado_por, atualizado_por)
SELECT id, local_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed'
FROM calendario.eventos
WHERE local_id IS NOT NULL
ON CONFLICT (evento_id, local_id) DO NOTHING;

-- 2. Localidades adicionais simultâneas para eventos com múltiplos locais
INSERT INTO calendario.eventos_locais (evento_id, local_id, criado_em_utc, atualizado_em_utc, criado_por, atualizado_por)
VALUES
    -- Retiro Paroquial: Salão Paroquial (já herdado acima), Igreja Matriz e Sala de Catequese 1
    ('40000000-0000-0000-0000-000000000601', '00000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed'),
    ('40000000-0000-0000-0000-000000000601', '00000000-0000-0000-0000-000000000003', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed'),
    -- Assembleia Pastoral: Salão Paroquial (já herdado) e Cozinha Paroquial
    ('40000000-0000-0000-0000-000000000602', '00000000-0000-0000-0000-000000000004', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed')
ON CONFLICT (evento_id, local_id) DO NOTHING;

-- Approvals
INSERT INTO calendario.aprovacoes (
    id, evento_id, tipo_solicitacao, aprovador_papel, status, criado_em_utc, 
    decidido_em_utc, solicitante_id, aprovador_id, decision_observacao, 
    action_payload_json, mensagem_erro_execucao, aprovacao_pai_id, conflitos_json,
    data_ocorrencia_utc, justificativa, version
) VALUES 
    (
        '60000000-0000-0000-0000-000000000001', NULL, 'CRIACAO_EVENTO', 'PAROCO', 'PENDENTE', '2026-05-25T12:00:00Z', 
        NULL, 'maria.catequese', NULL, NULL, 
        '{"idempotencyKey": "idemp-0000-000000000001", "titulo": "Retiro Geral de Catequistas 2026", "inicio": "2026-09-15T08:00:00Z", "fim": "2026-09-15T18:00:00Z", "localId": "00000000-0000-0000-0000-000000000002", "descricao": "Encontro anual de planejamento e espiritualidade.", "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000ee", "categoria": "FORMATIVO", "status": "CONFIRMADO"}', NULL,
        NULL, NULL, NULL, NULL, 0
    ),
    (
        '60000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000001', 'EDICAO_EVENTO', 'PAROCO', 'PENDENTE', '2026-05-25T13:00:00Z', 
        NULL, 'joao.liturgia', NULL, NULL, 
        '{"titulo": "Missa de Abertura da Catequese (Novo Horário)", "inicio": "2026-09-07T17:00:00Z", "fim": "2026-09-07T18:30:00Z", "localId": "00000000-0000-0000-0000-000000000001", "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000ee", "categoria": "LITURGICO"}', NULL,
        NULL, NULL, NULL, NULL, 0
    ),
    (
        '60000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000101', 'CANCELAMENTO', 'PAROCO', 'PENDENTE', '2026-05-25T14:00:00Z', 
        NULL, 'maria.catequese', NULL, NULL, 
        '{"motivo": "Falta de energia no Salão Paroquial"}', NULL,
        NULL, NULL, NULL, NULL, 0
    ),
    (
        '60000000-0000-0000-0000-000000000004', '40000000-0000-0000-0000-000000000001', 'CRIACAO_EVENTO', 'PAROCO', 'APROVADA', '2026-01-15T08:00:00Z', 
        '2026-01-16T09:00:00Z', 'padre.pedro', '10000000-0000-0000-0000-000000000001', 'Missa importante, autorizada.', 
        '{"idempotencyKey": "idemp-0000-000000000004", "titulo": "Missa de Abertura da Catequese", "inicio": "2026-09-07T18:00:00Z", "fim": "2026-09-07T19:30:00Z", "localId": "00000000-0000-0000-0000-000000000001", "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000ee", "categoria": "LITURGICO", "status": "CONFIRMADO"}', NULL,
        NULL, NULL, NULL, 'Abertura oficial do ano catequético paroquial.', 0
    ),
    (
        '60000000-0000-0000-0000-000000000005', NULL, 'EDICAO_EVENTO', 'PAROCO', 'REPROVADA', '2026-02-09T10:00:00Z', 
        '2026-02-10T11:00:00Z', 'joao.liturgia', '10000000-0000-0000-0000-000000000001', 'Horário conflita com casamento agendado.', 
        '{"titulo": "Missa de Abertura (Horário Conflitante)", "inicio": "2026-09-07T19:00:00Z", "fim": "2026-09-07T20:30:00Z", "localId": "00000000-0000-0000-0000-000000000001", "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000ee", "categoria": "LITURGICO"}', NULL,
        NULL, NULL, NULL, NULL, 0
    ),
    (
        '60000000-0000-0000-0000-000000000006', NULL, 'CRIACAO_EVENTO', 'PAROCO', 'FALHA_EXECUCAO', '2026-02-07T10:00:00Z', 
        '2026-02-07T10:05:00Z', 'joao.liturgia', '10000000-0000-0000-0000-000000000001', 'Tentativa de aprovação automática falhou.', 
        '{"idempotencyKey": "idemp-0000-000000000006", "titulo": "Missa Festiva Pascom", "inicio": "2026-09-07T18:00:00Z", "fim": "2026-09-07T19:30:00Z", "localId": "00000000-0000-0000-0000-000000000001", "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000ee", "categoria": "LITURGICO", "status": "CONFIRMADO"}', 'Conflito de horário detectado no local Igreja Principal.',
        NULL, NULL, NULL, NULL, 0
    ),
    -- Novos Cenários:
    -- 1. Conflito Deliberativo Analítico (Local e Ativo Patrimonial)
    (
        '60000000-0000-0000-0000-000000000010', '40000000-0000-0000-0000-000000000302', 'CRIACAO_EVENTO', 'PAROCO', 'PENDENTE', '2026-09-10T11:00:00Z', 
        NULL, 'pedro.liturgia', NULL, NULL, 
        '{"titulo": "Vigília Jovem com Show Musical", "inicio": "2026-10-12T19:00:00Z", "fim": "2026-10-12T22:30:00Z", "localId": "00000000-0000-0000-0000-000000000002", "ativoIds": ["80000000-0000-0000-0000-000000000001"]}', 
        NULL, NULL, 
        '[{"tipo": "LOCAL", "itemNome": "Salão Paroquial", "eventoConflitanteTitulo": "Missa Solene da Padroeira do Brasil", "solicitanteNome": "Padre Pedro", "inicioOcupadoUtc": "2026-10-12T18:00:00Z", "fimOcupadoUtc": "2026-10-12T21:00:00Z", "detalhe": "Sobreposição de 2 horas no Salão Paroquial"}, {"tipo": "ATIVO", "itemNome": "Projetor Epson PowerLite", "eventoConflitanteTitulo": "Missa Solene da Padroeira do Brasil", "solicitanteNome": "Padre Pedro", "inicioOcupadoUtc": "2026-10-12T18:00:00Z", "fimOcupadoUtc": "2026-10-12T21:00:00Z", "detalhe": "Ativo patrimonial já reservado para transmissão da celebração"}]', 
        '2026-10-12T19:00:00Z', 
        'Solicitação de uso do Salão Paroquial e Projetor para a Vigília Anual da Juventude.', 0
    ),
    -- Cenário Multi-Local: Solicitação com múltiplos locais (Salão Paroquial + Sala de Catequese 1 + Igreja Matriz)
    (
        '60000000-0000-0000-0000-000000000008', '40000000-0000-0000-0000-000000000601', 'EDICAO_EVENTO', 'PAROCO', 'PENDENTE', '2026-09-11T14:00:00Z', 
        NULL, 'maria.catequese', NULL, NULL, 
        '{"titulo": "Retiro Paroquial de Jovens e Lideranças (Ajuste de Locais)", "inicio": "2026-11-14T08:00:00Z", "fim": "2026-11-15T18:00:00Z", "localId": "00000000-0000-0000-0000-000000000002", "localIds": ["00000000-0000-0000-0000-000000000002", "00000000-0000-0000-0000-000000000003", "00000000-0000-0000-0000-000000000001"], "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000bb", "categoria": "FORMATIVO"}', NULL,
        NULL, NULL, NULL, 'Solicitação para inclusão formal de múltiplos espaços paroquiais simultâneos.', 0
    ),
    -- 2. Recorrência Mestre-Detalhe (Série Formativa de 4 semanas)
    (
        '60000000-0000-0000-0000-000000000020', NULL, 'CRIACAO_EVENTO', 'PAROCO', 'PENDENTE', '2026-09-01T10:00:00Z', 
        NULL, 'maria.catequese', NULL, NULL, 
        '{"titulo": "Ciclo Bíblico Paroquial 2026", "categoria": "FORMATIVO", "localId": "00000000-0000-0000-0000-000000000003", "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000ee"}', 
        NULL, NULL, NULL, NULL, 
        'Ciclo Bíblico Paroquial: formação contínua de 4 semanas aprovada em planejamento pastoral.', 0
    ),
    (
        '60000000-0000-0000-0000-000000000021', NULL, 'CRIACAO_EVENTO', 'PAROCO', 'APROVADA', '2026-09-01T10:00:00Z', 
        '2026-09-02T14:00:00Z', 'maria.catequese', '10000000-0000-0000-0000-000000000001', 'Ocorrência pontual autorizada.', 
        '{"titulo": "Ciclo Bíblico - Semana 1", "inicio": "2026-10-06T19:30:00Z", "fim": "2026-10-06T21:00:00Z", "localId": "00000000-0000-0000-0000-000000000003", "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000ee", "categoria": "FORMATIVO", "status": "CONFIRMADO"}', 
        NULL, '60000000-0000-0000-0000-000000000020', NULL, '2026-10-06T19:30:00Z', 
        'Ciclo Bíblico Paroquial - Encontro 1', 0
    ),
    (
        '60000000-0000-0000-0000-000000000022', NULL, 'CRIACAO_EVENTO', 'PAROCO', 'PENDENTE', '2026-09-01T10:00:00Z', 
        NULL, 'maria.catequese', NULL, NULL, 
        '{"titulo": "Ciclo Bíblico - Semana 2", "inicio": "2026-10-13T19:30:00Z", "fim": "2026-10-13T21:00:00Z", "localId": "00000000-0000-0000-0000-000000000003", "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000ee", "categoria": "FORMATIVO", "status": "CONFIRMADO"}', 
        NULL, '60000000-0000-0000-0000-000000000020', 
        '[{"tipo": "LOCAL", "itemNome": "Sala de Catequese 01", "eventoConflitanteTitulo": "Encontro de Pais da Juventude", "solicitanteNome": "Pedro Junior", "inicioOcupadoUtc": "2026-10-13T19:00:00Z", "fimOcupadoUtc": "2026-10-13T21:00:00Z", "detalhe": "Sala reservada pela Pastoral da Juventude no mesmo horário"}]', 
        '2026-10-13T19:30:00Z', 
        'Ciclo Bíblico Paroquial - Encontro 2', 0
    ),
    (
        '60000000-0000-0000-0000-000000000023', NULL, 'CRIACAO_EVENTO', 'PAROCO', 'PENDENTE', '2026-09-01T10:00:00Z', 
        NULL, 'maria.catequese', NULL, NULL, 
        '{"titulo": "Ciclo Bíblico - Semana 3", "inicio": "2026-10-20T19:30:00Z", "fim": "2026-10-20T21:00:00Z", "localId": "00000000-0000-0000-0000-000000000003", "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000ee", "categoria": "FORMATIVO", "status": "CONFIRMADO"}', 
        NULL, '60000000-0000-0000-0000-000000000020', NULL, '2026-10-20T19:30:00Z', 
        'Ciclo Bíblico Paroquial - Encontro 3', 0
    ),
    (
        '60000000-0000-0000-0000-000000000024', NULL, 'CRIACAO_EVENTO', 'PAROCO', 'REPROVADA', '2026-09-01T10:00:00Z', 
        '2026-09-03T16:00:00Z', 'maria.catequese', '10000000-0000-0000-0000-000000000005', 'Data coincide com a Assembleia Geral do Povo de Deus.', 
        '{"titulo": "Ciclo Bíblico - Semana 4", "inicio": "2026-10-27T19:30:00Z", "fim": "2026-10-27T21:00:00Z", "localId": "00000000-0000-0000-0000-000000000003", "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000ee", "categoria": "FORMATIVO", "status": "CONFIRMADO"}', 
        NULL, '60000000-0000-0000-0000-000000000020', NULL, '2026-10-27T19:30:00Z', 
        'Ciclo Bíblico Paroquial - Encontro 4', 0
    ),
    -- 3. Ações do Secretário com Justificativa Mandatória e Deliberação do Conselho
    (
        '60000000-0000-0000-0000-000000000030', NULL, 'CRIACAO_EVENTO', 'PAROCO', 'PENDENTE', '2026-09-12T14:00:00Z', 
        NULL, 'secretaria.paroquial', NULL, NULL, 
        '{"titulo": "Casamento Comunitário - Ensaio Geral", "inicio": "2026-11-05T19:00:00Z", "fim": "2026-11-05T20:30:00Z", "localId": "00000000-0000-0000-0000-000000000001", "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000dd", "categoria": "LITURGICO"}', 
        NULL, NULL, NULL, '2026-11-05T19:00:00Z', 
        'Atendimento presencial no balcão da secretaria: agendamento solicitado pelos noivos para celebração comunitária externa.', 0
    ),
    (
        '60000000-0000-0000-0000-000000000031', '40000000-0000-0000-0000-000000000005', 'CANCELAMENTO', 'PAROCO', 'APROVADA', '2026-09-14T09:00:00Z', 
        '2026-09-14T11:00:00Z', 'secretaria.paroquial', '10000000-0000-0000-0000-000000000005', 'Cancelamento homologado em alçada global pela Mesa Diretora do CPP.', 
        '{"motivo": "Cancelamento emergencial a pedido do pregador visitante por motivos de saúde."}', 
        NULL, NULL, NULL, '2026-06-18T19:00:00Z', 
        'Cancelamento emergencial solicitado presencialmente na secretaria com atestado médico anexado.', 0
    ),
    -- 4. Cenários de Formatos: Evento Online puro e Evento Online com Ativo em deliberação
    (
        '60000000-0000-0000-0000-000000000040', NULL, 'CRIACAO_EVENTO', 'PAROCO', 'PENDENTE', '2026-09-15T08:30:00Z', 
        NULL, 'maria.catequese', NULL, NULL, 
        '{"titulo": "Formação Bíblica Remota (100% Online)", "inicio": "2026-11-20T20:00:00Z", "fim": "2026-11-20T21:30:00Z", "formato": "ONLINE", "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000ee", "categoria": "FORMATIVO"}', 
        NULL, NULL, NULL, '2026-11-20T20:00:00Z', 
        'Formação 100% remota via videoconferência para catequistas que residem em áreas rurais.', 0
    ),
    (
        '60000000-0000-0000-0000-000000000041', NULL, 'CRIACAO_EVENTO', 'PAROCO', 'PENDENTE', '2026-09-15T09:00:00Z', 
        NULL, 'joao.liturgia', NULL, NULL, 
        '{"titulo": "Terço Paroquial Transmitido", "inicio": "2026-11-21T18:00:00Z", "fim": "2026-11-21T19:00:00Z", "formato": "ONLINE", "ativoIds": ["80000000-0000-0000-0000-000000000001"], "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000ee", "categoria": "LITURGICO"}', 
        NULL, NULL, NULL, '2026-11-21T18:00:00Z', 
        'Transmissão online pelas redes sociais da paróquia com utilização do Notebook paroquial.', 0
    );

-- Observations
INSERT INTO calendario.observacoes_evento (id, evento_id, usuario_id, tipo, conteudo, criado_em_utc, version)
VALUES 
    ('96000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000002', 'NOTA', 'Lembrar de avisar os coroinhas sobre a missa.', '2026-02-01T14:00:00Z', 1)
ON CONFLICT (id) DO NOTHING;

-- Revisão auditável da nota, preservando o conteúdo anterior para histórico colaborativo.
INSERT INTO calendario.observacoes_nota_revisoes (
    id, observacao_id, conteudo_anterior, conteudo_novo, revisado_por_usuario_id, revisado_em_utc, version
) VALUES (
    '97000000-0000-0000-0000-000000000001',
    '96000000-0000-0000-0000-000000000001',
    'Lembrar de avisar os coroinhas sobre a missa.',
    'Lembrar de avisar os coroinhas e os leitores sobre a missa.',
    '10000000-0000-0000-0000-000000000002',
    '2026-02-02T14:00:00Z',
    0
)
ON CONFLICT (id) DO NOTHING;

-- Project Approvals (Solicitações de Aprovação de Projetos)
INSERT INTO calendario.projeto_solicitacoes_aprovacao (
    id, projeto_id, tipo_solicitacao, status, solicitante_id, 
    solicitante_papel, solicitante_tipo_organizacao, action_payload_json, 
    cancelar_eventos_vinculados, criado_em_utc, decidido_em_utc, aprovador_id, 
    decision_observacao, version
) VALUES 
    (
        '70000000-0000-0000-0000-000000000001', NULL, 'CRIACAO', 'PENDENTE', 'maria.catequese', 
        'coordenador', 'PASTORAL', '{"nome": "Novo Projeto Catequese 2027", "descricao": "Planejamento para o ano de 2027", "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000ee", "inicio": "2027-02-01T00:00:00Z", "fim": "2027-12-15T23:59:59Z"}', 
        FALSE, '2026-06-01T12:00:00Z', NULL, NULL, 
        NULL, 0
    ),
    (
        '70000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', 'CANCELAMENTO', 'PENDENTE', 'pedro.liturgia', 
        'coordenador', 'PASTORAL', NULL, 
        TRUE, '2026-06-01T13:00:00Z', NULL, NULL, 
        NULL, 0
    ),
    (
        '70000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000001', 'CRIACAO', 'APROVADA', 'maria.catequese', 
        'coordenador', 'PASTORAL', '{"nome": "Catequese 2026", "descricao": "Ano letivo da catequese infantil", "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000ee", "inicio": "2026-02-01T00:00:00Z", "fim": "2026-12-15T23:59:59Z"}', 
        FALSE, '2026-01-10T10:00:00Z', '2026-01-11T09:00:00Z', 'padre.pedro', 
        'Aprovado para início em ano aberto', 0
    ),
    (
        '70000000-0000-0000-0000-000000000004', NULL, 'CRIACAO', 'REPROVADA', 'pedro.liturgia', 
        'coordenador', 'PASTORAL', '{"nome": "Festival de Bandas Católicas", "descricao": "Evento sem verba prevista", "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000bb", "inicio": "2026-11-01T00:00:00Z", "fim": "2026-11-02T23:59:59Z"}', 
        FALSE, '2026-03-01T10:00:00Z', '2026-03-03T14:00:00Z', 'padre.pedro', 
        'Orçamento incompatível com o caixa pastoral deste exercício.', 0
    );

-- Audit Trail
INSERT INTO calendario.auditoria_operacoes (id, organizacao_id, evento_id, recurso_tipo, recurso_id, acao, resultado, ator, ator_usuario_id, correlation_id, detalhes_auditaveis_json, ocorrido_em_utc)
VALUES 
    (gen_random_uuid(), '00000000-0000-0000-0000-0000000000ee', '40000000-0000-0000-0000-000000000001', 'EVENTO', '40000000-0000-0000-0000-000000000001', 'CRIACAO', 'SUCCESS', 'maria.catequese', '10000000-0000-0000-0000-000000000002', '60000000-0000-0000-0000-000000000004', '{"titulo": "Missa de Abertura da Catequese"}', '2026-01-15T08:00:00Z'),
    (gen_random_uuid(), '00000000-0000-0000-0000-0000000000ee', '40000000-0000-0000-0000-000000000001', 'EVENTO', '40000000-0000-0000-0000-000000000001', 'APROVACAO', 'SUCCESS', 'padre.pedro', '10000000-0000-0000-0000-000000000001', '60000000-0000-0000-0000-000000000004', '{"status": "CONFIRMADO"}', '2026-01-16T09:00:00Z');

-- ============================================================================
-- 2. PATRIMÔNIO — Ativos Patrimoniais e Alocações (massa de teste)
-- ============================================================================
-- Cobre os cenários: alocação direta (mesma organização), empréstimo entre
-- organizações (pendente de aprovação), manutenção, atraso de devolução (com
-- impacto em reserva futura) e ativo baixado. Organizações donas: Matriz (100),
-- Comunidade São José Allamano (200) e Comunidade Santo Agostinho (300) — as
-- únicas elegíveis a possuir ativos.

INSERT INTO calendario.ativos_patrimoniais (
    id, nome, codigo_tombamento, descricao, categoria, status, is_reservavel, organizacao_id,
    data_aquisicao, valor_aquisicao, cadastrado_por_id, criado_em_utc, atualizado_em_utc, deleted_at, version
) VALUES
    ('80000000-0000-0000-0000-000000000001', 'Projetor Multimídia Epson', 'PAT-2026-0001', 'Full HD, 3500 lumens', 'ELETRONICO', 'DISPONIVEL', TRUE, '00000000-0000-0000-0000-000000000100', '2024-03-10', 3500.00, NULL, '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z', NULL, 0),
    ('80000000-0000-0000-0000-000000000002', 'Caixa de Som Amplificada', 'PAT-2026-0002', NULL, 'ELETRONICO', 'DISPONIVEL', TRUE, '00000000-0000-0000-0000-000000000200', '2024-06-15', 1800.00, NULL, '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z', NULL, 0),
    ('80000000-0000-0000-0000-000000000003', 'Kit 100 Cadeiras Plásticas', 'PAT-2026-0003', NULL, 'MOBILIARIO', 'DISPONIVEL', TRUE, '00000000-0000-0000-0000-000000000300', '2023-11-20', 4200.00, NULL, '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z', NULL, 0),
    ('80000000-0000-0000-0000-000000000004', 'Notebook Dell Administrativo', 'PAT-2026-0004', 'Uso restrito à secretaria', 'ELETRONICO', 'DISPONIVEL', FALSE, '00000000-0000-0000-0000-000000000100', '2025-02-01', 3200.00, NULL, '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z', NULL, 0),
    ('80000000-0000-0000-0000-000000000005', 'Kit Som Portátil Comunitário', 'PAT-2026-0005', NULL, 'ELETRONICO', 'DISPONIVEL', TRUE, '00000000-0000-0000-0000-000000000300', '2024-09-05', 1500.00, NULL, '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z', NULL, 0),
    ('80000000-0000-0000-0000-000000000006', 'Retroprojetor Antigo', 'PAT-2026-0006', 'Lâmpada e lente em reparo', 'ELETRONICO', 'EM_MANUTENCAO', FALSE, '00000000-0000-0000-0000-000000000100', '2018-04-12', 200.00, NULL, '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z', NULL, 0),
    ('80000000-0000-0000-0000-000000000007', 'Som Amplificado Comunitário', 'PAT-2026-0007', NULL, 'ELETRONICO', 'DISPONIVEL', TRUE, '00000000-0000-0000-0000-000000000200', '2024-02-20', 1600.00, NULL, '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z', NULL, 0),
    ('80000000-0000-0000-0000-000000000008', 'Armário Antigo (Fora de Uso)', 'PAT-2026-0008', 'Desincorporado do patrimônio', 'MOBILIARIO', 'DISPONIVEL', FALSE, '00000000-0000-0000-0000-000000000100', '2015-01-01', 0.00, NULL, '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z', '2026-01-05T00:00:00Z', 0),
    ('80000000-0000-0000-0000-000000000009', 'Tenda Sanfonada 3x3m', 'PAT-2026-0009', 'Tenda para eventos externos e quermesses', 'INFRAESTRUTURA', 'RESERVADO', TRUE, '00000000-0000-0000-0000-000000000100', '2025-05-10', 1200.00, '10000000-0000-0000-0000-000000000001', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z', NULL, 0),
    ('80000000-0000-0000-0000-000000000010', 'Microfone Sem Fio Duplo', 'PAT-2026-0010', 'Receptor UHF com 2 microfones bastão', 'ELETRONICO', 'RESERVADO', TRUE, '00000000-0000-0000-0000-000000000100', '2025-08-20', 850.00, '10000000-0000-0000-0000-000000000001', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z', NULL, 0)
ON CONFLICT (id) DO NOTHING;

-- Mantém o contador de tombamento de 2026 à frente dos 10 números usados acima,
-- para que o próximo ativo criado pela aplicação gere PAT-2026-0011 sem colidir.
INSERT INTO calendario.patrimonio_numeracao_anual (ano, ultimo_numero, version)
VALUES (2026, 10, 0)
ON CONFLICT (ano) DO NOTHING;

-- Manutenção em andamento do retroprojetor antigo (Matriz) — mantém o ativo 0006 em EM_MANUTENCAO.
INSERT INTO calendario.ativo_manutencoes (
    id, ativo_id, descricao_reparo, data_inicio, data_fim_prevista, data_fim_real,
    registrado_por_id, criado_em_utc, atualizado_em_utc, version
) VALUES 
    (
        '82000000-0000-0000-0000-000000000001', '80000000-0000-0000-0000-000000000006',
        'Reparo da lâmpada e da lente do retroprojetor.', '2026-07-28T08:00:00Z', '2026-08-10T18:00:00Z', NULL,
        '10000000-0000-0000-0000-000000000001', '2026-07-28T08:00:00Z', '2026-07-28T08:00:00Z', 0
    ),
    (
        '82000000-0000-0000-0000-000000000002', '80000000-0000-0000-0000-000000000002',
        'Troca de potenciômetro e conector XLR.', '2026-05-01T08:00:00Z', '2026-05-05T18:00:00Z', '2026-05-04T16:00:00Z',
        '10000000-0000-0000-0000-000000000001', '2026-05-01T08:00:00Z', '2026-05-04T16:00:00Z', 0
    )
ON CONFLICT (id) DO NOTHING;

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
(2035, 'C', '2034-12-03 00:00:00+00', '2035-12-01 23:59:59+00', CURRENT_TIMESTAMP)
ON CONFLICT (ano) DO NOTHING;

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
('75000000-0000-0000-0000-000000002035', 'Tempo Comum 2035', 'TEMPO_COMUM', 'VERDE', 1, 14, 12, 1, 1.0, 'Tempo Comum 2035', 2035)
ON CONFLICT (id) DO NOTHING;



-- Solenidades Litúrgicas
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
('b1c2d3e4-f5a6-7b8c-9d0e-1f2a3b4c5d20', 'SAGRADO_CORACAO', 'Sagrado Coração de Jesus', 'MOVEL', NULL, NULL, 68, 'BRANCO', 2.0, 2, 'Solenidade do Sagrado Coração de Jesus', TRUE)
ON CONFLICT (id) DO NOTHING;

-- Atas de reunião do Conselho Pastoral Comunitário (CPC): homologada, pendente de homologação e vetada
INSERT INTO calendario.ata_reuniao (
    id, tipo, organizacao_convocante_id, organizacao_associada_id, titulo, data_reuniao, carater, modalidade,
    status, conteudo, numero, ano, membros_convocados, redator_id, homologador_id, motivo_veto, decidido_em_utc,
    ata_retificada_id, justificativa_retificacao, criado_em_utc
) VALUES
    ('91000000-0000-0000-0000-000000000001', 'REUNIAO_CPC', '00000000-0000-0000-0000-0000000002c0', NULL, 'Aprovação das Obras de Reforma do Salão Comunitário', '2026-05-10', 'ORDINARIA', 'PRESENCIAL', 'HOMOLOGADA', '{"titulo": "Aprovação das Obras de Reforma do Salão Comunitário", "dataReuniao": "2026-05-10", "local": "Salão da Comunidade São José Allamano", "carater": "ORDINARIA", "modalidade": "PRESENCIAL", "presenca": {"nominal": [{"fielId": "1f000000-0000-0000-0000-000000000005", "funcao": "Secretário do Conselho", "situacao": "PRESENTE"}, {"fielId": "1f000000-0000-0000-0000-000000000007", "funcao": null, "situacao": "PRESENTE"}], "total": null}, "ataAnterior": {"ataId": null, "aprovacao": "NAO_APLICAVEL", "ressalvas": null}, "itens": [{"ordem": 1, "titulo": "Aprovação das Obras de Reforma do Salão Comunitário", "relato": "Orçamento para reparo do telhado e pintura externa do salão.", "deliberacoes": [{"ordem": 1, "texto": "Aprovar o orçamento para reparo do telhado e pintura externa do salão.", "resultado": "PARECER_FAVORAVEL", "votacao": {"favor": 2, "contra": 0, "abstencoes": 0}, "conflitos": []}]}]}', 1, 2026, NULL, 'maria.conselho', '10000000-0000-0000-0000-000000000001', NULL, '2026-05-12T14:30:00Z', NULL, NULL, '2026-05-11T10:00:00Z'),
    ('91000000-0000-0000-0000-000000000002', 'REUNIAO_CPC', '00000000-0000-0000-0000-0000000003c0', NULL, 'Aquisição de Novo Sistema de Som', '2026-06-01', 'ORDINARIA', 'PRESENCIAL', 'PENDENTE_HOMOLOGACAO', '{"titulo": "Aquisição de Novo Sistema de Som", "dataReuniao": "2026-06-01", "local": "Igreja Santo Agostinho", "carater": "ORDINARIA", "modalidade": "PRESENCIAL", "presenca": {"nominal": [{"fielId": "1f000000-0000-0000-0000-000000000005", "funcao": "Secretário do Conselho", "situacao": "PRESENTE"}, {"fielId": "1f000000-0000-0000-0000-000000000007", "funcao": null, "situacao": "PRESENTE"}], "total": null}, "ataAnterior": {"ataId": null, "aprovacao": "NAO_APLICAVEL", "ressalvas": null}, "itens": [{"ordem": 1, "titulo": "Aquisição de Novo Sistema de Som", "relato": "Proposta de compra de microfones sem fio e mesa de som para as celebrações.", "deliberacoes": [{"ordem": 1, "texto": "Recomendar a compra de microfones sem fio e mesa de som.", "resultado": "PARECER_FAVORAVEL", "votacao": {"favor": 2, "contra": 0, "abstencoes": 0}, "conflitos": []}]}]}', NULL, NULL, NULL, 'maria.conselho', NULL, NULL, NULL, NULL, NULL, '2026-06-02T09:00:00Z'),
    ('91000000-0000-0000-0000-000000000003', 'REUNIAO_CPC', '00000000-0000-0000-0000-0000000002c0', NULL, 'Alteração dos Horários das Missas de Domingo', '2026-04-15', 'ORDINARIA', 'PRESENCIAL', 'VETADA', '{"titulo": "Alteração dos Horários das Missas de Domingo", "dataReuniao": "2026-04-15", "local": "Salão da Comunidade São José Allamano", "carater": "ORDINARIA", "modalidade": "PRESENCIAL", "presenca": {"nominal": [{"fielId": "1f000000-0000-0000-0000-000000000005", "funcao": "Secretário do Conselho", "situacao": "PRESENTE"}, {"fielId": "1f000000-0000-0000-0000-000000000007", "funcao": null, "situacao": "PRESENTE"}], "total": null}, "ataAnterior": {"ataId": null, "aprovacao": "NAO_APLICAVEL", "ressalvas": null}, "itens": [{"ordem": 1, "titulo": "Alteração dos Horários das Missas de Domingo", "relato": "Mudança do horário da missa matutina das 08h00 para 07h30.", "deliberacoes": [{"ordem": 1, "texto": "Mudar o horário da missa matutina das 08h00 para 07h30.", "resultado": "PARECER_FAVORAVEL", "votacao": {"favor": 2, "contra": 0, "abstencoes": 0}, "conflitos": []}]}]}', NULL, NULL, NULL, 'maria.conselho', '10000000-0000-0000-0000-000000000001', 'Horário inviabiliza atendimento de confissões prévias pelo pároco.', '2026-04-17T16:00:00Z', NULL, NULL, '2026-04-16T11:00:00Z')
ON CONFLICT (id) DO NOTHING;

-- Grupos Paroquiais Envolvidos em Eventos
INSERT INTO calendario.eventos_envolvidos (evento_id, organizacao_id, papel_participacao)
VALUES
    ('40000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-0000000000aa', 'APOIO'),
    ('40000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-0000000000aa', 'APOIO'),
    ('40000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-0000000000ee', 'APOIO'),
    ('40000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-0000000000bb', 'RESPONSAVEL')
ON CONFLICT (evento_id, organizacao_id) DO NOTHING;

-- Observações de Ativos Patrimoniais (Manutenção / Histórico)
INSERT INTO calendario.observacoes_ativo_patrimonial (
    id, ativo_patrimonial_id, usuario_id, tipo, conteudo, criado_em_utc, removida, version
) VALUES (
    '92000000-0000-0000-0000-000000000001',
    '80000000-0000-0000-0000-000000000006',
    '10000000-0000-0000-0000-000000000001',
    'MANUTENCAO',
    'Encaminhado para assistência técnica especializada para troca de lâmpada e substituição da lente de projeção.',
    '2026-07-28T08:30:00Z',
    FALSE,
    0
) ON CONFLICT (id) DO NOTHING;

-- Cadastro de Fiéis: Matriz como vínculo inicial e dados de contato brutos.
UPDATE calendario.fieis
SET pais_origem = 'BRA', celular = regexp_replace(COALESCE(celular_whatsapp, telefone), '\\D', '', 'g'),
    is_whatsapp = COALESCE(celular_whatsapp IS NOT NULL, FALSE),
    unidade_territorial_id = '00000000-0000-0000-0000-000000000100', is_dizimista = FALSE
WHERE pais_origem IS NULL OR unidade_territorial_id IS NULL;

-- ============================================================================
-- 3. CONFIGURAÇÃO DA PARÓQUIA & PAPÉIS GLOBAIS
-- ============================================================================
INSERT INTO calendario.paroquia_configuracao (
    id, nome, diocese, endereco, logo_bytes, cor_primaria_hex, cor_secundaria_hex, version
) VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Paróquia Nossa Senhora de Fátima',
    'Diocese de Campo Limpo',
    'Praça da Matriz, 100, Centro, São Paulo - SP, CEP 08000-000',
    NULL,
    '#1976D2',
    '#DC004E',
    0
) ON CONFLICT (id) DO NOTHING;

INSERT INTO calendario.papel_global (
    id, fiel_id, papel, ativo, criado_em_utc, atualizado_em_utc, criado_por, atualizado_por
) VALUES
    ('61000000-0000-0000-0000-000000000001', '1f000000-0000-0000-0000-000000000000', 'admin', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed'),
    ('61000000-0000-0000-0000-000000000003', '1f000000-0000-0000-0000-000000000027', 'secretaria-paroquial', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed')
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- 4. RESERVAS (Locais e Ativos Patrimoniais)
-- ============================================================================
INSERT INTO calendario.reservas (
    id, tipo_item, item_id, data_hora_inicio, data_hora_fim, buffer_minutos,
    descricao, solicitante_id, pastoral_movimento_id, evento_id, grupo_recorrencia_id,
    status, aprovador_id, aprovado_em, motivo_cancelamento, cancelado_por_id,
    check_avarias, criado_em_utc, atualizado_em_utc, deleted_at, version
) VALUES
    -- 1. Reserva LOCAL confirmada vinculada a evento
    (
        '83000000-0000-0000-0000-000000000001', 'LOCAL', '00000000-0000-0000-0000-000000000002',
        '2026-09-15T08:00:00Z', '2026-09-15T18:00:00Z', 30,
        'Reserva do Salão Paroquial para Retiro de Catequistas',
        '10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-0000000000ee',
        '40000000-0000-0000-0000-000000000003', NULL,
        'CONFIRMADA', '10000000-0000-0000-0000-000000000001', '2026-06-01T10:00:00Z',
        NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, 0
    ),
    -- 2. Reserva LOCAL pendente de aprovação (avulsa)
    (
        '83000000-0000-0000-0000-000000000002', 'LOCAL', '00000000-0000-0000-0000-000000000003',
        '2026-09-20T19:00:00Z', '2026-09-20T21:00:00Z', 15,
        'Reunião com pais de catequizandos',
        '10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-0000000000ee',
        NULL, NULL,
        'PENDENTE', NULL, NULL,
        NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, 0
    ),
    -- 3. Reserva LOCAL cancelada
    (
        '83000000-0000-0000-0000-000000000003', 'LOCAL', '00000000-0000-0000-0000-000000000004',
        '2026-08-01T14:00:00Z', '2026-08-01T17:00:00Z', 30,
        'Oficina culinária beneficente',
        '10000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-0000000000bb',
        NULL, NULL,
        'CANCELADA', NULL, NULL,
        'Cancelado a pedido do coordenador devido a choque com cronograma escolar.',
        '10000000-0000-0000-0000-000000000004', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, 0
    ),
    -- 4. Reserva ATIVO retirado (em uso corrente)
    (
        '83000000-0000-0000-0000-000000000004', 'ATIVO', '80000000-0000-0000-0000-000000000009',
        '2026-09-01T08:00:00Z', '2026-09-30T18:00:00Z', 60,
        'Tenda para apoio à feira de artesanato paroquial',
        '10000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-0000000000bb',
        NULL, NULL,
        'RETIRADO', '10000000-0000-0000-0000-000000000001', '2026-08-25T14:00:00Z',
        NULL, NULL, 'Retirada em perfeito estado pela equipe de jovens.',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, 0
    ),
    -- 5. Reserva ATIVO confirmada aguardando retirada
    (
        '83000000-0000-0000-0000-000000000005', 'ATIVO', '80000000-0000-0000-0000-000000000010',
        '2026-09-12T08:00:00Z', '2026-09-12T12:00:00Z', 30,
        'Microfone sem fio para missa campal',
        '10000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-0000000000aa',
        NULL, NULL,
        'CONFIRMADA', '10000000-0000-0000-0000-000000000001', '2026-09-01T09:00:00Z',
        NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, 0
    ),
    -- 6. Reserva ATIVO finalizada com devolução e check de avarias
    (
        '83000000-0000-0000-0000-000000000006', 'ATIVO', '80000000-0000-0000-0000-000000000001',
        '2026-07-10T18:00:00Z', '2026-07-10T22:00:00Z', 30,
        'Projetor para exibição de filme bíblico',
        '10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-0000000000ee',
        NULL, NULL,
        'FINALIZADA', '10000000-0000-0000-0000-000000000001', '2026-07-05T11:00:00Z',
        NULL, NULL, 'Devolvido sem avarias, cabos completos e funcionais.',
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, 0
    ),
    -- 7. Reserva LOCAL de Dia Inteiro (buffer 0 embutido)
    (
        '83000000-0000-0000-0000-000000000011', 'LOCAL', '00000000-0000-0000-0000-000000000002',
        '2026-10-15T03:00:00Z', '2026-10-16T02:59:59Z', 0,
        'Reserva de Dia Inteiro do Salão Paroquial para Festa da Padroeira',
        '10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-0000000000cc',
        '40000000-0000-0000-0000-000000000401', NULL,
        'CONFIRMADA', '10000000-0000-0000-0000-000000000001', '2026-06-01T10:00:00Z',
        NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, 0
    ),
    -- 8. Reserva LOCAL Período da Manhã (Sala de Reunião 2 - buffer 0 embutido)
    (
        '83000000-0000-0000-0000-000000000012', 'LOCAL', '00000000-0000-0000-0000-000000000003',
        '2026-10-17T09:00:00Z', '2026-10-17T14:59:59Z', 0,
        'Reserva do Turno da Manhã para Círculo Bíblico',
        '10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-0000000000cc',
        '40000000-0000-0000-0000-000000000403', NULL,
        'CONFIRMADA', '10000000-0000-0000-0000-000000000001', '2026-06-01T10:00:00Z',
        NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, 0
    ),
    -- 9. Reserva LOCAL Período da Tarde (Mesma Sala de Reunião 2 sem conflito de turno - buffer 0)
    (
        '83000000-0000-0000-0000-000000000013', 'LOCAL', '00000000-0000-0000-0000-000000000003',
        '2026-10-17T15:00:00Z', '2026-10-17T20:59:59Z', 0,
        'Reserva do Turno da Tarde para Encontro Pastoral da Criança',
        '10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-0000000000ff',
        '40000000-0000-0000-0000-000000000404', NULL,
        'CONFIRMADA', '10000000-0000-0000-0000-000000000001', '2026-06-01T10:00:00Z',
        NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, 0
    ),
    -- 10. Reservas de Múltiplos Locais para Retiro Paroquial
    (
        '83000000-0000-0000-0000-000000000021', 'LOCAL', '00000000-0000-0000-0000-000000000002',
        '2026-11-14T08:00:00Z', '2026-11-15T18:00:00Z', 30,
        'Reserva automática do Salão Paroquial vinculada ao evento: Retiro Paroquial de Jovens e Lideranças',
        '10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-0000000000bb',
        '40000000-0000-0000-0000-000000000601', NULL,
        'CONFIRMADA', '10000000-0000-0000-0000-000000000001', '2026-09-01T10:00:00Z',
        NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, 0
    ),
    (
        '83000000-0000-0000-0000-000000000022', 'LOCAL', '00000000-0000-0000-0000-000000000001',
        '2026-11-14T08:00:00Z', '2026-11-15T18:00:00Z', 30,
        'Reserva automática da Igreja Matriz vinculada ao evento: Retiro Paroquial de Jovens e Lideranças',
        '10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-0000000000bb',
        '40000000-0000-0000-0000-000000000601', NULL,
        'CONFIRMADA', '10000000-0000-0000-0000-000000000001', '2026-09-01T10:00:00Z',
        NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, 0
    ),
    (
        '83000000-0000-0000-0000-000000000023', 'LOCAL', '00000000-0000-0000-0000-000000000003',
        '2026-11-14T08:00:00Z', '2026-11-15T18:00:00Z', 30,
        'Reserva automática da Sala de Catequese vinculada ao evento: Retiro Paroquial de Jovens e Lideranças',
        '10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-0000000000bb',
        '40000000-0000-0000-0000-000000000601', NULL,
        'CONFIRMADA', '10000000-0000-0000-0000-000000000001', '2026-09-01T10:00:00Z',
        NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, NULL, 0
    )
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- 5. AJUSTES CADASTRAIS (LGPD / Fiéis)
-- ============================================================================
INSERT INTO calendario.solicitacoes_ajuste_cadastral (
    id, fiel_id, usuario_solicitante_id, email, celular, telefone_fixo,
    endereco_logradouro, endereco_numero, endereco_complemento, endereco_bairro,
    endereco_cep, endereco_cidade, endereco_estado, status, justificativa_analise,
    analisado_por_usuario_id, analisado_em_utc, criado_em_utc
) VALUES
    -- 1. PENDENTE
    (
        '84000000-0000-0000-0000-000000000001',
        '1f000000-0000-0000-0000-000000000003',
        '10000000-0000-0000-0000-000000000003',
        'joao.santos.novo@example.com',
        '11988887766',
        '1133334455',
        'Rua Santo Agostinho',
        '125',
        'Apto 42',
        'Santo Agostinho',
        '08000-002',
        'São Paulo',
        'SP',
        'PENDENTE',
        NULL,
        NULL,
        NULL,
        CURRENT_TIMESTAMP
    ),
    -- 2. APROVADA
    (
        '84000000-0000-0000-0000-000000000002',
        '1f000000-0000-0000-0000-000000000002',
        '10000000-0000-0000-0000-000000000002',
        'maria.oliveira.atualizada@example.com',
        '11999990002',
        NULL,
        'Rua das Flores',
        '102',
        'Casa B',
        'Vila Curuçá',
        '08000-001',
        'São Paulo',
        'SP',
        'APROVADA',
        'Comprovante de residência conferido e validado pela secretaria.',
        '10000000-0000-0000-0000-000000000001',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    ),
    -- 3. REJEITADA
    (
        '84000000-0000-0000-0000-000000000003',
        '1f000000-0000-0000-0000-000000000004',
        '10000000-0000-0000-0000-000000000004',
        'pedro.invalido@example.com',
        '11000000000',
        NULL,
        'Rua Desconhecida',
        'S/N',
        NULL,
        'Bairro Inexistente',
        '00000-000',
        'São Paulo',
        'SP',
        'REJEITADA',
        'Dados de contato inconsistentes e CEP não localizado.',
        '10000000-0000-0000-0000-000000000001',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- 6. AJUSTES CLERICAIS (Clero)
-- ============================================================================
INSERT INTO calendario.solicitacoes_ajuste_clerical (
    id, usuario_solicitante_id, clerigo_id, email, celular, telefone_fixo,
    endereco, status, justificativa_analise, analisado_por_usuario_id,
    analisado_em_utc, criado_em_utc
) VALUES
    -- 1. PENDENTE
    (
        '85000000-0000-0000-0000-000000000001',
        '10000000-0000-0000-0000-000000000007',
        '1c000000-0000-0000-0000-000000000006',
        'padre.joao.novo@example.com',
        '11999998877',
        '1133338877',
        'Casa Paroquial, Rua da Matriz, 12, Centro, São Paulo - SP',
        'PENDENTE',
        NULL,
        NULL,
        NULL,
        CURRENT_TIMESTAMP
    ),
    -- 2. APROVADA
    (
        '85000000-0000-0000-0000-000000000002',
        '10000000-0000-0000-0000-000000000026',
        '1c000000-0000-0000-0000-000000000007',
        'diacono.francisco.atualizado@example.com',
        '11999990026',
        '1133332211',
        'Av. São José, 190, São José Allamano, São Paulo - SP',
        'APROVADA',
        'Atualização de domicílio confirmada com a Cúria Diocesana.',
        '10000000-0000-0000-0000-000000000001',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP
    )
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- 7. RELACIONAMENTOS FAMILIARES (Polimórficos Fiel / Clérigo)
-- ============================================================================
INSERT INTO calendario.relacionamentos_familiares (
    id, par_id, pessoa_origem_id, tipo_pessoa_origem,
    pessoa_destino_id, tipo_pessoa_destino, tipo_relacao,
    criado_em_utc, deleted_at
) VALUES
    -- Par 1: Cônjuges (Maria do Conselho <-> José do Conselho)
    (
        '87000000-0000-0000-0000-000000000001',
        '87000000-0000-0000-0000-000000000002',
        '1f000000-0000-0000-0000-000000000005', 'FIEL',
        '1f000000-0000-0000-0000-000000000007', 'FIEL',
        'CONJUGE', CURRENT_TIMESTAMP, NULL
    ),
    (
        '87000000-0000-0000-0000-000000000002',
        '87000000-0000-0000-0000-000000000001',
        '1f000000-0000-0000-0000-000000000007', 'FIEL',
        '1f000000-0000-0000-0000-000000000005', 'FIEL',
        'CONJUGE', CURRENT_TIMESTAMP, NULL
    ),
    -- Par 2: Irmãos (Maria Oliveira <-> Lúcia de Oliveira)
    (
        '87000000-0000-0000-0000-000000000003',
        '87000000-0000-0000-0000-000000000004',
        '1f000000-0000-0000-0000-000000000002', 'FIEL',
        '1f000000-0000-0000-0000-000000000009', 'FIEL',
        'IRMAO', CURRENT_TIMESTAMP, NULL
    ),
    (
        '87000000-0000-0000-0000-000000000004',
        '87000000-0000-0000-0000-000000000003',
        '1f000000-0000-0000-0000-000000000009', 'FIEL',
        '1f000000-0000-0000-0000-000000000002', 'FIEL',
        'IRMAO', CURRENT_TIMESTAMP, NULL
    ),
    -- Par 3: Relacionamento Fiel <-> Clérigo (Pedro Junior [fiel] filho do Pe. Pedro da Silva [clérigo])
    (
        '87000000-0000-0000-0000-000000000005',
        '87000000-0000-0000-0000-000000000006',
        '1f000000-0000-0000-0000-000000000004', 'FIEL',
        '1c000000-0000-0000-0000-000000000001', 'CLERIGO',
        'FILHO', CURRENT_TIMESTAMP, NULL
    ),
    (
        '87000000-0000-0000-0000-000000000006',
        '87000000-0000-0000-0000-000000000005',
        '1c000000-0000-0000-0000-000000000001', 'CLERIGO',
        '1f000000-0000-0000-0000-000000000004', 'FIEL',
        'GENITOR', CURRENT_TIMESTAMP, NULL
    )
ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- 8. NOTIFICAÇÕES (Central de Notificações)
-- ============================================================================
INSERT INTO calendario.notificacoes (
    id, usuario_id, tipo, mensagem, criado_em_utc, lida_em_utc
) VALUES
    -- 1. Notificação não lida (Aprovação pendente)
    (
        '88000000-0000-0000-0000-000000000001',
        '10000000-0000-0000-0000-000000000001',
        'SOLICITACAO_RESERVA',
        'Nova solicitação de reserva pendente para o Salão Paroquial.',
        CURRENT_TIMESTAMP,
        NULL
    ),
    -- 2. Notificação não lida (Ajuste cadastral)
    (
        '88000000-0000-0000-0000-000000000002',
        '10000000-0000-0000-0000-000000000001',
        'AJUSTE_CADASTRAL',
        'O fiel João Santos solicitou alteração cadastral.',
        CURRENT_TIMESTAMP,
        NULL
    ),
    -- 3. Notificação lida (Confirmação de evento)
    (
        '88000000-0000-0000-0000-000000000003',
        '10000000-0000-0000-0000-000000000002',
        'EVENTO_APROVADO',
        'Seu evento "Missa de Abertura da Catequese" foi aprovado pelo Pároco.',
        '2026-01-16T09:05:00Z',
        '2026-01-16T10:30:00Z'
    )
ON CONFLICT (id) DO NOTHING;


-- ============================================================================
-- SACRAMENTOS: assentos, participantes, averbações, ressalvas, notificações canônicas e histórico
-- Cenários cobertos:
--   01 Batismo homologado, vínculos tardios mistos (aprovados e participante sem cadastro), averbação
--      homologada + pendente e ressalva de retificação
--   02 Batismo homologado de filiação adotiva (dado sensível) com averbação reservada
--   03 Batismo em rascunho incompleto, com vínculo PENDENTE de aprovação do Pároco (fila de Aprovações)
--   04 Batismo cancelado por duplicidade (libera livro/folha/termo do assento 01)
--   05 Confirmação homologada com notificação canônica enviada
--   06 Confirmação em rascunho com vínculo PENDENTE
--   07 Matrimônio homologado com duas notificações canônicas
--   08 Matrimônio em livro RESERVADO (secreto), invisível à Secretaria
--   09 Ordem Sacra (diaconato permanente) homologada, ordenado vinculado ao clérigo
-- ============================================================================
INSERT INTO calendario.assentos_sacramentais (
    id, tipo, status, visibilidade, data_lavratura, lavrado_por, livro, folha, termo, paroquia_custodiante,
    data_celebracao, local_celebracao, dado_sensivel, dados_especificos, homologado_por, homologado_em_utc,
    motivo_cancelamento, cancelado_por, cancelado_em_utc,
    criado_em_utc, atualizado_em_utc, criado_por, atualizado_por, version
) VALUES
    ('5a000000-0000-0000-0000-000000000001', 'BATISMO', 'SEALED', 'COMUM', '2025-03-16', 'secretaria.paroquial', '1', '10', '100',
     'Paróquia Nossa Senhora de Fátima', '2025-03-15 10:00:00', 'Igreja Matriz', FALSE,
     '{"certidaoNascimentoCartorio": "1º Cartório de Registro Civil de São Paulo", "certidaoNascimentoTermo": "55120", "estadoFiliacao": "MATRIMONIAL"}'::jsonb,
     'padre.pedro', '2025-03-17 12:00:00+00', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'padre.pedro', 0),
    ('5a000000-0000-0000-0000-000000000002', 'BATISMO', 'SEALED', 'COMUM', '2025-05-11', 'secretaria.paroquial', '1', '10', '101',
     'Paróquia Nossa Senhora de Fátima', '2025-05-10 16:00:00', 'Capela Santo Agostinho', TRUE,
     '{"certidaoNascimentoCartorio": "2º Cartório de Registro Civil de São Paulo", "certidaoNascimentoTermo": "81234", "estadoFiliacao": "ADOTIVA"}'::jsonb,
     'padre.pedro', '2025-05-12 12:00:00+00', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'padre.pedro', 0),
    ('5a000000-0000-0000-0000-000000000003', 'BATISMO', 'DRAFT', 'COMUM', '2026-02-02', 'secretaria.paroquial', NULL, NULL, NULL,
     'Paróquia Nossa Senhora de Fátima', '2026-02-01 10:00:00', 'Igreja Matriz', FALSE,
     '{"estadoFiliacao": "NAO_CASADOS"}'::jsonb,
     NULL, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'secretaria.paroquial', 0),
    ('5a000000-0000-0000-0000-000000000004', 'BATISMO', 'CANCELADO', 'COMUM', '2025-03-16', 'secretaria.paroquial', '1', '10', '100',
     'Paróquia Nossa Senhora de Fátima', '2025-03-15 10:00:00', 'Igreja Matriz', FALSE,
     '{"certidaoNascimentoCartorio": "1º Cartório de Registro Civil de São Paulo", "certidaoNascimentoTermo": "55120", "estadoFiliacao": "MATRIMONIAL"}'::jsonb,
     'padre.pedro', '2025-03-16 18:00:00+00', 'Termo lavrado em duplicidade; prevalece o assento de livro 1, folha 10, termo 100.', 'padre.pedro',
     '2025-03-18 12:00:00+00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'padre.pedro', 0),
    ('5a000000-0000-0000-0000-000000000005', 'CONFIRMACAO', 'SEALED', 'COMUM', '2025-11-02', 'secretaria.paroquial', '2', '5', '20',
     'Paróquia Nossa Senhora de Fátima', '2025-11-01 19:00:00', 'Igreja Matriz', FALSE,
     '{"batismoParoquia": "Paróquia Santa Luzia", "batismoDiocese": "Diocese de São Miguel Paulista", "batismoLivro": "12", "batismoFolha": "3", "batismoTermo": "88"}'::jsonb,
     'padre.pedro', '2025-11-03 12:00:00+00', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'padre.pedro', 0),
    ('5a000000-0000-0000-0000-000000000006', 'CONFIRMACAO', 'DRAFT', 'COMUM', '2026-03-10', 'secretaria.paroquial', NULL, NULL, NULL,
     'Paróquia Nossa Senhora de Fátima', '2026-03-08 19:00:00', 'Igreja Matriz', FALSE,
     '{"batismoParoquia": "Paróquia Nossa Senhora de Fátima"}'::jsonb,
     NULL, NULL, NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'secretaria.paroquial', 0),
    ('5a000000-0000-0000-0000-000000000007', 'MATRIMONIO', 'SEALED', 'COMUM', '2025-09-21', 'secretaria.paroquial', '3', '1', '7',
     'Paróquia Nossa Senhora de Fátima', '2025-09-20 17:00:00', 'Igreja Matriz', FALSE,
     '{"numeroProcessoHabilitacao": "HAB-2025-031"}'::jsonb,
     'padre.pedro', '2025-09-22 12:00:00+00', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'padre.pedro', 0),
    ('5a000000-0000-0000-0000-000000000008', 'MATRIMONIO', 'SEALED', 'RESERVADO', '2025-10-05', 'padre.pedro', 'R1', '1', '1',
     'Paróquia Nossa Senhora de Fátima', '2025-10-04 08:00:00', 'Capela da Matriz', FALSE,
     '{"numeroProcessoHabilitacao": "HAB-RES-001"}'::jsonb,
     'padre.pedro', '2025-10-05 12:00:00+00', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'padre.pedro', 'padre.pedro', 0),
    ('5a000000-0000-0000-0000-000000000009', 'ORDEM', 'SEALED', 'COMUM', '2024-12-09', 'secretaria.paroquial', '4', '2', '3',
     'Paróquia Nossa Senhora de Fátima', '2024-12-08 10:00:00', 'Catedral Diocesana', FALSE,
     '{"grau": "DIACONATO_PERMANENTE", "incardinacao": "Diocese de São Miguel Paulista"}'::jsonb,
     'padre.pedro', '2024-12-10 12:00:00+00', NULL, NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'padre.pedro', 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO calendario.assento_participantes (
    id, assento_id, papel, nome_historico, documento, local_nascimento, catolico, crismado, idade_na_celebracao,
    condicao_canonica, fiel_id, clerigo_id, vinculo_pendente, criado_em_utc, atualizado_em_utc, criado_por, atualizado_por
) VALUES
    -- 01 Batismo: batizando sem cadastro (late binding) e demais vinculados
    ('5b000000-0000-0000-0000-000000000101', '5a000000-0000-0000-0000-000000000001', 'BATIZANDO', 'Sofia Oliveira', NULL, 'São Paulo/SP/BR', NULL, NULL, NULL, NULL, NULL, NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'secretaria.paroquial'),
    ('5b000000-0000-0000-0000-000000000102', '5a000000-0000-0000-0000-000000000001', 'MAE', 'Maria Oliveira', NULL, NULL, NULL, NULL, NULL, NULL, '1f000000-0000-0000-0000-000000000002', NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'padre.pedro'),
    ('5b000000-0000-0000-0000-000000000103', '5a000000-0000-0000-0000-000000000001', 'PAI', 'Carlos de Oliveira', NULL, NULL, NULL, NULL, NULL, NULL, '1f000000-0000-0000-0000-000000000010', NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'padre.pedro'),
    ('5b000000-0000-0000-0000-000000000104', '5a000000-0000-0000-0000-000000000001', 'PADRINHO', 'Pedro Junior', NULL, NULL, TRUE, TRUE, 30, NULL, '1f000000-0000-0000-0000-000000000004', NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'padre.pedro'),
    ('5b000000-0000-0000-0000-000000000105', '5a000000-0000-0000-0000-000000000001', 'MADRINHA', 'Lúcia de Oliveira', NULL, NULL, TRUE, TRUE, 35, NULL, '1f000000-0000-0000-0000-000000000009', NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'padre.pedro'),
    ('5b000000-0000-0000-0000-000000000106', '5a000000-0000-0000-0000-000000000001', 'MINISTRO_CELEBRANTE', 'Pe. Pedro da Silva', NULL, NULL, NULL, NULL, NULL, 'PRESBITERO', NULL, '1c000000-0000-0000-0000-000000000001', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'padre.pedro'),
    -- 02 Batismo adotivo (dado sensível): adotantes e padrinho vinculados, batizando sem cadastro
    ('5b000000-0000-0000-0000-000000000201', '5a000000-0000-0000-0000-000000000002', 'BATIZANDO', 'Miguel Acolhida', NULL, 'São Paulo/SP/BR', NULL, NULL, NULL, NULL, NULL, NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'secretaria.paroquial'),
    ('5b000000-0000-0000-0000-000000000202', '5a000000-0000-0000-0000-000000000002', 'ADOTANTE', 'Marta Acolhida São José', NULL, NULL, NULL, NULL, NULL, NULL, '1f000000-0000-0000-0000-000000000021', NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'padre.pedro'),
    ('5b000000-0000-0000-0000-000000000203', '5a000000-0000-0000-0000-000000000002', 'ADOTANTE', 'Paulo Dízimo São José', NULL, NULL, NULL, NULL, NULL, NULL, '1f000000-0000-0000-0000-000000000022', NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'padre.pedro'),
    ('5b000000-0000-0000-0000-000000000204', '5a000000-0000-0000-0000-000000000002', 'PADRINHO', 'Mateus Tesoureiro Santo Agostinho', NULL, NULL, TRUE, TRUE, 43, NULL, '1f000000-0000-0000-0000-000000000018', NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'padre.pedro'),
    ('5b000000-0000-0000-0000-000000000205', '5a000000-0000-0000-0000-000000000002', 'MINISTRO_CELEBRANTE', 'Pe. João Batista', NULL, NULL, NULL, NULL, NULL, 'PRESBITERO', NULL, '1c000000-0000-0000-0000-000000000006', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'padre.pedro'),
    -- 03 Batismo rascunho: batizando com vínculo PENDENTE (aguarda Aprovações), demais sem cadastro
    ('5b000000-0000-0000-0000-000000000301', '5a000000-0000-0000-0000-000000000003', 'BATIZANDO', 'Lucas Fiel São José', NULL, 'São Paulo/SP/BR', NULL, NULL, NULL, NULL, '1f000000-0000-0000-0000-000000000024', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'secretaria.paroquial'),
    ('5b000000-0000-0000-0000-000000000302', '5a000000-0000-0000-0000-000000000003', 'MAE', 'Rosa Fiel São José', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'secretaria.paroquial'),
    -- 04 Batismo cancelado (duplicidade)
    ('5b000000-0000-0000-0000-000000000401', '5a000000-0000-0000-0000-000000000004', 'BATIZANDO', 'Sofia Oliveira', NULL, 'São Paulo/SP/BR', NULL, NULL, NULL, NULL, NULL, NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'secretaria.paroquial'),
    ('5b000000-0000-0000-0000-000000000402', '5a000000-0000-0000-0000-000000000004', 'MAE', 'Maria Oliveira', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'secretaria.paroquial'),
    -- 05 Confirmação
    ('5b000000-0000-0000-0000-000000000501', '5a000000-0000-0000-0000-000000000005', 'CRISMANDO', 'Clara Fiel Santo Agostinho', NULL, 'São Paulo/SP/BR', NULL, NULL, NULL, NULL, '1f000000-0000-0000-0000-000000000025', NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'padre.pedro'),
    ('5b000000-0000-0000-0000-000000000502', '5a000000-0000-0000-0000-000000000005', 'PADRINHO', 'Carlos de Oliveira', NULL, NULL, TRUE, TRUE, 45, NULL, '1f000000-0000-0000-0000-000000000010', NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'padre.pedro'),
    ('5b000000-0000-0000-0000-000000000503', '5a000000-0000-0000-0000-000000000005', 'MINISTRO_CELEBRANTE', 'Pe. João Batista', NULL, NULL, NULL, NULL, NULL, 'PRESBITERO', NULL, '1c000000-0000-0000-0000-000000000006', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'padre.pedro'),
    -- 06 Confirmação rascunho: crismando com vínculo PENDENTE
    ('5b000000-0000-0000-0000-000000000601', '5a000000-0000-0000-0000-000000000006', 'CRISMANDO', 'Rafael Secretário São José', NULL, 'São Paulo/SP/BR', NULL, NULL, NULL, NULL, '1f000000-0000-0000-0000-000000000016', NULL, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'secretaria.paroquial'),
    ('5b000000-0000-0000-0000-000000000602', '5a000000-0000-0000-0000-000000000006', 'MINISTRO_CELEBRANTE', 'Pe. Pedro da Silva', NULL, NULL, NULL, NULL, NULL, 'PRESBITERO', NULL, NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'secretaria.paroquial'),
    -- 07 Matrimônio
    ('5b000000-0000-0000-0000-000000000701', '5a000000-0000-0000-0000-000000000007', 'CONJUGE', 'João Santos', NULL, 'São Paulo/SP/BR', NULL, NULL, NULL, NULL, '1f000000-0000-0000-0000-000000000003', NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'padre.pedro'),
    ('5b000000-0000-0000-0000-000000000702', '5a000000-0000-0000-0000-000000000007', 'CONJUGE', 'Lúcia de Oliveira', NULL, 'São Paulo/SP/BR', NULL, NULL, NULL, NULL, '1f000000-0000-0000-0000-000000000009', NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'padre.pedro'),
    ('5b000000-0000-0000-0000-000000000703', '5a000000-0000-0000-0000-000000000007', 'TESTEMUNHA', 'Marcos Tesoureiro São José', NULL, NULL, NULL, NULL, NULL, NULL, '1f000000-0000-0000-0000-000000000017', NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'padre.pedro'),
    ('5b000000-0000-0000-0000-000000000704', '5a000000-0000-0000-0000-000000000007', 'TESTEMUNHA', 'Tiago Pascom', NULL, NULL, NULL, NULL, NULL, NULL, '1f000000-0000-0000-0000-000000000019', NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'padre.pedro'),
    ('5b000000-0000-0000-0000-000000000705', '5a000000-0000-0000-0000-000000000007', 'MINISTRO_ASSISTENTE', 'Pe. Pedro da Silva', NULL, NULL, NULL, NULL, NULL, 'PRESBITERO', NULL, '1c000000-0000-0000-0000-000000000001', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'padre.pedro'),
    -- 08 Matrimônio reservado (secreto): nubentes e testemunhas sem cadastro; ministro vinculado
    ('5b000000-0000-0000-0000-000000000801', '5a000000-0000-0000-0000-000000000008', 'CONJUGE', 'Nubente Reservado A', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'padre.pedro', 'padre.pedro'),
    ('5b000000-0000-0000-0000-000000000802', '5a000000-0000-0000-0000-000000000008', 'CONJUGE', 'Nubente Reservado B', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'padre.pedro', 'padre.pedro'),
    ('5b000000-0000-0000-0000-000000000803', '5a000000-0000-0000-0000-000000000008', 'TESTEMUNHA', 'Testemunha Reservada 1', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'padre.pedro', 'padre.pedro'),
    ('5b000000-0000-0000-0000-000000000804', '5a000000-0000-0000-0000-000000000008', 'TESTEMUNHA', 'Testemunha Reservada 2', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'padre.pedro', 'padre.pedro'),
    ('5b000000-0000-0000-0000-000000000805', '5a000000-0000-0000-0000-000000000008', 'MINISTRO_ASSISTENTE', 'Pe. Pedro da Silva', NULL, NULL, NULL, NULL, NULL, 'PRESBITERO', NULL, '1c000000-0000-0000-0000-000000000001', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'padre.pedro', 'padre.pedro'),
    -- 09 Ordem
    ('5b000000-0000-0000-0000-000000000901', '5a000000-0000-0000-0000-000000000009', 'ORDENADO', 'Francisco de Assis Costa', NULL, 'São Paulo/SP/BR', NULL, NULL, NULL, 'DIACONO', NULL, '1c000000-0000-0000-0000-000000000007', FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'padre.pedro'),
    ('5b000000-0000-0000-0000-000000000902', '5a000000-0000-0000-0000-000000000009', 'BISPO_ORDENANTE', 'Dom Aparecido Ribeiro', NULL, NULL, NULL, NULL, NULL, 'BISPO', NULL, NULL, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'secretaria.paroquial')
ON CONFLICT (id) DO NOTHING;

INSERT INTO calendario.averbacoes_batismo (
    id, assento_id, tipo_evento, data_averbacao, numero_documento, teor_nota, reservada, inserida_por, status,
    homologada_por, homologada_em_utc, criado_em_utc, atualizado_em_utc, criado_por, atualizado_por
) VALUES
    ('5c000000-0000-0000-0000-000000000001', '5a000000-0000-0000-0000-000000000001', 'CONFIRMACAO', '2025-11-05', 'NOT-2025-014',
     'Confirmada em 01/11/2025 na Paróquia Nossa Senhora de Fátima, livro 2, folha 5, termo 20.', FALSE, 'secretaria.paroquial', 'HOMOLOGADA',
     'padre.pedro', '2025-11-06 12:00:00+00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'padre.pedro'),
    ('5c000000-0000-0000-0000-000000000002', '5a000000-0000-0000-0000-000000000001', 'MATRIMONIO', '2026-02-20', 'NOT-2026-003',
     'Matrimônio celebrado em 14/02/2026 na Paróquia São Judas Tadeu, conforme notificação recebida.', FALSE, 'secretaria.paroquial', 'PENDENTE_HOMOLOGACAO',
     NULL, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'secretaria.paroquial'),
    ('5c000000-0000-0000-0000-000000000003', '5a000000-0000-0000-0000-000000000002', 'ANOTACAO_DISCIPLINAR', '2025-06-01', 'DEC-2025-007',
     'Anotação reservada por determinação do Ordinário.', TRUE, 'padre.pedro', 'HOMOLOGADA',
     'padre.pedro', '2025-06-01 12:00:00+00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'padre.pedro', 'padre.pedro')
ON CONFLICT (id) DO NOTHING;

INSERT INTO calendario.assento_ressalvas (
    id, assento_id, campo, valor_original, valor_corrigido, justificativa, atestada_por, atestada_em_utc,
    criado_em_utc, atualizado_em_utc, criado_por, atualizado_por
) VALUES
    ('5d000000-0000-0000-0000-000000000001', '5a000000-0000-0000-0000-000000000001', 'localNascimento', 'São Paulo/SP/BR', 'Guarulhos/SP/BR',
     'Erro material de grafia apurado na certidão de nascimento apresentada.', 'padre.pedro', '2025-04-02 12:00:00+00',
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'padre.pedro', 'padre.pedro')
ON CONFLICT (id) DO NOTHING;

INSERT INTO calendario.assento_notificacoes (
    id, assento_id, destinatario_paroquia, emitida_em, enviada_em, registrada_por,
    criado_em_utc, atualizado_em_utc, criado_por, atualizado_por
) VALUES
    ('5e000000-0000-0000-0000-000000000001', '5a000000-0000-0000-0000-000000000005', 'Paróquia Santa Luzia', '2025-11-02', '2025-11-05', 'secretaria.paroquial', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'secretaria.paroquial'),
    ('5e000000-0000-0000-0000-000000000002', '5a000000-0000-0000-0000-000000000007', 'Paróquia Santa Luzia', '2025-09-21', '2025-09-24', 'secretaria.paroquial', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'secretaria.paroquial'),
    ('5e000000-0000-0000-0000-000000000003', '5a000000-0000-0000-0000-000000000007', 'Paróquia São Judas Tadeu', '2025-09-21', NULL, 'secretaria.paroquial', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'secretaria.paroquial'),
    ('5e000000-0000-0000-0000-000000000004', '5a000000-0000-0000-0000-000000000009', 'Paróquia Santa Luzia', '2024-12-09', '2024-12-12', 'secretaria.paroquial', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'secretaria.paroquial')
ON CONFLICT (id) DO NOTHING;

INSERT INTO calendario.assento_historico (
    id, assento_id, acao, usuario_id, ator, perfil, endereco_ip, valores_anteriores, valores_posteriores, ocorrido_em_utc,
    criado_em_utc, atualizado_em_utc, criado_por, atualizado_por
) VALUES
    ('5f000000-0000-0000-0000-000000000001', '5a000000-0000-0000-0000-000000000001', 'lavrar', '10000000-0000-0000-0000-000000000027', 'secretaria.paroquial', 'secretaria-paroquial', '10.0.0.15',
     NULL, '{"status":"DRAFT"}', '2025-03-16 14:00:00+00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'secretaria.paroquial'),
    ('5f000000-0000-0000-0000-000000000002', '5a000000-0000-0000-0000-000000000001', 'homologar', '10000000-0000-0000-0000-000000000001', 'padre.pedro', 'paroco', '10.0.0.7',
     '{"status":"DRAFT"}', '{"status":"SEALED","homologadoPor":"padre.pedro"}', '2025-03-17 12:00:00+00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'padre.pedro', 'padre.pedro'),
    ('5f000000-0000-0000-0000-000000000003', '5a000000-0000-0000-0000-000000000001', 'retificar', '10000000-0000-0000-0000-000000000001', 'padre.pedro', 'paroco', '10.0.0.7',
     '{"ressalvas":[]}', '{"ressalvas":[{"campo":"localNascimento"}]}', '2025-04-02 12:00:00+00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'padre.pedro', 'padre.pedro'),
    ('5f000000-0000-0000-0000-000000000004', '5a000000-0000-0000-0000-000000000003', 'lavrar', '10000000-0000-0000-0000-000000000027', 'secretaria.paroquial', 'secretaria-paroquial', '10.0.0.15',
     NULL, '{"status":"DRAFT","vinculoPendente":true}', '2026-02-02 14:00:00+00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'secretaria.paroquial', 'secretaria.paroquial'),
    ('5f000000-0000-0000-0000-000000000005', '5a000000-0000-0000-0000-000000000004', 'cancelar', '10000000-0000-0000-0000-000000000001', 'padre.pedro', 'paroco', '10.0.0.7',
     '{"status":"SEALED"}', '{"status":"CANCELADO"}', '2025-03-18 12:00:00+00', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'padre.pedro', 'padre.pedro')
ON CONFLICT (id) DO NOTHING;

-- Notificações aos Párocos pelos vínculos propostos pela Secretaria (assentos 03 e 06)
INSERT INTO calendario.notificacoes (id, usuario_id, tipo, mensagem, criado_em_utc, lida_em_utc)
VALUES
    ('88000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000001', 'SACRAMENTO_VINCULO_PENDENTE',
     '1 vínculo(s) de fiel/clérigo em assento de BATISMO proposto(s) por ''secretaria.paroquial'' aguardam aprovação do Pároco.', CURRENT_TIMESTAMP, NULL),
    ('88000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000001', 'SACRAMENTO_VINCULO_PENDENTE',
     '1 vínculo(s) de fiel/clérigo em assento de CONFIRMACAO proposto(s) por ''secretaria.paroquial'' aguardam aprovação do Pároco.', CURRENT_TIMESTAMP, NULL)
ON CONFLICT (id) DO NOTHING;

-- Mandatos de coordenação de exemplo: espelha os coordenadores/vices semeados como mandatos homologados (2 anos).
INSERT INTO calendario.mandato_coordenacao (id, organizacao_id, fiel_id, funcao, inicio, fim, status, homologado_por, decidido_em_utc)
SELECT gen_random_uuid(), m.organizacao_id, m.fiel_id,
       CASE WHEN m.papel = 'coordenador' THEN 'COORDENADOR' ELSE 'VICE_COORDENADOR' END,
       CURRENT_DATE, CURRENT_DATE + INTERVAL '2 years', 'HOMOLOGADO', 'seed', now()
FROM calendario.membro_grupo m
WHERE m.ativo AND m.papel IN ('coordenador', 'secretario-grupo')
  AND NOT EXISTS (SELECT 1 FROM calendario.mandato_coordenacao c
                  WHERE c.organizacao_id = m.organizacao_id AND c.fiel_id = m.fiel_id);

-- Idoneidade de exemplo para os coordenadores semeados (dados fictícios de desenvolvimento).
UPDATE calendario.membro_grupo
SET formacao_concluida_em = DATE '2025-01-01', vivencia_sacramental_atestada_em = DATE '2025-01-01',
    termo_protecao_menores_em = DATE '2025-01-01', enviado_em = DATE '2025-01-01', enviado_por = 'seed'
WHERE ativo AND papel IN ('coordenador', 'secretario-grupo') AND formacao_concluida_em IS NULL;

-- =====================================================================================================
-- Módulo Organização: movimentos, serviços paroquiais, mandatos em todos os estados e idoneidade mista
-- (dados fictícios de desenvolvimento).
-- =====================================================================================================

-- Catálogo: movimento SEM estatuto reconhecido, para demonstrar o bloqueio de instanciação (Cân. 299 e 304).
INSERT INTO calendario.pastorais_movimentos_oficiais (id, nome, tipo, version)
VALUES ('50000000-0000-0000-0000-000000000199', 'Movimento em Processo de Reconhecimento', 'MOVIMENTO', 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO calendario.organizacao (id, nome, tipo, contato, ativo, parent_id, pastoral_oficial_id, setor_pastoral, version)
VALUES
    -- Movimentos apostólicos (estatuto reconhecido no catálogo)
    ('00000000-0000-0000-0000-0000000004a1', 'Encontro de Casais com Cristo (ECC) - Matriz', 'MOVIMENTO', 'ecc@nsfatima.com', TRUE, '00000000-0000-0000-0000-0000000000cc', '50000000-0000-0000-0000-000000000105', 'FORMACAO_ENSINAR', 0),
    ('00000000-0000-0000-0000-0000000004a2', 'Renovação Carismática (RCC) - São José Allamano', 'MOVIMENTO', 'rcc.saojose@nsfatima.com', TRUE, '00000000-0000-0000-0000-0000000002c0', '50000000-0000-0000-0000-000000000101', 'SANTIFICACAO_LITURGIA', 0),
    ('00000000-0000-0000-0000-0000000004a3', 'Terço dos Homens - Matriz', 'MOVIMENTO', 'terco@nsfatima.com', TRUE, '00000000-0000-0000-0000-0000000000cc', '50000000-0000-0000-0000-000000000109', 'SANTIFICACAO_LITURGIA', 0),
    -- Serviços paroquiais (articulados ao conselho pastoral)
    ('00000000-0000-0000-0000-0000000004b1', 'Coroinhas e Acólitos', 'SERVICO_PAROQUIAL', 'coroinhas@nsfatima.com', TRUE, '00000000-0000-0000-0000-0000000000cc', NULL, 'SANTIFICACAO_LITURGIA', 0),
    ('00000000-0000-0000-0000-0000000004b2', 'Ministros Extraordinários da Sagrada Comunhão', 'SERVICO_PAROQUIAL', 'mesc@nsfatima.com', TRUE, '00000000-0000-0000-0000-0000000000cc', NULL, 'SANTIFICACAO_LITURGIA', 0),
    ('00000000-0000-0000-0000-0000000004b3', 'Acolhida e Cerimonial - Santo Agostinho', 'SERVICO_PAROQUIAL', 'acolhida.sa@nsfatima.com', TRUE, '00000000-0000-0000-0000-0000000003c0', NULL, 'SOCIAL_CARIDADE', 0),
    -- Organismo inativo (histórico)
    ('00000000-0000-0000-0000-0000000004c1', 'Pastoral do Menor (encerrada)', 'PASTORAL', NULL, FALSE, '00000000-0000-0000-0000-0000000000cc', '50000000-0000-0000-0000-000000000001', 'SOCIAL_CARIDADE', 0)
ON CONFLICT (id) DO NOTHING;

INSERT INTO calendario.membro_grupo (id, fiel_id, organizacao_id, tipo_organizacao, papel, igreja_vinculo_id, ativo, criado_em_utc, atualizado_em_utc)
VALUES
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-0000000004a1', 'MOVIMENTO', 'coordenador', '00000000-0000-0000-0000-000000000100', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-0000000004a1', 'MOVIMENTO', 'membro-ativo', '00000000-0000-0000-0000-000000000100', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000024', '00000000-0000-0000-0000-0000000004a1', 'MOVIMENTO', 'voluntario', '00000000-0000-0000-0000-000000000100', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000024', '00000000-0000-0000-0000-0000000004a2', 'MOVIMENTO', 'membro-ativo', '00000000-0000-0000-0000-000000000200', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000015', '00000000-0000-0000-0000-0000000004a2', 'MOVIMENTO', 'membro-ativo', '00000000-0000-0000-0000-000000000200', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000009', '00000000-0000-0000-0000-0000000004a3', 'MOVIMENTO', 'membro-ativo', '00000000-0000-0000-0000-000000000100', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-0000000004a3', 'MOVIMENTO', 'voluntario', '00000000-0000-0000-0000-000000000100', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-0000000004b1', 'SERVICO_PAROQUIAL', 'coordenador', '00000000-0000-0000-0000-000000000100', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-0000000004b1', 'SERVICO_PAROQUIAL', 'voluntario', '00000000-0000-0000-0000-000000000100', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000012', '00000000-0000-0000-0000-0000000004b2', 'SERVICO_PAROQUIAL', 'coordenador', '00000000-0000-0000-0000-000000000100', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000013', '00000000-0000-0000-0000-0000000004b2', 'SERVICO_PAROQUIAL', 'membro-ativo', '00000000-0000-0000-0000-000000000100', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000025', '00000000-0000-0000-0000-0000000004b3', 'SERVICO_PAROQUIAL', 'coordenador', '00000000-0000-0000-0000-000000000300', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (gen_random_uuid(), '1f000000-0000-0000-0000-000000000014', '00000000-0000-0000-0000-0000000004b3', 'SERVICO_PAROQUIAL', 'voluntario', '00000000-0000-0000-0000-000000000300', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Idoneidade mista: completa (aptos), sem termo de menores (Coroinhas), sem envio (MESC) e vazia (demais).
UPDATE calendario.membro_grupo
SET formacao_concluida_em = DATE '2025-03-01', vivencia_sacramental_atestada_em = DATE '2025-03-01',
    termo_protecao_menores_em = DATE '2025-03-01', termo_protecao_menores_valido_ate = DATE '2028-03-01',
    enviado_em = DATE '2025-03-10', enviado_por = 'seed'
WHERE organizacao_id IN ('00000000-0000-0000-0000-0000000004a1', '00000000-0000-0000-0000-0000000004a2',
                         '00000000-0000-0000-0000-0000000004b3')
  AND papel IN ('coordenador', 'membro-ativo');
UPDATE calendario.membro_grupo
SET formacao_concluida_em = DATE '2025-03-01', vivencia_sacramental_atestada_em = DATE '2025-03-01',
    enviado_em = DATE '2025-03-10', enviado_por = 'seed'
WHERE organizacao_id = '00000000-0000-0000-0000-0000000004b1' AND papel = 'coordenador';
UPDATE calendario.membro_grupo
SET formacao_concluida_em = DATE '2025-03-01', vivencia_sacramental_atestada_em = DATE '2025-03-01',
    termo_protecao_menores_em = DATE '2024-03-01', termo_protecao_menores_valido_ate = DATE '2025-03-01'
WHERE organizacao_id = '00000000-0000-0000-0000-0000000004b2' AND papel = 'coordenador';

-- Mandatos em todos os estados: ECC (coordenador homologado, vice proposto), RCC (proposta vetada e mandato
-- anterior encerrado), Terço (proposta pendente), Coroinhas (homologado legado, com pendência de idoneidade).
INSERT INTO calendario.mandato_coordenacao (id, organizacao_id, fiel_id, funcao, inicio, fim, status, homologado_por, motivo_veto, decidido_em_utc)
VALUES
    (gen_random_uuid(), '00000000-0000-0000-0000-0000000004a1', '1f000000-0000-0000-0000-000000000010', 'COORDENADOR', CURRENT_DATE - INTERVAL '3 months', CURRENT_DATE + INTERVAL '21 months', 'HOMOLOGADO', 'seed', NULL, now()),
    (gen_random_uuid(), '00000000-0000-0000-0000-0000000004a1', '1f000000-0000-0000-0000-000000000011', 'VICE_COORDENADOR', CURRENT_DATE, CURRENT_DATE + INTERVAL '18 months', 'PROPOSTO', NULL, NULL, NULL),
    (gen_random_uuid(), '00000000-0000-0000-0000-0000000004a2', '1f000000-0000-0000-0000-000000000024', 'COORDENADOR', CURRENT_DATE, CURRENT_DATE + INTERVAL '2 years', 'VETADO', 'seed', 'Formação doutrinária básica ainda não concluída.', now()),
    (gen_random_uuid(), '00000000-0000-0000-0000-0000000004a2', '1f000000-0000-0000-0000-000000000015', 'COORDENADOR', DATE '2022-01-01', DATE '2024-12-31', 'ENCERRADO', 'seed', NULL, now()),
    (gen_random_uuid(), '00000000-0000-0000-0000-0000000004a3', '1f000000-0000-0000-0000-000000000009', 'COORDENADOR', CURRENT_DATE, CURRENT_DATE + INTERVAL '2 years', 'PROPOSTO', NULL, NULL, NULL),
    (gen_random_uuid(), '00000000-0000-0000-0000-0000000004b1', '1f000000-0000-0000-0000-000000000003', 'COORDENADOR', CURRENT_DATE - INTERVAL '1 year', CURRENT_DATE + INTERVAL '1 year', 'HOMOLOGADO', 'seed', NULL, now()),
    (gen_random_uuid(), '00000000-0000-0000-0000-0000000004b3', '1f000000-0000-0000-0000-000000000025', 'COORDENADOR', CURRENT_DATE - INTERVAL '6 months', CURRENT_DATE + INTERVAL '30 months', 'HOMOLOGADO', 'seed', NULL, now());

-- Espelho de leitura: nome do fiel com mandato homologado na coordenação.
UPDATE calendario.organizacao o
SET coordenador = f.nome
FROM calendario.mandato_coordenacao m
JOIN calendario.fieis f ON f.id = m.fiel_id
WHERE m.organizacao_id = o.id AND m.status = 'HOMOLOGADO' AND m.funcao = 'COORDENADOR'
  AND o.id IN ('00000000-0000-0000-0000-0000000004a1', '00000000-0000-0000-0000-0000000004b1',
               '00000000-0000-0000-0000-0000000004b3');

-- Contato institucional separado: e-mail (a partir do contato semeado) e telefone fictício por organização.
UPDATE calendario.organizacao SET email = contato WHERE contato LIKE '%@%' AND email IS NULL;
UPDATE calendario.organizacao
SET telefone = '(11) 9' || LPAD((ABS(('x' || SUBSTR(MD5(id::text), 1, 7))::bit(28)::int) % 10000)::text, 4, '0') || '-' ||
               LPAD((ABS(('x' || SUBSTR(MD5(nome), 1, 7))::bit(28)::int) % 10000)::text, 4, '0')
WHERE telefone IS NULL AND tipo <> 'CONSELHO';
UPDATE calendario.organizacao SET contato = COALESCE(email, telefone);
