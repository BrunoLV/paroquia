DELETE FROM calendario.eventos_envolvidos;
DELETE FROM calendario.aprovacoes;
DELETE FROM calendario.observacoes_evento;
DELETE FROM calendario.auditoria_operacoes;
DELETE FROM calendario.eventos;
DELETE FROM calendario.eventos_recorrencia;
DELETE FROM calendario.projetos_eventos;
DELETE FROM calendario.membro_grupo;
DELETE FROM calendario.papel_global;
DELETE FROM calendario.usuarios;
DELETE FROM calendario.fieis;
DELETE FROM calendario.locais;
DELETE FROM calendario.grupo_paroquial;
DELETE FROM calendario.unidade_territorial;
DELETE FROM calendario.pastorais_movimentos_oficiais;

-- Clero D (dd) e Matriz (01/100) são UnidadesTerritoriais (track linguagem_ubiqua_dominio)
INSERT INTO calendario.unidade_territorial (id, nome, tipo)
VALUES ('00000000-0000-0000-0000-0000000000dd', 'Clero D', 'CLERO'),
       ('00000000-0000-0000-0000-000000000001', 'Igreja Default', 'MATRIZ'),
       ('00000000-0000-0000-0000-000000000100', 'Igreja Matriz', 'MATRIZ');

INSERT INTO calendario.grupo_paroquial (id, nome, tipo)
VALUES ('00000000-0000-0000-0000-000000000001', 'Organizacao Default', 'MINISTERIO'),
       ('00000000-0000-0000-0000-0000000000aa', 'Pastoral A', 'PASTORAL'),
       ('00000000-0000-0000-0000-0000000000bb', 'Pastoral B', 'PASTORAL'),
       ('00000000-0000-0000-0000-0000000000cc', 'Conselho C', 'CONSELHO');

INSERT INTO calendario.fieis (id, nome, cpf, cpf_hash, data_nascimento, email, telefone, endereco, vocacao,
                              ativo, criado_em_utc, atualizado_em_utc, criado_por, atualizado_por)
VALUES ('f0000000-0000-0000-0000-000000000001', 'Joao Silva', '11111111111',
        '1111111111111111111111111111111111111111111111111111111111111111', '1990-01-01',
        'joao.silva@example.com', '11911111111', 'Rua Fixture, 1', 'LEIGO', TRUE, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP, 'fixture', 'fixture'),
       ('f0000000-0000-0000-0000-000000000002', 'Maria Secretaria', '22222222222',
        '2222222222222222222222222222222222222222222222222222222222222222', '1990-01-01',
        'maria.secretaria@example.com', '11922222222', 'Rua Fixture, 2', 'LEIGO', TRUE, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP, 'fixture', 'fixture'),
       ('f0000000-0000-0000-0000-000000000003', 'Pedro Membro', '33333333333',
        '3333333333333333333333333333333333333333333333333333333333333333', '1990-01-01',
        'pedro.membro@example.com', '11933333333', 'Rua Fixture, 3', 'LEIGO', TRUE, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP, 'fixture', 'fixture'),
       ('f0000000-0000-0000-0000-000000000004', 'Ana Conselho', '44444444444',
        '4444444444444444444444444444444444444444444444444444444444444444', '1990-01-01',
        'ana.conselho@example.com', '11944444444', 'Rua Fixture, 4', 'LEIGO', TRUE, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP, 'fixture', 'fixture'),
       ('f0000000-0000-0000-0000-000000000005', 'Usuario Inativo', '55555555555',
        '5555555555555555555555555555555555555555555555555555555555555555', '1990-01-01',
        'usuario.inativo@example.com', '11955555555', 'Rua Fixture, 5', 'LEIGO', TRUE, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP, 'fixture', 'fixture'),
       ('f0000000-0000-0000-0000-000000000006', 'Clara Invalida', '66666666666',
        '6666666666666666666666666666666666666666666666666666666666666666', '1990-01-01',
        'clara.invalida@example.com', '11966666666', 'Rua Fixture, 6', 'LEIGO', TRUE, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP, 'fixture', 'fixture'),
       ('f0000000-0000-0000-0000-000000000007', 'Sem Vinculo', '77777777777',
        '7777777777777777777777777777777777777777777777777777777777777777', '1990-01-01',
        'sem.vinculo@example.com', '11977777777', 'Rua Fixture, 7', 'LEIGO', TRUE, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP, 'fixture', 'fixture');

INSERT INTO calendario.usuarios (id, username, password_hash, enabled, fiel_id, criado_em_utc,
                                  atualizado_em_utc)
VALUES ('00000000-0000-0000-0000-000000000001', 'joao.silva', '{noop}senha123', TRUE,
        'f0000000-0000-0000-0000-000000000001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('00000000-0000-0000-0000-000000000002', 'maria.secretaria', '{noop}senha123', TRUE,
        'f0000000-0000-0000-0000-000000000002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('00000000-0000-0000-0000-000000000003', 'pedro.membro', '{noop}senha123', TRUE,
        'f0000000-0000-0000-0000-000000000003', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('00000000-0000-0000-0000-000000000004', 'ana.conselho', '{noop}senha123', TRUE,
        'f0000000-0000-0000-0000-000000000004', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('00000000-0000-0000-0000-000000000005', 'usuario.inativo', '{noop}senha123', FALSE,
        'f0000000-0000-0000-0000-000000000005', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('00000000-0000-0000-0000-000000000006', 'clara.invalida', '{noop}senha123', TRUE,
        'f0000000-0000-0000-0000-000000000006', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('00000000-0000-0000-0000-000000000007', 'sem.vinculo', '{noop}senha123', TRUE,
        'f0000000-0000-0000-0000-000000000007', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO calendario.membro_grupo (id, fiel_id, grupo_paroquial_id, tipo_grupo, papel, ativo,
                                      criado_em_utc, atualizado_em_utc)
VALUES ('10000000-0000-0000-0000-000000000001', 'f0000000-0000-0000-0000-000000000001',
        '00000000-0000-0000-0000-0000000000aa', 'PASTORAL', 'coordenador-pastoral-local', TRUE,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('10000000-0000-0000-0000-000000000002', 'f0000000-0000-0000-0000-000000000002',
        '00000000-0000-0000-0000-0000000000cc', 'CONSELHO', 'secretario', TRUE,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('10000000-0000-0000-0000-000000000003', 'f0000000-0000-0000-0000-000000000003',
        '00000000-0000-0000-0000-0000000000aa', 'PASTORAL', 'voluntario-pastoral-local', TRUE,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('10000000-0000-0000-0000-000000000004', 'f0000000-0000-0000-0000-000000000004',
        '00000000-0000-0000-0000-0000000000cc', 'CONSELHO', 'presidente-cpc', TRUE,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('10000000-0000-0000-0000-000000000005', 'f0000000-0000-0000-0000-000000000004',
        '00000000-0000-0000-0000-0000000000bb', 'PASTORAL', 'coordenador-pastoral-local', TRUE,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       -- Clara Invalida: combinação papel/tipo deliberadamente incompatível (presidente-cpc só é válido
       -- para CONSELHO, não para PASTORAL) — usada para testar o erro ROLE_SCOPE_INVALID.
       ('10000000-0000-0000-0000-000000000006', 'f0000000-0000-0000-0000-000000000006',
        '00000000-0000-0000-0000-0000000000aa', 'PASTORAL', 'presidente-cpc', TRUE,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       ('10000000-0000-0000-0000-000000000007', 'f0000000-0000-0000-0000-000000000007',
        '00000000-0000-0000-0000-0000000000aa', 'PASTORAL', 'voluntario-pastoral-local', TRUE,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
