-- NS Fatima Calendar MVP - Seed Data
-- Este script deve ser executado APÓS as migrações do Flyway.

-- Limpa dados existentes para evitar conflitos em re-execuções
TRUNCATE calendario.projeto_solicitacoes_aprovacao, calendario.auditoria_operacoes, calendario.observacoes_evento, calendario.aprovacoes, 
         calendario.eventos, calendario.eventos_recorrencia, calendario.projetos_eventos, 
         calendario.anos_paroquiais, calendario.membros_organizacao, calendario.membros, 
         calendario.usuarios, calendario.locais, calendario.organizacoes RESTART IDENTITY CASCADE;

-- ============================================================================
-- 1. SEED DATA
-- ============================================================================

-- Locations (Locais)
INSERT INTO calendario.locais (id, nome, tipo, capacidade, ativo, version)
VALUES 
    ('00000000-0000-0000-0000-000000000001', 'Igreja Principal', 'IGREJA', 500, TRUE, 0),
    ('00000000-0000-0000-0000-000000000002', 'Salão Paroquial', 'SALAO', 300, TRUE, 0),
    ('00000000-0000-0000-0000-000000000003', 'Sala de Catequese 1', 'SALA', 30, TRUE, 0),
    ('00000000-0000-0000-0000-000000000004', 'Cozinha Paroquial', 'SALA', 10, TRUE, 0);

-- Organizations (Organizacoes)
INSERT INTO calendario.organizacoes (id, nome, tipo, contato, ativo, parent_id, version)
VALUES 
    ('00000000-0000-0000-0000-000000000001', 'Paróquia Nossa Senhora de Fátima', 'CLERO', 'contato@nsfatima.com', TRUE, NULL, 0),
    ('00000000-0000-0000-0000-000000000100', 'Igreja Matriz Nossa Senhora de Fátima', 'MATRIZ', 'matriz@nsfatima.com', TRUE, NULL, 0),
    ('00000000-0000-0000-0000-000000000200', 'Comunidade São José Allamano', 'COMUNIDADE', 'saojose@nsfatima.com', TRUE, '00000000-0000-0000-0000-000000000100', 0),
    ('00000000-0000-0000-0000-000000000300', 'Comunidade Santo Agostinho', 'COMUNIDADE', 'santoagostinho@nsfatima.com', TRUE, '00000000-0000-0000-0000-000000000100', 0),
    ('00000000-0000-0000-0000-0000000000aa', 'Pastoral da Comunicação', 'PASTORAL', 'pascom@nsfatima.com', TRUE, NULL, 0),
    ('00000000-0000-0000-0000-0000000000bb', 'Pastoral da Juventude', 'PASTORAL', 'juventude@nsfatima.com', TRUE, NULL, 0),
    ('00000000-0000-0000-0000-0000000000cc', 'Conselho Pastoral Paroquial', 'CONSELHO', 'cpp@nsfatima.com', TRUE, NULL, 0),
    ('00000000-0000-0000-0000-0000000000dd', 'Clero Paroquial', 'CLERO', 'clero@nsfatima.com', TRUE, NULL, 0),
    ('00000000-0000-0000-0000-0000000000ee', 'Pastoral da Catequese', 'PASTORAL', 'catequese@nsfatima.com', TRUE, NULL, 0),
    ('00000000-0000-0000-0000-0000000000ff', 'Pastoral do Batismo', 'PASTORAL', 'batismo@nsfatima.com', TRUE, NULL, 0),
    ('00000000-0000-0000-0000-000000000099', 'Conselho Econômico Paroquial', 'CONSELHO_ECONOMICO', 'economico@nsfatima.com', TRUE, NULL, 0)
ON CONFLICT (id) DO NOTHING;

-- Users (Usuarios)
INSERT INTO calendario.usuarios (id, username, password_hash, enabled, roles, version)
VALUES 
    ('10000000-0000-0000-0000-000000000001', 'padre.pedro', '{noop}senha123', TRUE, 'ADMIN,COORDENADOR_CLERO', 0),
    ('10000000-0000-0000-0000-000000000002', 'maria.catequese', '{noop}senha123', TRUE, 'COORDENADOR_PASTORAL', 0),
    ('10000000-0000-0000-0000-000000000003', 'joao.liturgia', '{noop}senha123', TRUE, 'MEMBRO_PASTORAL', 0),
    ('10000000-0000-0000-0000-000000000004', 'pedro.liturgia', '{noop}senha123', TRUE, 'COORDENADOR_PASTORAL', 0),
    ('10000000-0000-0000-0000-000000000005', 'maria.conselho', '{noop}senha123', TRUE, 'COORDENADOR_CONSELHO', 0),
    ('10000000-0000-0000-0000-000000000007', 'padre.joao', '{noop}senha123', TRUE, 'COORDENADOR_CLERO', 0),
    ('10000000-0000-0000-0000-000000000008', 'jose.conselho', '{noop}senha123', TRUE, 'COORDENADOR_CONSELHO', 0),
    ('10000000-0000-0000-0000-000000000009', 'ana.conselho', '{noop}senha123', TRUE, 'COORDENADOR_CONSELHO', 0),
    ('10000000-0000-0000-0000-000000000010', 'lucia.catequese', '{noop}senha123', TRUE, 'COORDENADOR_PASTORAL', 0),
    ('10000000-0000-0000-0000-000000000011', 'carlos.catequese', '{noop}senha123', TRUE, 'MEMBRO_PASTORAL', 0),
    ('10000000-0000-0000-0000-000000000012', 'lucas.juventude', '{noop}senha123', TRUE, 'COORDENADOR_PASTORAL', 0),
    ('10000000-0000-0000-0000-000000000091', 'pedro.financas', '{noop}senha123', TRUE, 'COORDENADOR_CONSELHO', 0),
    ('10000000-0000-0000-0000-000000000092', 'ana.administracao', '{noop}senha123', TRUE, 'COORDENADOR_CONSELHO', 0);

-- Members (Membros)
INSERT INTO calendario.membros (id, nome_completo, email, telefone, usuario_id, ativo, version)
VALUES
    ('20000000-0000-0000-0000-000000000001', 'Pedro da Silva (Padre)', 'padre.pedro@nsfatima.com', '11999990001', '10000000-0000-0000-0000-000000000001', TRUE, 0),
    ('20000000-0000-0000-0000-000000000002', 'Maria Oliveira', 'maria.catequese@gmail.com', '11999990002', '10000000-0000-0000-0000-000000000002', TRUE, 0),
    ('20000000-0000-0000-0000-000000000003', 'João Santos', 'joao.liturgia@outlook.com', '11999990003', '10000000-0000-0000-0000-000000000003', TRUE, 0),
    ('20000000-0000-0000-0000-000000000004', 'Pedro Junior', 'pedro.liturgia@bol.com.br', '11999990004', '10000000-0000-0000-0000-000000000004', TRUE, 0),
    ('20000000-0000-0000-0000-000000000005', 'Maria do Conselho', 'conselho@nsfatima.com', '11999990005', '10000000-0000-0000-0000-000000000005', TRUE, 0),
    ('20000000-0000-0000-0000-000000000007', 'João Batista (Vigário)', 'padre.joao@nsfatima.com', '11999990007', '10000000-0000-0000-0000-000000000007', TRUE, 0),
    ('20000000-0000-0000-0000-000000000008', 'José do Conselho', 'jose.conselho@nsfatima.com', '11999990008', '10000000-0000-0000-0000-000000000008', TRUE, 0),
    ('20000000-0000-0000-0000-000000000009', 'Ana do Conselho', 'ana.conselho@nsfatima.com', '11999990009', '10000000-0000-0000-0000-000000000009', TRUE, 0),
    ('20000000-0000-0000-0000-000000000010', 'Lúcia de Oliveira', 'lucia.catequese@gmail.com', '11999990010', '10000000-0000-0000-0000-000000000010', TRUE, 0),
    ('20000000-0000-0000-0000-000000000011', 'Carlos de Oliveira', 'carlos.catequese@gmail.com', '11999990011', '10000000-0000-0000-0000-000000000011', TRUE, 0),
    ('20000000-0000-0000-0000-000000000012', 'Lucas dos Santos', 'lucas.juventude@gmail.com', '11999990012', '10000000-0000-0000-0000-000000000012', TRUE, 0),
    ('20000000-0000-0000-0000-000000000091', 'Pedro Finanças', 'pedro.financas@nsfatima.com', '11999990091', '10000000-0000-0000-0000-000000000091', TRUE, 0),
    ('20000000-0000-0000-0000-000000000092', 'Ana Administração', 'ana.administracao@nsfatima.com', '11999990092', '10000000-0000-0000-0000-000000000092', TRUE, 0);

-- Organizational Memberships
INSERT INTO calendario.membros_organizacao (id, usuario_id, organizacao_id, tipo_organizacao, papel, ativo, version)
VALUES
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-0000000000dd', 'CLERO', 'paroco', TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000007', '00000000-0000-0000-0000-0000000000dd', 'CLERO', 'vigario', TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-0000000000cc', 'CONSELHO', 'coordenador', TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000008', '00000000-0000-0000-0000-0000000000cc', 'CONSELHO', 'vice-coordenador', TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000009', '00000000-0000-0000-0000-0000000000cc', 'CONSELHO', 'secretario', TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-0000000000ee', 'PASTORAL', 'coordenador', TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-0000000000ee', 'PASTORAL', 'vice-coordenador', TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-0000000000ee', 'PASTORAL', 'membro', TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-0000000000bb', 'PASTORAL', 'coordenador', TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000012', '00000000-0000-0000-0000-0000000000bb', 'PASTORAL', 'vice-coordenador', TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-0000000000bb', 'PASTORAL', 'membro', TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000091', '00000000-0000-0000-0000-000000000099', 'CONSELHO_ECONOMICO', 'coordenador', TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000092', '00000000-0000-0000-0000-000000000099', 'CONSELHO_ECONOMICO', 'vice-coordenador', TRUE, 0);

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
    ('40000000-0000-0000-0000-000000000011', 'Missa dos Catequistas', 'Celebração com benção dos catequistas', '00000000-0000-0000-0000-0000000000ee', '00000000-0000-0000-0000-000000000001', 'LITURGICO', 'CONFIRMADO', '2026-06-07T10:00:00Z', '2026-06-07T11:30:00Z', NULL, 0);

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
