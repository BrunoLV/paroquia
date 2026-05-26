-- NS Fatima Calendar MVP - Seed Data
-- Este script deve ser executado APÓS as migrações do Flyway.

-- Limpa dados existentes para evitar conflitos em re-execuções
TRUNCATE calendario.auditoria_operacao, calendario.observacoes_evento, calendario.aprovacoes, 
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

-- EXTRA Organizations (Aquelas que o Flyway V001 NÃO cria)
INSERT INTO calendario.organizacoes (id, nome, tipo, contato, ativo, version)
VALUES 
    ('00000000-0000-0000-0000-0000000000ee', 'Pastoral da Catequese', 'PASTORAL', 'catequese@nsfatima.com', TRUE, 0),
    ('00000000-0000-0000-0000-0000000000ff', 'Pastoral do Batismo', 'PASTORAL', 'batismo@nsfatima.com', TRUE, 0)
ON CONFLICT (id) DO NOTHING;

-- Users (Usuarios)
INSERT INTO calendario.usuarios (id, username, password_hash, enabled, roles, version)
VALUES 
    ('10000000-0000-0000-0000-000000000001', 'padre.pedro', '{noop}senha123', TRUE, 'ADMIN,COORDENADOR_CLERO', 0),
    ('10000000-0000-0000-0000-000000000002', 'maria.catequese', '{noop}senha123', TRUE, 'COORDENADOR_PASTORAL', 0),
    ('10000000-0000-0000-0000-000000000003', 'joao.liturgia', '{noop}senha123', TRUE, 'MEMBRO_PASTORAL', 0),
    ('10000000-0000-0000-0000-000000000004', 'pedro.liturgia', '{noop}senha123', TRUE, 'COORDENADOR_PASTORAL', 0),
    ('10000000-0000-0000-0000-000000000005', 'maria.conselho', '{noop}senha123', TRUE, 'COORDENADOR_CONSELHO', 0);

-- Members (Membros)
INSERT INTO calendario.membros (id, nome_completo, email, telefone, usuario_id, ativo, version)
VALUES
    ('20000000-0000-0000-0000-000000000001', 'Pedro da Silva (Padre)', 'padre.pedro@nsfatima.com', '11999990001', '10000000-0000-0000-0000-000000000001', TRUE, 0),
    ('20000000-0000-0000-0000-000000000002', 'Maria Oliveira', 'maria.catequese@gmail.com', '11999990002', '10000000-0000-0000-0000-000000000002', TRUE, 0),
    ('20000000-0000-0000-0000-000000000003', 'João Santos', 'joao.liturgia@outlook.com', '11999990003', '10000000-0000-0000-0000-000000000003', TRUE, 0),
    ('20000000-0000-0000-0000-000000000004', 'Pedro Junior', 'pedro.liturgia@bol.com.br', '11999990004', '10000000-0000-0000-0000-000000000004', TRUE, 0),
    ('20000000-0000-0000-0000-000000000005', 'Maria do Conselho', 'conselho@nsfatima.com', '11999990005', '10000000-0000-0000-0000-000000000005', TRUE, 0);

-- Organizational Memberships
INSERT INTO calendario.membros_organizacao (id, usuario_id, organizacao_id, tipo_organizacao, papel, ativo, version)
VALUES
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-0000000000dd', 'CLERO', 'COORDENADOR', TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-0000000000ee', 'PASTORAL', 'COORDENADOR', TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-0000000000bb', 'PASTORAL', 'MEMBRO', TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-0000000000bb', 'PASTORAL', 'COORDENADOR', TRUE, 0),
    (gen_random_uuid(), '10000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-0000000000cc', 'CONSELHO', 'COORDENADOR', TRUE, 0);

-- Years (Anos Paroquiais)
INSERT INTO calendario.anos_paroquiais (ano, status, data_fechamento_utc)
VALUES 
    (2025, 'FECHADO', '2025-01-01T00:00:00Z'),
    (2026, 'PLANEJAMENTO', NULL),
    (2027, 'ABERTO', NULL)
ON CONFLICT (ano) DO NOTHING;

-- Projects (Projetos)
INSERT INTO calendario.projetos_eventos (id, nome, descricao, status, organizacao_responsavel_id, inicio_utc, fim_utc, version)
VALUES 
    ('30000000-0000-0000-0000-000000000001', 'Catequese 2026', 'Ano letivo da catequese infantil', 'ATIVO', '00000000-0000-0000-0000-0000000000ee', '2026-02-01T00:00:00Z', '2026-12-15T23:59:59Z', 0),
    ('30000000-0000-0000-0000-000000000002', 'Festividades de Maio', 'Mês de Nossa Senhora', 'ATIVO', '00000000-0000-0000-0000-0000000000dd', '2026-05-01T00:00:00Z', '2026-05-31T23:59:59Z', 0);

-- Events (Eventos)
INSERT INTO calendario.eventos (id, titulo, descricao, organizacao_responsavel_id, local_id, categoria, status, inicio_utc, fim_utc, projeto_id, version)
VALUES 
    ('40000000-0000-0000-0000-000000000001', 'Missa de Abertura da Catequese', 'Missa com pais e alunos', '00000000-0000-0000-0000-0000000000ee', '00000000-0000-0000-0000-000000000001', 'LITURGICO', 'CONFIRMADO', '2026-02-07T18:00:00Z', '2026-02-07T19:30:00Z', '30000000-0000-0000-0000-000000000001', 0);

-- Event with Extra Status
INSERT INTO calendario.eventos (id, titulo, descricao, organizacao_responsavel_id, local_id, categoria, status, inicio_utc, fim_utc, adicionado_extra_justificativa, version)
VALUES 
    ('40000000-0000-0000-0000-000000000002', 'Reunião Extraordinária Pascom', 'Urgência na cobertura do evento x', '00000000-0000-0000-0000-0000000000aa', '00000000-0000-0000-0000-000000000003', 'ADMINISTRATIVO', 'ADICIONADO_EXTRA', '2026-03-10T20:00:00Z', '2026-03-10T21:00:00Z', 'Necessidade de planejar live de última hora', 0);

-- Recurring Event stub
INSERT INTO calendario.eventos_recorrencia (id, data_inicio, data_fim, regra, version)
VALUES 
    ('50000000-0000-0000-0000-000000000001', '2026-02-14', '2026-11-28', '{"frequencia": "SEMANAL", "intervalo": 1, "diasDaSemana": ["SATURDAY"]}', 0);

-- Recurring Events instances
INSERT INTO calendario.eventos (id, titulo, descricao, organizacao_responsavel_id, local_id, categoria, status, inicio_utc, fim_utc, projeto_id, recorrencia_id, version)
VALUES 
    ('40000000-0000-0000-0000-000000000101', 'Catequese Infantil - Turma A', 'Encontro semanal', '00000000-0000-0000-0000-0000000000ee', '00000000-0000-0000-0000-000000000003', 'FORMATIVO', 'CONFIRMADO', '2026-02-14T09:00:00Z', '2026-02-14T10:30:00Z', '30000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', 0),
    ('40000000-0000-0000-0000-000000000102', 'Catequese Infantil - Turma A', 'Encontro semanal', '00000000-0000-0000-0000-0000000000ee', '00000000-0000-0000-0000-000000000003', 'FORMATIVO', 'CONFIRMADO', '2026-02-21T09:00:00Z', '2026-02-21T10:30:00Z', '30000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001', 0);

-- Approvals
INSERT INTO calendario.aprovacoes (
    id, evento_id, tipo_solicitacao, aprovador_papel, status, criado_em_utc, 
    decidido_em_utc, solicitante_id, aprovador_id, decision_observacao, 
    action_payload_json, mensagem_erro_execucao, version
) VALUES 
    (
        '60000000-0000-0000-0000-000000000001', NULL, 'CRIACAO_EVENTO', 'CONSELHO_COORDENADOR', 'PENDENTE', '2026-05-25T12:00:00Z', 
        NULL, 'maria.catequese', NULL, NULL, 
        '{"titulo": "Retiro Geral de Catequistas 2026", "inicio": "2026-06-15T08:00:00Z", "fim": "2026-06-15T18:00:00Z", "localId": "00000000-0000-0000-0000-000000000002", "descricao": "Encontro anual de planejamento e espiritualidade."}', NULL, 0
    ),
    (
        '60000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000001', 'EDICAO_EVENTO', 'PAROCO', 'PENDENTE', '2026-05-25T13:00:00Z', 
        NULL, 'joao.liturgia', NULL, NULL, 
        '{"titulo": "Missa de Abertura da Catequese (Novo Horário)", "inicio": "2026-02-07T17:00:00Z", "fim": "2026-02-07T18:30:00Z", "localId": "00000000-0000-0000-0000-000000000001"}', NULL, 0
    ),
    (
        '60000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000101', 'CANCELAMENTO', 'CONSELHO_COORDENADOR', 'PENDENTE', '2026-05-25T14:00:00Z', 
        NULL, 'maria.catequese', NULL, NULL, 
        '{"motivo": "Falta de energia no Salão Paroquial"}', NULL, 0
    ),
    (
        '60000000-0000-0000-0000-000000000004', '40000000-0000-0000-0000-000000000001', 'CRIACAO_EVENTO', 'PAROCO', 'APROVADA', '2026-01-15T08:00:00Z', 
        '2026-01-16T09:00:00Z', 'padre.pedro', '10000000-0000-0000-0000-000000000001', 'Missa importante, autorizada.', 
        '{"titulo": "Missa de Abertura da Catequese"}', NULL, 0
    ),
    (
        '60000000-0000-0000-0000-000000000005', NULL, 'EDICAO_EVENTO', 'PAROCO', 'REPROVADA', '2026-02-09T10:00:00Z', 
        '2026-02-10T11:00:00Z', 'joao.liturgia', '10000000-0000-0000-0000-000000000001', 'Horário conflita com casamento agendado.', 
        '{"titulo": "Missa de Abertura (Horário Conflitante)", "inicio": "2026-02-07T19:00:00Z"}', NULL, 0
    ),
    (
        '60000000-0000-0000-0000-000000000006', NULL, 'CRIACAO_EVENTO', 'PAROCO', 'FALHA_EXECUCAO', '2026-02-07T10:00:00Z', 
        '2026-02-07T10:05:00Z', 'joao.liturgia', '10000000-0000-0000-0000-000000000001', 'Tentativa de aprovação automática falhou.', 
        '{"titulo": "Missa Festiva Pascom", "inicio": "2026-02-07T18:00:00Z", "fim": "2026-02-07T19:30:00Z", "localId": "00000000-0000-0000-0000-000000000001"}', 'Conflito de horário detectado no local Igreja Principal.', 0
    );

-- Observations
INSERT INTO calendario.observacoes_evento (id, evento_id, usuario_id, tipo, conteudo, criado_em_utc, version)
VALUES 
    (gen_random_uuid(), '40000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000002', 'NOTA', 'Lembrar de avisar os coroinhas sobre a missa.', '2026-02-01T14:00:00Z', 0);

-- Audit Trail
INSERT INTO calendario.auditoria_operacao (id, aggregate_id, aggregate_type, usuario_id, operacao, timestamp_utc, payload_antes, payload_depois)
VALUES 
    (gen_random_uuid(), '40000000-0000-0000-0000-000000000001', 'EVENTO', '10000000-0000-0000-0000-000000000002', 'CRIACAO', '2026-01-15T08:00:00Z', NULL, '{"titulo": "Missa de Abertura"}'),
    (gen_random_uuid(), '40000000-0000-0000-0000-000000000001', 'EVENTO', '10000000-0000-0000-0000-000000000001', 'APROVACAO', '2026-01-16T09:00:00Z', '{"status": "PENDENTE"}', '{"status": "CONFIRMADO"}');
