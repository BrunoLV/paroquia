-- NS Fatima Calendar MVP - Seed Data
-- Este script deve ser executado APÓS as migrações do Flyway.

-- Limpa dados existentes para evitar conflitos em re-execuções
TRUNCATE calendario.auditoria_operacoes, calendario.observacoes_evento, calendario.aprovacoes, 
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
INSERT INTO calendario.organizacoes (id, nome, tipo, contato, ativo, version)
VALUES 
    ('00000000-0000-0000-0000-000000000001', 'Paróquia Nossa Senhora de Fátima', 'CLERO', 'contato@nsfatima.com', TRUE, 0),
    ('00000000-0000-0000-0000-0000000000aa', 'Pastoral da Comunicação', 'PASTORAL', 'pascom@nsfatima.com', TRUE, 0),
    ('00000000-0000-0000-0000-0000000000bb', 'Pastoral da Juventude', 'PASTORAL', 'juventude@nsfatima.com', TRUE, 0),
    ('00000000-0000-0000-0000-0000000000cc', 'Conselho Pastoral Paroquial', 'CONSELHO', 'cpp@nsfatima.com', TRUE, 0),
    ('00000000-0000-0000-0000-0000000000dd', 'Clero Paroquial', 'CLERO', 'clero@nsfatima.com', TRUE, 0),
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
    (2025, 'CONCLUIDO', '2025-01-01T00:00:00Z'),
    (2026, 'EM_PLANEJAMENTO', NULL),
    (2027, 'EM_PLANEJAMENTO', NULL)
ON CONFLICT (ano) DO UPDATE SET status = EXCLUDED.status, data_fechamento_utc = EXCLUDED.data_fechamento_utc;

-- Projects (Projetos)
INSERT INTO calendario.projetos_eventos (id, nome, descricao, status, organizacao_responsavel_id, inicio_utc, fim_utc, version)
VALUES 
    ('30000000-0000-0000-0000-000000000001', 'Catequese 2026', 'Ano letivo da catequese infantil', 'ATIVO', '00000000-0000-0000-0000-0000000000ee', '2026-02-01T00:00:00Z', '2026-12-15T23:59:59Z', 0),
    ('30000000-0000-0000-0000-000000000002', 'Festividades de Maio', 'Mês de Nossa Senhora', 'ATIVO', '00000000-0000-0000-0000-0000000000dd', '2026-05-01T00:00:00Z', '2026-05-31T23:59:59Z', 0);

-- Events (Eventos)
INSERT INTO calendario.eventos (id, titulo, descricao, organizacao_responsavel_id, local_id, categoria, status, inicio_utc, fim_utc, projeto_id, version)
VALUES 
    ('40000000-0000-0000-0000-000000000001', 'Missa de Abertura da Catequese', 'Missa com pais e alunos', '00000000-0000-0000-0000-0000000000ee', '00000000-0000-0000-0000-000000000001', 'LITURGICO', 'CONFIRMADO', '2026-09-07T18:00:00Z', '2026-09-07T19:30:00Z', '30000000-0000-0000-0000-000000000001', 0);

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

-- Audit Trail
INSERT INTO calendario.auditoria_operacoes (id, organizacao_id, evento_id, recurso_tipo, recurso_id, acao, resultado, ator, ator_usuario_id, correlation_id, detalhes_auditaveis_json, ocorrido_em_utc)
VALUES 
    (gen_random_uuid(), '00000000-0000-0000-0000-0000000000ee', '40000000-0000-0000-0000-000000000001', 'EVENTO', '40000000-0000-0000-0000-000000000001', 'CRIACAO', 'SUCCESS', 'maria.catequese', '10000000-0000-0000-0000-000000000002', '60000000-0000-0000-0000-000000000004', '{"titulo": "Missa de Abertura da Catequese"}', '2026-01-15T08:00:00Z'),
    (gen_random_uuid(), '00000000-0000-0000-0000-0000000000ee', '40000000-0000-0000-0000-000000000001', 'EVENTO', '40000000-0000-0000-0000-000000000001', 'APROVACAO', 'SUCCESS', 'padre.pedro', '10000000-0000-0000-0000-000000000001', '60000000-0000-0000-0000-000000000004', '{"status": "CONFIRMADO"}', '2026-01-16T09:00:00Z');
