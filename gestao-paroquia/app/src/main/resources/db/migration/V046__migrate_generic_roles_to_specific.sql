-- V046__migrate_generic_roles_to_specific.sql
-- Migration script to convert generic roles (coordenador, vice-coordenador, membro) to specific eclesial roles

UPDATE calendario.membros
SET papel = 'coordenador-pastoral-local'
WHERE papel IN ('coordenador', 'vice-coordenador');

UPDATE calendario.membros
SET papel = 'voluntario-pastoral-local'
WHERE papel = 'membro';

UPDATE calendario.usuarios
SET roles = REPLACE(roles, 'coordenador', 'coordenador-pastoral-local')
WHERE roles LIKE '%coordenador%';

UPDATE calendario.usuarios
SET roles = REPLACE(roles, 'membro', 'voluntario-pastoral-local')
WHERE roles LIKE '%membro%';
