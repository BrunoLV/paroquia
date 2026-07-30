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

-- Organizations (Organizacoes)
INSERT INTO calendario.organizacoes (id, nome, tipo, contato, coordenador, vice_coordenador, ativo, parent_id, pastoral_oficial_id, version)
VALUES 
    -- Raízes (parent = NULL)
    ('00000000-0000-0000-0000-000000000001', 'Paróquia Nossa Senhora de Fátima', 'CLERO', 'contato@nsfatima.com', 'Pedro da Silva (Padre)', 'João Batista (Vigário)', TRUE, NULL, NULL, 0),
    ('00000000-0000-0000-0000-0000000000dd', 'Clero Paroquial', 'CLERO', 'clero@nsfatima.com', 'Pedro da Silva (Padre)', 'João Batista (Vigário)', TRUE, NULL, NULL, 0),
    -- Filhos do Clero Paroquial
    ('00000000-0000-0000-0000-000000000100', 'Igreja Matriz Nossa Senhora de Fátima', 'MATRIZ', 'matriz@nsfatima.com', NULL, NULL, TRUE, '00000000-0000-0000-0000-0000000000dd', NULL, 0),
    ('00000000-0000-0000-0000-000000000200', 'Comunidade São José Allamano', 'COMUNIDADE', 'saojose@nsfatima.com', NULL, NULL, TRUE, '00000000-0000-0000-0000-0000000000dd', NULL, 0),
    ('00000000-0000-0000-0000-000000000300', 'Comunidade Santo Agostinho', 'COMUNIDADE', 'santoagostinho@nsfatima.com', NULL, NULL, TRUE, '00000000-0000-0000-0000-0000000000dd', NULL, 0),
    -- Conselhos Paroquiais (filhos da Igreja Matriz)
    ('00000000-0000-0000-0000-0000000000cc', 'Conselho Pastoral Paroquial', 'CONSELHO_PASTORAL', 'cpp@nsfatima.com', 'Maria do Conselho', 'José do Conselho', TRUE, '00000000-0000-0000-0000-000000000100', NULL, 0),
    ('00000000-0000-0000-0000-000000000099', 'Conselho Econômico Paroquial', 'CONSELHO_ECONOMICO', 'economico@nsfatima.com', 'Pedro Finanças', 'Ana Administração', TRUE, '00000000-0000-0000-0000-000000000100', NULL, 0),
    -- Conselhos Comunitários (filhos das Comunidades)
    ('00000000-0000-0000-0000-0000000002c0', 'Conselho Pastoral Comunitário - São José Allamano', 'CONSELHO_PASTORAL', 'cpc.saojose@nsfatima.com', NULL, NULL, TRUE, '00000000-0000-0000-0000-000000000200', NULL, 0),
    ('00000000-0000-0000-0000-0000000002e0', 'Conselho Econômico Comunitário - São José Allamano', 'CONSELHO_ECONOMICO', 'cec.saojose@nsfatima.com', NULL, NULL, TRUE, '00000000-0000-0000-0000-000000000200', NULL, 0),
    ('00000000-0000-0000-0000-0000000003c0', 'Conselho Pastoral Comunitário - Santo Agostinho', 'CONSELHO_PASTORAL', 'cpc.santoagostinho@nsfatima.com', NULL, NULL, TRUE, '00000000-0000-0000-0000-000000000300', NULL, 0),
    ('00000000-0000-0000-0000-0000000003e0', 'Conselho Econômico Comunitário - Santo Agostinho', 'CONSELHO_ECONOMICO', 'cec.santoagostinho@nsfatima.com', NULL, NULL, TRUE, '00000000-0000-0000-0000-000000000300', NULL, 0),
    -- Pastorais Paroquiais (filhas do CPP)
    ('00000000-0000-0000-0000-0000000000aa', 'Pastoral da Comunicação', 'PASTORAL', 'pascom@nsfatima.com', NULL, NULL, TRUE, '00000000-0000-0000-0000-0000000000cc', '50000000-0000-0000-0000-000000000014', 0),
    ('00000000-0000-0000-0000-0000000000bb', 'Pastoral da Juventude', 'PASTORAL', 'juventude@nsfatima.com', 'Pedro Junior', 'Lucas dos Santos', TRUE, '00000000-0000-0000-0000-0000000000cc', '50000000-0000-0000-0000-000000000012', 0),
    ('00000000-0000-0000-0000-0000000000ee', 'Pastoral da Catequese', 'PASTORAL', 'catequese@nsfatima.com', 'Maria Oliveira', 'Lúcia de Oliveira', TRUE, '00000000-0000-0000-0000-0000000000cc', '50000000-0000-0000-0000-000000000021', 0),
    ('00000000-0000-0000-0000-0000000000ff', 'Pastoral do Batismo', 'PASTORAL', 'batismo@nsfatima.com', NULL, NULL, TRUE, '00000000-0000-0000-0000-0000000000cc', '50000000-0000-0000-0000-000000000020', 0),
    -- Pastorais Comunitárias (filhas dos CPCs)
    ('00000000-0000-0000-0000-0000000002a1', 'Pastoral da Acolhida - São José Allamano', 'PASTORAL', 'acolhida.saojose@nsfatima.com', NULL, NULL, TRUE, '00000000-0000-0000-0000-0000000002c0', '50000000-0000-0000-0000-000000000022', 0),
    ('00000000-0000-0000-0000-0000000002d1', 'Pastoral do Dízimo - São José Allamano', 'PASTORAL', 'dizimo.saojose@nsfatima.com', NULL, NULL, TRUE, '00000000-0000-0000-0000-0000000002c0', '50000000-0000-0000-0000-000000000025', 0),
    ('00000000-0000-0000-0000-0000000003a1', 'Pastoral da Liturgia - Santo Agostinho', 'PASTORAL', 'liturgia.santoagostinho@nsfatima.com', NULL, NULL, TRUE, '00000000-0000-0000-0000-0000000003c0', '50000000-0000-0000-0000-000000000018', 0),
    ('00000000-0000-0000-0000-0000000003b1', 'Pastoral Social - Santo Agostinho', 'PASTORAL', 'social.santoagostinho@nsfatima.com', NULL, NULL, TRUE, '00000000-0000-0000-0000-0000000003c0', '50000000-0000-0000-0000-000000000028', 0)
ON CONFLICT (id) DO NOTHING;

-- Locations (Locais)
INSERT INTO calendario.locais (id, nome, tipo, capacidade, ativo, organizacao_id, version)
VALUES 
    ('00000000-0000-0000-0000-000000000001', 'Igreja Principal', 'IGREJA', 500, TRUE, '00000000-0000-0000-0000-000000000100', 0),
    ('00000000-0000-0000-0000-000000000002', 'Salão Paroquial', 'SALAO', 300, TRUE, '00000000-0000-0000-0000-000000000100', 0),
    ('00000000-0000-0000-0000-000000000003', 'Sala de Catequese 1', 'SALA', 30, TRUE, '00000000-0000-0000-0000-000000000100', 0),
    ('00000000-0000-0000-0000-000000000004', 'Cozinha Paroquial', 'SALA', 10, TRUE, '00000000-0000-0000-0000-000000000100', 0)
ON CONFLICT (id) DO NOTHING;


-- Users (Usuarios)
INSERT INTO calendario.usuarios (id, username, password_hash, enabled, roles, igreja_id, nome, telefone, batizado, primeira_comunhao, crismado, casado_igreja_catolica, version)
VALUES 
    ('10000000-0000-0000-0000-000000000001', 'padre.pedro', '{noop}senha123', TRUE, 'ADMIN,PAROCO', '00000000-0000-0000-0000-000000000100', 'Pedro da Silva (Padre)', '11999990001', TRUE, TRUE, TRUE, FALSE, 0),
    ('10000000-0000-0000-0000-000000000002', 'maria.catequese', '{noop}senha123', TRUE, 'COORDENADOR_PASTORAL_GLOBAL', '00000000-0000-0000-0000-000000000100', 'Maria Oliveira', '11999990002', TRUE, TRUE, TRUE, TRUE, 0),
    ('10000000-0000-0000-0000-000000000003', 'joao.liturgia', '{noop}senha123', TRUE, 'VOLUNTARIO_PASTORAL_LOCAL', '00000000-0000-0000-0000-000000000300', 'João Santos', '11999990003', TRUE, TRUE, TRUE, FALSE, 0),
    ('10000000-0000-0000-0000-000000000004', 'pedro.liturgia', '{noop}senha123', TRUE, 'COORDENADOR_PASTORAL_LOCAL', '00000000-0000-0000-0000-000000000100', 'Pedro Junior', '11999990004', TRUE, TRUE, FALSE, FALSE, 0),
    ('10000000-0000-0000-0000-000000000005', 'maria.conselho', '{noop}senha123', TRUE, 'MESA_DIRETORA_CPP', '00000000-0000-0000-0000-000000000100', 'Maria do Conselho', '11999990005', TRUE, TRUE, TRUE, TRUE, 0),
    ('10000000-0000-0000-0000-000000000007', 'padre.joao', '{noop}senha123', TRUE, 'VIGARIO', '00000000-0000-0000-0000-000000000100', 'João Batista (Vigário)', '11999990007', TRUE, TRUE, TRUE, FALSE, 0),
    ('10000000-0000-0000-0000-000000000008', 'jose.conselho', '{noop}senha123', TRUE, 'MEMBRO_CPP', '00000000-0000-0000-0000-000000000100', 'José do Conselho', '11999990008', TRUE, TRUE, TRUE, TRUE, 0),
    ('10000000-0000-0000-0000-000000000009', 'ana.conselho', '{noop}senha123', TRUE, 'MEMBRO_CPP', '00000000-0000-0000-0000-000000000100', 'Ana do Conselho', '11999990009', TRUE, TRUE, TRUE, TRUE, 0),
    ('10000000-0000-0000-0000-000000000010', 'lucia.catequese', '{noop}senha123', TRUE, 'COORDENADOR_PASTORAL_LOCAL', '00000000-0000-0000-0000-000000000100', 'Lúcia de Oliveira', '11999990010', TRUE, TRUE, TRUE, FALSE, 0),
    ('10000000-0000-0000-0000-000000000011', 'carlos.catequese', '{noop}senha123', TRUE, 'VOLUNTARIO_PASTORAL_LOCAL', '00000000-0000-0000-0000-000000000100', 'Carlos de Oliveira', '11999990011', TRUE, TRUE, FALSE, FALSE, 0),
    ('10000000-0000-0000-0000-000000000012', 'lucas.juventude', '{noop}senha123', TRUE, 'COORDENADOR_PASTORAL_LOCAL', '00000000-0000-0000-0000-000000000100', 'Lucas dos Santos', '11999990012', TRUE, TRUE, TRUE, FALSE, 0),
    ('10000000-0000-0000-0000-000000000091', 'pedro.financas', '{noop}senha123', TRUE, 'MEMBRO_CAE', '00000000-0000-0000-0000-000000000100', 'Pedro Finanças', '11999990091', TRUE, TRUE, TRUE, TRUE, 0),
    ('10000000-0000-0000-0000-000000000092', 'ana.administracao', '{noop}senha123', TRUE, 'MEMBRO_CAE', '00000000-0000-0000-0000-000000000100', 'Ana Administração', '11999990092', TRUE, TRUE, TRUE, TRUE, 0)
ON CONFLICT (id) DO NOTHING;


-- Members (Membros)
INSERT INTO calendario.membros (id, usuario_id, organizacao_id, papel, ativo, version)
VALUES
    ('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-0000000000dd', 'paroco', TRUE, 0),
    ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-0000000000ee', 'coordenador-pastoral-global', TRUE, 0),
    ('20000000-0000-0000-0000-000000000003', '10000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-0000000003a1', 'voluntario-pastoral-local', TRUE, 0),
    ('20000000-0000-0000-0000-000000000004', '10000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-0000000000bb', 'coordenador-pastoral-local', TRUE, 0),
    ('20000000-0000-0000-0000-000000000005', '10000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-0000000000cc', 'mesa-diretora-cpp', TRUE, 0),
    ('20000000-0000-0000-0000-000000000007', '10000000-0000-0000-0000-000000000007', '00000000-0000-0000-0000-0000000000dd', 'vigario', TRUE, 0),
    ('20000000-0000-0000-0000-000000000008', '10000000-0000-0000-0000-000000000008', '00000000-0000-0000-0000-0000000000cc', 'membro-cpp', TRUE, 0),
    ('20000000-0000-0000-0000-000000000009', '10000000-0000-0000-0000-000000000009', '00000000-0000-0000-0000-0000000000cc', 'membro-cpp', TRUE, 0),
    ('20000000-0000-0000-0000-000000000010', '10000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-0000000000ee', 'coordenador-pastoral-local', TRUE, 0),
    ('20000000-0000-0000-0000-000000000011', '10000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-0000000000ee', 'voluntario-pastoral-local', TRUE, 0),
    ('20000000-0000-0000-0000-000000000012', '10000000-0000-0000-0000-000000000012', '00000000-0000-0000-0000-0000000000bb', 'coordenador-pastoral-local', TRUE, 0),
    ('20000000-0000-0000-0000-000000000091', '10000000-0000-0000-0000-000000000091', '00000000-0000-0000-0000-000000000099', 'membro-cae', TRUE, 0),
    ('20000000-0000-0000-0000-000000000092', '10000000-0000-0000-0000-000000000092', '00000000-0000-0000-0000-000000000099', 'membro-cae', TRUE, 0)
ON CONFLICT (id) DO NOTHING;

-- Organizational Memberships
INSERT INTO calendario.membros_organizacao (id, usuario_id, organizacao_id, tipo_organizacao, papel, igreja_vinculo_id, ativo, version)
VALUES
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-0000000000dd', 'CLERO', 'paroco', NULL, TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000007', '00000000-0000-0000-0000-0000000000dd', 'CLERO', 'vigario', NULL, TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-0000000000cc', 'CONSELHO_PASTORAL', 'mesa-diretora-cpp', '00000000-0000-0000-0000-000000000100', TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000008', '00000000-0000-0000-0000-0000000000cc', 'CONSELHO_PASTORAL', 'membro-cpp', '00000000-0000-0000-0000-000000000100', TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000009', '00000000-0000-0000-0000-0000000000cc', 'CONSELHO_PASTORAL', 'membro-cpp', '00000000-0000-0000-0000-000000000100', TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-0000000000ee', 'PASTORAL', 'coordenador-pastoral-global', '00000000-0000-0000-0000-000000000100', TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-0000000000ee', 'PASTORAL', 'coordenador-pastoral-local', '00000000-0000-0000-0000-000000000100', TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-0000000000ee', 'PASTORAL', 'voluntario-pastoral-local', '00000000-0000-0000-0000-000000000100', TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-0000000000bb', 'PASTORAL', 'coordenador-pastoral-local', '00000000-0000-0000-0000-000000000100', TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000012', '00000000-0000-0000-0000-0000000000bb', 'PASTORAL', 'coordenador-pastoral-local', '00000000-0000-0000-0000-000000000100', TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-0000000003a1', 'PASTORAL', 'voluntario-pastoral-local', '00000000-0000-0000-0000-000000000300', TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000091', '00000000-0000-0000-0000-000000000099', 'CONSELHO_ECONOMICO', 'membro-cae', '00000000-0000-0000-0000-000000000100', TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000092', '00000000-0000-0000-0000-000000000099', 'CONSELHO_ECONOMICO', 'membro-cae', '00000000-0000-0000-0000-000000000100', TRUE, 0)

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
    ('30000000-0000-0000-0000-000000000003', 'Reforma da Matriz', 'Reforma e pintura da Igreja Principal', 'EM_ANDAMENTO', '00000000-0000-0000-0000-0000000000dd', '2026-01-01T00:00:00Z', '2026-06-20T23:59:59Z', 0);

-- Events (Eventos)
INSERT INTO calendario.eventos (id, titulo, descricao, organizacao_responsavel_id, local_id, categoria, status, inicio_utc, fim_utc, projeto_id, version)
VALUES 
    ('40000000-0000-0000-0000-000000000001', 'Missa de Abertura da Catequese', 'Missa com pais e alunos', '00000000-0000-0000-0000-0000000000ee', '00000000-0000-0000-0000-000000000001', 'LITURGICO', 'CONFIRMADO', '2026-09-07T18:00:00Z', '2026-09-07T19:30:00Z', '30000000-0000-0000-0000-000000000001', 0),
    ('40000000-0000-0000-0000-000000000003', 'Encontro de Formação - Catequistas', 'Formação litúrgica', '00000000-0000-0000-0000-0000000000ee', '00000000-0000-0000-0000-000000000002', 'FORMATIVO', 'CONFIRMADO', '2026-06-10T19:30:00Z', '2026-06-10T21:00:00Z', '30000000-0000-0000-0000-000000000001', 0),
    ('40000000-0000-0000-0000-000000000004', 'Missa de Corpus Christi', 'Celebração solene e procissão', '00000000-0000-0000-0000-0000000000dd', '00000000-0000-0000-0000-000000000001', 'LITURGICO', 'CONFIRMADO', '2026-06-04T08:00:00Z', '2026-06-04T10:00:00Z', NULL, 0),
    ('40000000-0000-0000-0000-000000000005', 'Inauguração da Pintura', 'Evento de entrega das obras', '00000000-0000-0000-0000-0000000000dd', '00000000-0000-0000-0000-000000000001', 'SOCIAL', 'CONFIRMADO', '2026-06-18T19:00:00Z', '2026-06-18T21:00:00Z', '30000000-0000-0000-0000-000000000003', 0),
    ('40000000-0000-0000-0000-000000000006', 'Missa da Padroeira', 'Missa festiva de Nossa Senhora de Fátima', '00000000-0000-0000-0000-0000000000dd', '00000000-0000-0000-0000-000000000001', 'LITURGICO', 'CONFIRMADO', '2026-06-13T18:00:00Z', '2026-06-13T19:30:00Z', NULL, 0),
    ('40000000-0000-0000-0000-000000000007', 'Encontro de Casais', 'Formação com famílias', '00000000-0000-0000-0000-0000000000ff', '00000000-0000-0000-0000-000000000002', 'FORMATIVO', 'CONFIRMADO', '2026-06-14T08:00:00Z', '2026-06-14T12:00:00Z', NULL, 0),
    ('40000000-0000-0000-0000-000000000008', 'Reunião Ordinária do CPP', 'Planejamento pastoral', '00000000-0000-0000-0000-0000000000cc', '00000000-0000-0000-0000-000000000002', 'ADMINISTRATIVO', 'CONFIRMADO', '2026-06-10T20:00:00Z', '2026-06-10T21:30:00Z', NULL, 0),
    ('40000000-0000-0000-0000-000000000009', 'Live de Pentecostes', 'Transmissão ao vivo', '00000000-0000-0000-0000-0000000000aa', '00000000-0000-0000-0000-000000000001', 'SOCIAL', 'CONFIRMADO', '2026-06-05T19:00:00Z', '2026-06-05T20:30:00Z', NULL, 0),
    ('40000000-0000-0000-0000-000000000010', 'Grupo de Jovens Sentinelas', 'Momento de partilha e oração', '00000000-0000-0000-0000-0000000000bb', '00000000-0000-0000-0000-000000000003', 'FORMATIVO', 'CONFIRMADO', '2026-06-06T17:00:00Z', '2026-06-06T18:30:00Z', NULL, 0),
    ('40000000-0000-0000-0000-000000000011', 'Missa dos Catequistas', 'Celebração com benção dos catequistas', '00000000-0000-0000-0000-0000000000ee', '00000000-0000-0000-0000-000000000001', 'LITURGICO', 'CONFIRMADO', '2026-06-07T10:00:00Z', '2026-06-07T11:30:00Z', NULL, 0),
    ('40000000-0000-0000-0000-000000000012', 'Missa Dominical de Julho', 'Missa com a comunidade paroquial', '00000000-0000-0000-0000-0000000000dd', '00000000-0000-0000-0000-000000000001', 'LITURGICO', 'CONFIRMADO', '2026-07-26T18:00:00Z', '2026-07-26T19:30:00Z', NULL, 0),
    ('40000000-0000-0000-0000-000000000013', 'Reunião de Planejamento Pastoral', 'Avaliando o semestre', '00000000-0000-0000-0000-0000000000cc', '00000000-0000-0000-0000-000000000002', 'ADMINISTRATIVO', 'CONFIRMADO', '2026-07-28T19:30:00Z', '2026-07-28T21:00:00Z', NULL, 0),
    ('40000000-0000-0000-0000-000000000014', 'Missa de São Pedro e São Paulo', 'Celebração solene', '00000000-0000-0000-0000-0000000000dd', '00000000-0000-0000-0000-000000000001', 'LITURGICO', 'CONFIRMADO', '2026-07-30T19:00:00Z', '2026-07-30T20:30:00Z', NULL, 0),
    ('40000000-0000-0000-0000-000000000015', 'Encontro de Jovens de Férias', 'Momento de oração e lazer', '00000000-0000-0000-0000-0000000000bb', '00000000-0000-0000-0000-000000000003', 'SOCIAL', 'CONFIRMADO', '2026-07-31T17:00:00Z', '2026-07-31T19:00:00Z', NULL, 0),
    ('40000000-0000-0000-0000-000000000016', 'Missa da Família - Agosto', 'Abertura da semana da família', '00000000-0000-0000-0000-0000000000dd', '00000000-0000-0000-0000-000000000001', 'LITURGICO', 'CONFIRMADO', '2026-08-02T10:00:00Z', '2026-08-02T11:30:00Z', NULL, 0);

-- Event with Extra Status
INSERT INTO calendario.eventos (id, titulo, descricao, organizacao_responsavel_id, local_id, categoria, status, inicio_utc, fim_utc, adicionado_extra_justificativa, version)
VALUES 
    ('40000000-0000-0000-0000-000000000002', 'Reunião Extraordinária Pascom', 'Urgência na cobertura do evento x', '00000000-0000-0000-0000-0000000000aa', '00000000-0000-0000-0000-000000000003', 'ADMINISTRATIVO', 'ADICIONADO_EXTRA', '2026-09-10T20:00:00Z', '2026-09-10T21:00:00Z', 'Necessidade de planejar live de última hora', 0);

-- Recurring Events instances (Insert base event instance first with NULL recorrencia_id to satisfy FK)
INSERT INTO calendario.eventos (id, titulo, descricao, organizacao_responsavel_id, local_id, categoria, status, inicio_utc, fim_utc, projeto_id, recorrencia_id, version)
VALUES 
    ('40000000-0000-0000-0000-000000000101', 'Catequese Infantil - Turma A', 'Encontro semanal', '00000000-0000-0000-0000-0000000000ee', '00000000-0000-0000-0000-000000000003', 'FORMATIVO', 'CONFIRMADO', '2026-09-12T09:00:00Z', '2026-09-12T10:30:00Z', '30000000-0000-0000-0000-000000000001', NULL, 0);

-- Recurring Event stub (now can reference '40000000-0000-0000-0000-000000000101' as evento_base_id)
INSERT INTO calendario.eventos_recorrencia (id, evento_base_id, frequencia, intervalo, regra_json, data_fim_utc, version)
VALUES 
    ('50000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000101', 'SEMANAL', 1, '{"frequencia": "SEMANAL", "intervalo": 1, "diasDaSemana": ["SATURDAY"]}', '2026-11-28T23:59:59Z', 0);

-- Update base event instance to link to its recurrence
UPDATE calendario.eventos 
SET recorrencia_id = '50000000-0000-0000-0000-000000000001'
WHERE id = '40000000-0000-0000-0000-000000000101';

-- Insert remaining recurring instances referencing recorrencia_id
INSERT INTO calendario.eventos (id, titulo, descricao, organizacao_responsavel_id, local_id, categoria, status, inicio_utc, fim_utc, projeto_id, recorrencia_id, version)
VALUES 
    ('40000000-0000-0000-0000-000000000102', 'Catequese Infantil - Turma A', 'Encontro semanal', '00000000-0000-0000-0000-0000000000ee', '00000000-0000-0000-0000-000000000003', 'FORMATIVO', 'CONFIRMADO', '2026-09-19T09:00:00Z', '2026-09-19T10:30:00Z', '30000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', 0);

-- Approvals
INSERT INTO calendario.aprovacoes (
    id, evento_id, tipo_solicitacao, aprovador_papel, status, criado_em_utc, 
    decidido_em_utc, solicitante_id, aprovador_id, decision_observacao, 
    action_payload_json, mensagem_erro_execucao, version
) VALUES 
    (
        '60000000-0000-0000-0000-000000000001', NULL, 'CRIACAO_EVENTO', 'CONSELHO_COORDENADOR', 'PENDENTE', '2026-05-25T12:00:00Z', 
        NULL, 'maria.catequese', NULL, NULL, 
        '{"idempotencyKey": "idemp-0000-000000000001", "titulo": "Retiro Geral de Catequistas 2026", "inicio": "2026-09-15T08:00:00Z", "fim": "2026-09-15T18:00:00Z", "localId": "00000000-0000-0000-0000-000000000002", "descricao": "Encontro anual de planejamento e espiritualidade.", "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000ee", "categoria": "FORMATIVO", "status": "CONFIRMADO"}', NULL, 0
    ),
    (
        '60000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000001', 'EDICAO_EVENTO', 'PAROCO', 'PENDENTE', '2026-05-25T13:00:00Z', 
        NULL, 'joao.liturgia', NULL, NULL, 
        '{"titulo": "Missa de Abertura da Catequese (Novo Horário)", "inicio": "2026-09-07T17:00:00Z", "fim": "2026-09-07T18:30:00Z", "localId": "00000000-0000-0000-0000-000000000001", "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000ee", "categoria": "LITURGICO"}', NULL, 0
    ),
    (
        '60000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000101', 'CANCELAMENTO', 'CONSELHO_COORDENADOR', 'PENDENTE', '2026-05-25T14:00:00Z', 
        NULL, 'maria.catequese', NULL, NULL, 
        '{"motivo": "Falta de energia no Salão Paroquial"}', NULL, 0
    ),
    (
        '60000000-0000-0000-0000-000000000004', '40000000-0000-0000-0000-000000000001', 'CRIACAO_EVENTO', 'PAROCO', 'APROVADA', '2026-01-15T08:00:00Z', 
        '2026-01-16T09:00:00Z', 'padre.pedro', '10000000-0000-0000-0000-000000000001', 'Missa importante, autorizada.', 
        '{"idempotencyKey": "idemp-0000-000000000004", "titulo": "Missa de Abertura da Catequese", "inicio": "2026-09-07T18:00:00Z", "fim": "2026-09-07T19:30:00Z", "localId": "00000000-0000-0000-0000-000000000001", "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000ee", "categoria": "LITURGICO", "status": "CONFIRMADO"}', NULL, 0
    ),
    (
        '60000000-0000-0000-0000-000000000005', NULL, 'EDICAO_EVENTO', 'PAROCO', 'REPROVADA', '2026-02-09T10:00:00Z', 
        '2026-02-10T11:00:00Z', 'joao.liturgia', '10000000-0000-0000-0000-000000000001', 'Horário conflita com casamento agendado.', 
        '{"titulo": "Missa de Abertura (Horário Conflitante)", "inicio": "2026-09-07T19:00:00Z", "fim": "2026-09-07T20:30:00Z", "localId": "00000000-0000-0000-0000-000000000001", "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000ee", "categoria": "LITURGICO"}', NULL, 0
    ),
    (
        '60000000-0000-0000-0000-000000000006', NULL, 'CRIACAO_EVENTO', 'PAROCO', 'FALHA_EXECUCAO', '2026-02-07T10:00:00Z', 
        '2026-02-07T10:05:00Z', 'joao.liturgia', '10000000-0000-0000-0000-000000000001', 'Tentativa de aprovação automática falhou.', 
        '{"idempotencyKey": "idemp-0000-000000000006", "titulo": "Missa Festiva Pascom", "inicio": "2026-09-07T18:00:00Z", "fim": "2026-09-07T19:30:00Z", "localId": "00000000-0000-0000-0000-000000000001", "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000ee", "categoria": "LITURGICO", "status": "CONFIRMADO"}', 'Conflito de horário detectado no local Igreja Principal.', 0
    );

-- Observations
INSERT INTO calendario.observacoes_evento (id, evento_id, usuario_id, tipo, conteudo, criado_em_utc, version)
VALUES 
    (gen_random_uuid(), '40000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000002', 'NOTA', 'Lembrar de avisar os coroinhas sobre a missa.', '2026-02-01T14:00:00Z', 0);

-- Project Approvals (Solicitações de Aprovação de Projetos)
INSERT INTO calendario.projeto_solicitacoes_aprovacao (
    id, projeto_id, tipo_solicitacao, status, solicitante_id, 
    solicitante_papel, solicitante_tipo_organizacao, action_payload_json, 
    cancelar_eventos_vinculados, criado_em_utc, decidido_em_utc, aprovador_id, 
    decision_observacao, version
) VALUES 
    (
        '70000000-0000-0000-0000-000000000001', NULL, 'CRIACAO', 'PENDENTE', 'maria.catequese', 
        'COORDENADOR_PASTORAL', 'PASTORAL', '{"nome": "Novo Projeto Catequese 2027", "descricao": "Planejamento para o ano de 2027", "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000ee", "inicio": "2027-02-01T00:00:00Z", "fim": "2027-12-15T23:59:59Z"}', 
        FALSE, '2026-06-01T12:00:00Z', NULL, NULL, 
        NULL, 0
    ),
    (
        '70000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000002', 'CANCELAMENTO', 'PENDENTE', 'pedro.liturgia', 
        'COORDENADOR_PASTORAL', 'PASTORAL', NULL, 
        TRUE, '2026-06-01T13:00:00Z', NULL, NULL, 
        NULL, 0
    ),
    (
        '70000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000001', 'CRIACAO', 'APROVADA', 'maria.catequese', 
        'COORDENADOR_PASTORAL', 'PASTORAL', '{"nome": "Catequese 2026", "descricao": "Ano letivo da catequese infantil", "organizacaoResponsavelId": "00000000-0000-0000-0000-0000000000ee", "inicio": "2026-02-01T00:00:00Z", "fim": "2026-12-15T23:59:59Z"}', 
        FALSE, '2026-01-10T10:00:00Z', '2026-01-11T09:00:00Z', 'padre.pedro', 
        'Aprovado para início em ano aberto', 0
    );

-- Audit Trail
INSERT INTO calendario.auditoria_operacoes (id, organizacao_id, evento_id, recurso_tipo, recurso_id, acao, resultado, ator, ator_usuario_id, correlation_id, detalhes_auditaveis_json, ocorrido_em_utc)
VALUES 
    (gen_random_uuid(), '00000000-0000-0000-0000-0000000000ee', '40000000-0000-0000-0000-000000000001', 'EVENTO', '40000000-0000-0000-0000-000000000001', 'CRIACAO', 'SUCCESS', 'maria.catequese', '10000000-0000-0000-0000-000000000002', '60000000-0000-0000-0000-000000000004', '{"titulo": "Missa de Abertura da Catequese"}', '2026-01-15T08:00:00Z'),
    (gen_random_uuid(), '00000000-0000-0000-0000-0000000000ee', '40000000-0000-0000-0000-000000000001', 'EVENTO', '40000000-0000-0000-0000-000000000001', 'APROVACAO', 'SUCCESS', 'padre.pedro', '10000000-0000-0000-0000-000000000001', '60000000-0000-0000-0000-000000000004', '{"status": "CONFIRMADO"}', '2026-01-16T09:00:00Z');
