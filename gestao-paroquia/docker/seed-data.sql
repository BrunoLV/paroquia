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
    ('00000000-0000-0000-0000-000000000004', 'Cozinha Paroquial', 'SALA', 10, TRUE, '00000000-0000-0000-0000-000000000100', 0),
    ('00000000-0000-0000-0000-000000000005', 'Salão Comunidade São José Allamano', 'SALAO', 150, TRUE, '00000000-0000-0000-0000-000000000200', 0),
    ('00000000-0000-0000-0000-000000000006', 'Salão Comunidade Santo Agostinho', 'SALAO', 150, TRUE, '00000000-0000-0000-0000-000000000300', 0)
ON CONFLICT (id) DO NOTHING;


-- Users (Usuarios)
-- Fieis (pessoas físicas correspondentes aos Usuarios abaixo)
INSERT INTO calendario.fieis (id, nome, cpf, cpf_hash, data_nascimento, email, telefone, endereco,
                              vocacao, ativo, criado_em_utc, atualizado_em_utc, criado_por, atualizado_por)
VALUES
    ('1f000000-0000-0000-0000-000000000001', 'Pedro da Silva (Padre)', '00000000001', '031f50e5e09a5f61b2da02c2f59fde68fba579de4e1ef7a5490d6425f810977e', '1980-01-01', 'padre.pedro@example.com', '11999990001', 'Endereco Seed 1', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed'),
    ('1f000000-0000-0000-0000-000000000002', 'Maria Oliveira', '00000000002', '83764048c38ff46c8bea9b07d9ff2d8f8ee759334e3c4040da075c6de4964ade', '1980-01-01', 'maria.catequese@example.com', '11999990002', 'Endereco Seed 2', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed'),
    ('1f000000-0000-0000-0000-000000000003', 'João Santos', '00000000003', '5fe5f4974dabb8ad6e16a663a5da789608363f7ef8d036e90a08a82e76725a7e', '1980-01-01', 'joao.liturgia@example.com', '11999990003', 'Endereco Seed 3', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed'),
    ('1f000000-0000-0000-0000-000000000004', 'Pedro Junior', '00000000004', '8edd917ae34b0c41d9c5d1219601a98c5c24a6949334f89b8075cbcae32d6d14', '1980-01-01', 'pedro.liturgia@example.com', '11999990004', 'Endereco Seed 4', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed'),
    ('1f000000-0000-0000-0000-000000000005', 'Maria do Conselho', '00000000005', '575bb6dd332492ed89809e98c1eb300343e97898c12b09414560471242bed35a', '1980-01-01', 'maria.conselho@example.com', '11999990005', 'Endereco Seed 5', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed'),
    ('1f000000-0000-0000-0000-000000000006', 'João Batista (Vigário)', '00000000006', '471e904bc81fd079271f4add8b7b78384f30c19e557fb4a32e5f310ec4260ec6', '1980-01-01', 'padre.joao@example.com', '11999990007', 'Endereco Seed 6', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed'),
    ('1f000000-0000-0000-0000-000000000007', 'José do Conselho', '00000000007', '6b315f73399206b7b957093bc830705cf059be0d2c0bfdcf1afe52b704c1e7a9', '1980-01-01', 'jose.conselho@example.com', '11999990008', 'Endereco Seed 7', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed'),
    ('1f000000-0000-0000-0000-000000000008', 'Ana do Conselho', '00000000008', '46026a0184154dba96ac2cf6ef69aa6a57dcc07d768d8354b3ddd16008d355ea', '1980-01-01', 'ana.conselho@example.com', '11999990009', 'Endereco Seed 8', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed'),
    ('1f000000-0000-0000-0000-000000000009', 'Lúcia de Oliveira', '00000000009', 'cb10f8c68fbb2772eb8c11b4e4f0ee27937db6f3347d9696f709df9e94a5c487', '1980-01-01', 'lucia.catequese@example.com', '11999990010', 'Endereco Seed 9', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed'),
    ('1f000000-0000-0000-0000-000000000010', 'Carlos de Oliveira', '00000000010', 'bc40a6e499021d2b5a4b0b60e932d6b53053384218f81acdcb11fd83939be605', '1980-01-01', 'carlos.catequese@example.com', '11999990011', 'Endereco Seed 10', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed'),
    ('1f000000-0000-0000-0000-000000000011', 'Lucas dos Santos', '00000000011', 'ec95ca9a68d5e645068bff18a444f5035a9c474a6b44d186a20ed7e05a29689f', '1980-01-01', 'lucas.juventude@example.com', '11999990012', 'Endereco Seed 11', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed'),
    ('1f000000-0000-0000-0000-000000000012', 'Pedro Finanças', '00000000012', 'a7bbab4eab5ae60a310a77448f83ecae88ce92dfd60157871d94db9a07fe185f', '1980-01-01', 'pedro.financas@example.com', '11999990091', 'Endereco Seed 12', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed'),
    ('1f000000-0000-0000-0000-000000000013', 'Ana Administração', '00000000013', '82af09a26b663ccb5c486e3f39ae6c96edef701c29417c3003451839ce2a889b', '1980-01-01', 'ana.administracao@example.com', '11999990092', 'Endereco Seed 13', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed'),
    ('1f000000-0000-0000-0000-000000000014', 'Lúcia Fernandes (Presidente CPC Santo Agostinho)', '00000000014', 'a2b86a5f7a87f04cc769393e90bbeef42e403ba38d565f2ada4cfd886096bb4b', '1980-01-01', 'lucia.presidentecpc@example.com', '11999990013', 'Endereco Seed 14', 'LEIGO', TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'seed', 'seed')
ON CONFLICT (id) DO NOTHING;

INSERT INTO calendario.usuarios (id, username, password_hash, enabled, roles, igreja_id, nome, telefone, batizado, primeira_comunhao, crismado, casado_igreja_catolica, version, fiel_id)
VALUES 
    ('10000000-0000-0000-0000-000000000001', 'padre.pedro', '{noop}senha123', TRUE, 'ADMIN,PAROCO', '00000000-0000-0000-0000-000000000100', 'Pedro da Silva (Padre)', '11999990001', TRUE, TRUE, TRUE, FALSE, 0, '1f000000-0000-0000-0000-000000000001'),
    ('10000000-0000-0000-0000-000000000002', 'maria.catequese', '{noop}senha123', TRUE, 'COORDENADOR_PASTORAL_GLOBAL', '00000000-0000-0000-0000-000000000100', 'Maria Oliveira', '11999990002', TRUE, TRUE, TRUE, TRUE, 0, '1f000000-0000-0000-0000-000000000002'),
    ('10000000-0000-0000-0000-000000000003', 'joao.liturgia', '{noop}senha123', TRUE, 'VOLUNTARIO_PASTORAL_LOCAL', '00000000-0000-0000-0000-000000000300', 'João Santos', '11999990003', TRUE, TRUE, TRUE, FALSE, 0, '1f000000-0000-0000-0000-000000000003'),
    ('10000000-0000-0000-0000-000000000004', 'pedro.liturgia', '{noop}senha123', TRUE, 'COORDENADOR_PASTORAL_LOCAL', '00000000-0000-0000-0000-000000000100', 'Pedro Junior', '11999990004', TRUE, TRUE, FALSE, FALSE, 0, '1f000000-0000-0000-0000-000000000004'),
    ('10000000-0000-0000-0000-000000000005', 'maria.conselho', '{noop}senha123', TRUE, 'MESA_DIRETORA_CPP', '00000000-0000-0000-0000-000000000100', 'Maria do Conselho', '11999990005', TRUE, TRUE, TRUE, TRUE, 0, '1f000000-0000-0000-0000-000000000005'),
    ('10000000-0000-0000-0000-000000000007', 'padre.joao', '{noop}senha123', TRUE, 'VIGARIO', '00000000-0000-0000-0000-000000000100', 'João Batista (Vigário)', '11999990007', TRUE, TRUE, TRUE, FALSE, 0, '1f000000-0000-0000-0000-000000000006'),
    ('10000000-0000-0000-0000-000000000008', 'jose.conselho', '{noop}senha123', TRUE, 'MEMBRO_CPP', '00000000-0000-0000-0000-000000000100', 'José do Conselho', '11999990008', TRUE, TRUE, TRUE, TRUE, 0, '1f000000-0000-0000-0000-000000000007'),
    ('10000000-0000-0000-0000-000000000009', 'ana.conselho', '{noop}senha123', TRUE, 'MEMBRO_CPP', '00000000-0000-0000-0000-000000000100', 'Ana do Conselho', '11999990009', TRUE, TRUE, TRUE, TRUE, 0, '1f000000-0000-0000-0000-000000000008'),
    ('10000000-0000-0000-0000-000000000010', 'lucia.catequese', '{noop}senha123', TRUE, 'COORDENADOR_PASTORAL_LOCAL', '00000000-0000-0000-0000-000000000100', 'Lúcia de Oliveira', '11999990010', TRUE, TRUE, TRUE, FALSE, 0, '1f000000-0000-0000-0000-000000000009'),
    ('10000000-0000-0000-0000-000000000011', 'carlos.catequese', '{noop}senha123', TRUE, 'VOLUNTARIO_PASTORAL_LOCAL', '00000000-0000-0000-0000-000000000100', 'Carlos de Oliveira', '11999990011', TRUE, TRUE, FALSE, FALSE, 0, '1f000000-0000-0000-0000-000000000010'),
    ('10000000-0000-0000-0000-000000000012', 'lucas.juventude', '{noop}senha123', TRUE, 'COORDENADOR_PASTORAL_LOCAL', '00000000-0000-0000-0000-000000000100', 'Lucas dos Santos', '11999990012', TRUE, TRUE, TRUE, FALSE, 0, '1f000000-0000-0000-0000-000000000011'),
    ('10000000-0000-0000-0000-000000000091', 'pedro.financas', '{noop}senha123', TRUE, 'MEMBRO_CAE', '00000000-0000-0000-0000-000000000100', 'Pedro Finanças', '11999990091', TRUE, TRUE, TRUE, TRUE, 0, '1f000000-0000-0000-0000-000000000012'),
    ('10000000-0000-0000-0000-000000000092', 'ana.administracao', '{noop}senha123', TRUE, 'MEMBRO_CAE', '00000000-0000-0000-0000-000000000100', 'Ana Administração', '11999990092', TRUE, TRUE, TRUE, TRUE, 0, '1f000000-0000-0000-0000-000000000013'),
    -- Presidente do CPC de Santo Agostinho, escopado diretamente à comunidade — usado para testar a
    -- autorização de empréstimo de ativos patrimoniais (só o presidente do CPC da organização DONA do
    -- ativo pode aprovar o empréstimo, junto com o pároco/admin)
    ('10000000-0000-0000-0000-000000000013', 'lucia.presidentecpc', '{noop}senha123', TRUE, 'PRESIDENTE_CPC', '00000000-0000-0000-0000-000000000300', 'Lúcia Fernandes (Presidente CPC Santo Agostinho)', '11999990013', TRUE, TRUE, TRUE, TRUE, 0, '1f000000-0000-0000-0000-000000000014')
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
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000092', '00000000-0000-0000-0000-000000000099', 'CONSELHO_ECONOMICO', 'membro-cae', '00000000-0000-0000-0000-000000000100', TRUE, 0),
    -- Escopo direto na Comunidade (organizacao_id = Santo Agostinho), não no conselho — necessário para
    -- a checagem de autorização de empréstimo de ativos, que compara o contexto do ator diretamente
    -- contra a organização dona do ativo.
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000013', '00000000-0000-0000-0000-000000000300', 'CONSELHO_PASTORAL', 'presidente-cpc', '00000000-0000-0000-0000-000000000300', TRUE, 0)

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
    ('40000000-0000-0000-0000-000000000016', 'Missa da Família - Agosto', 'Abertura da semana da família', '00000000-0000-0000-0000-0000000000dd', '00000000-0000-0000-0000-000000000001', 'LITURGICO', 'CONFIRMADO', '2026-08-02T10:00:00Z', '2026-08-02T11:30:00Z', NULL, 0),
    -- Eventos em locais comunitários (fora da Matriz) — usados para testar alocação/empréstimo de ativos patrimoniais
    ('40000000-0000-0000-0000-000000000201', 'Festa Junina Comunitária - São José', 'Arraiá da comunidade com quermesse', '00000000-0000-0000-0000-0000000002c0', '00000000-0000-0000-0000-000000000005', 'SOCIAL', 'CONFIRMADO', '2026-07-10T18:00:00Z', '2026-07-15T22:00:00Z', NULL, 0),
    ('40000000-0000-0000-0000-000000000202', 'Reunião do Conselho - Santo Agostinho', 'Planejamento pastoral comunitário', '00000000-0000-0000-0000-0000000003c0', '00000000-0000-0000-0000-000000000006', 'ADMINISTRATIVO', 'CONFIRMADO', '2026-08-12T19:30:00Z', '2026-08-12T21:00:00Z', NULL, 0),
    ('40000000-0000-0000-0000-000000000203', 'Retiro Paroquial de Agosto', 'Retiro espiritual aberto a todas as comunidades', '00000000-0000-0000-0000-0000000000dd', '00000000-0000-0000-0000-000000000001', 'FORMATIVO', 'CONFIRMADO', '2026-08-20T08:00:00Z', '2026-08-20T18:00:00Z', NULL, 0),
    ('40000000-0000-0000-0000-000000000204', 'Encontro Comunitário de Agosto - São José', 'Encontro mensal da comunidade', '00000000-0000-0000-0000-0000000002c0', '00000000-0000-0000-0000-000000000005', 'SOCIAL', 'CONFIRMADO', '2026-08-25T18:00:00Z', '2026-08-26T22:00:00Z', NULL, 0);

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

-- ============================================================================
-- 2. PATRIMÔNIO — Ativos Patrimoniais e Alocações (massa de teste)
-- ============================================================================
-- Cobre os cenários: alocação direta (mesma organização), empréstimo entre
-- organizações (pendente de aprovação), manutenção, atraso de devolução (com
-- impacto em reserva futura) e ativo baixado. Organizações donas: Matriz (100),
-- Comunidade São José Allamano (200) e Comunidade Santo Agostinho (300) — as
-- únicas elegíveis a possuir ativos.

INSERT INTO calendario.ativos_patrimoniais (
    id, nome, numero_tombamento, categoria, status, reservavel, organizacao_id,
    data_aquisicao, valor_estimado, criado_em_utc, version
) VALUES
    ('80000000-0000-0000-0000-000000000001', 'Projetor Multimídia Epson', 'PAT-2026-0001', 'ELETRONICO', 'DISPONIVEL', TRUE, '00000000-0000-0000-0000-000000000100', '2024-03-10', 3500.00, '2026-01-01T00:00:00Z', 0),
    ('80000000-0000-0000-0000-000000000002', 'Caixa de Som Amplificada', 'PAT-2026-0002', 'ELETRONICO', 'DISPONIVEL', TRUE, '00000000-0000-0000-0000-000000000200', '2024-06-15', 1800.00, '2026-01-01T00:00:00Z', 0),
    ('80000000-0000-0000-0000-000000000003', 'Kit 100 Cadeiras Plásticas', 'PAT-2026-0003', 'MOBILIARIO', 'DISPONIVEL', TRUE, '00000000-0000-0000-0000-000000000300', '2023-11-20', 4200.00, '2026-01-01T00:00:00Z', 0),
    ('80000000-0000-0000-0000-000000000004', 'Notebook Dell Administrativo', 'PAT-2026-0004', 'ELETRONICO', 'ALOCADO', FALSE, '00000000-0000-0000-0000-000000000100', '2025-02-01', 3200.00, '2026-01-01T00:00:00Z', 0),
    ('80000000-0000-0000-0000-000000000005', 'Kit Som Portátil Comunitário', 'PAT-2026-0005', 'ELETRONICO', 'DISPONIVEL', TRUE, '00000000-0000-0000-0000-000000000300', '2024-09-05', 1500.00, '2026-01-01T00:00:00Z', 0),
    ('80000000-0000-0000-0000-000000000006', 'Retroprojetor Antigo', 'PAT-2026-0006', 'ELETRONICO', 'EM_MANUTENCAO', FALSE, '00000000-0000-0000-0000-000000000100', '2018-04-12', 200.00, '2026-01-01T00:00:00Z', 0),
    ('80000000-0000-0000-0000-000000000007', 'Som Amplificado Comunitário', 'PAT-2026-0007', 'ELETRONICO', 'ALOCADO', TRUE, '00000000-0000-0000-0000-000000000200', '2024-02-20', 1600.00, '2026-01-01T00:00:00Z', 0),
    ('80000000-0000-0000-0000-000000000008', 'Armário Antigo (Fora de Uso)', 'PAT-2026-0008', 'MOBILIARIO', 'BAIXADO', FALSE, '00000000-0000-0000-0000-000000000100', '2015-01-01', 0.00, '2026-01-01T00:00:00Z', 0)
ON CONFLICT (id) DO NOTHING;

-- Mantém o contador de tombamento de 2026 à frente dos 8 números usados acima,
-- para que o próximo ativo criado pela aplicação gere PAT-2026-0009 sem colidir.
INSERT INTO calendario.patrimonio_numeracao_anual (ano, ultimo_numero, version)
VALUES (2026, 8, 0)
ON CONFLICT (ano) DO NOTHING;

-- Solicitação de aprovação para o empréstimo pendente (ativo 0005 → evento 0203, Retiro Paroquial)
INSERT INTO calendario.aprovacoes (
    id, evento_id, tipo_solicitacao, aprovador_papel, status, criado_em_utc,
    decidido_em_utc, solicitante_id, solicitante_papel, solicitante_tipo_organizacao,
    aprovador_id, decision_observacao, action_payload_json, correlation_id,
    mensagem_erro_execucao, version
) VALUES (
    '60000000-0000-0000-0000-000000000007', '40000000-0000-0000-0000-000000000203', 'ALOCACAO_ATIVO_EMPRESTIMO', 'conselho-coordenador', 'PENDENTE', '2026-08-01T09:00:00Z',
    NULL, '10000000-0000-0000-0000-000000000003', 'voluntario-pastoral-local', 'PASTORAL',
    NULL, NULL, '{"eventoId": "40000000-0000-0000-0000-000000000203", "organizacaoResponsavelId": "00000000-0000-0000-0000-000000000300", "motivo": "Empréstimo do kit de som para uso no retiro paroquial de agosto", "localId": "00000000-0000-0000-0000-000000000001", "ativoId": "80000000-0000-0000-0000-000000000005", "ativoNome": "Kit Som Portátil Comunitário"}', '81000000-0000-0000-0000-000000000002',
    NULL, 0
) ON CONFLICT (id) DO NOTHING;

INSERT INTO calendario.alocacoes_ativos (
    id, ativo_id, tipo, evento_id, organizacao_origem_id, organizacao_destino_id,
    emprestimo, data_inicio, data_fim, status, solicitante_id, justificativa,
    aprovacao_id, impactada_por_atraso, alerta_atraso_emitido_em, motivo_cancelamento,
    criado_em_utc, devolvido_em_utc, version
) VALUES
    -- Alocação direta confirmada: notebook (Matriz) usado na Missa da Família (local da Matriz)
    ('81000000-0000-0000-0000-000000000001', '80000000-0000-0000-0000-000000000004', 'ALOCACAO_EVENTO', '40000000-0000-0000-0000-000000000016', '00000000-0000-0000-0000-000000000100', '00000000-0000-0000-0000-000000000100', FALSE, '2026-08-02', '2026-08-02', 'CONFIRMADA', NULL, 'Uso administrativo durante a missa da família', NULL, FALSE, NULL, NULL, '2026-07-30T10:00:00Z', NULL, 0),
    -- Empréstimo pendente de aprovação: kit de som (Santo Agostinho) emprestado para o Retiro Paroquial (local da Matriz)
    ('81000000-0000-0000-0000-000000000002', '80000000-0000-0000-0000-000000000005', 'ALOCACAO_EVENTO', '40000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000300', '00000000-0000-0000-0000-000000000100', TRUE, '2026-08-20', '2026-08-20', 'PENDENTE_APROVACAO', '10000000-0000-0000-0000-000000000003', 'Empréstimo do kit de som para uso no retiro paroquial de agosto', '60000000-0000-0000-0000-000000000007', FALSE, NULL, NULL, '2026-08-01T09:00:00Z', NULL, 0),
    -- Manutenção em andamento do retroprojetor antigo (Matriz)
    ('81000000-0000-0000-0000-000000000003', '80000000-0000-0000-0000-000000000006', 'MANUTENCAO', NULL, '00000000-0000-0000-0000-000000000100', NULL, FALSE, '2026-07-28', '2026-08-10', 'CONFIRMADA', NULL, 'Reparo da lâmpada e da lente', NULL, FALSE, NULL, NULL, '2026-07-28T08:00:00Z', NULL, 0),
    -- Devolução atrasada: som amplificado (São José) usado na Festa Junina, ainda não devolvido
    ('81000000-0000-0000-0000-000000000004', '80000000-0000-0000-0000-000000000007', 'ALOCACAO_EVENTO', '40000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000200', '00000000-0000-0000-0000-000000000200', FALSE, '2026-07-10', '2026-07-15', 'ATRASADA', NULL, 'Uso na festa junina comunitária', NULL, FALSE, '2026-07-18T00:00:00Z', NULL, '2026-07-08T10:00:00Z', NULL, 0),
    -- Reserva futura do mesmo ativo, impactada pelo atraso acima (usado para testar o alerta em cascata)
    ('81000000-0000-0000-0000-000000000005', '80000000-0000-0000-0000-000000000007', 'ALOCACAO_EVENTO', '40000000-0000-0000-0000-000000000204', '00000000-0000-0000-0000-000000000200', '00000000-0000-0000-0000-000000000200', FALSE, '2026-08-25', '2026-08-26', 'CONFIRMADA', NULL, 'Uso no encontro comunitário de agosto', NULL, TRUE, NULL, NULL, '2026-08-01T10:00:00Z', NULL, 0)
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
('75000000-0000-0000-0000-000000002035', 'Tempo Comum 2035', 'TEMPO_COMUM', 'VERDE', 1, 14, 12, 1, 1.0, 'Tempo Comum 2035', 2035);

