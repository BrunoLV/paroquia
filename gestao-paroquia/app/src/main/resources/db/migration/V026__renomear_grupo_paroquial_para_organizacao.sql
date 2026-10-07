-- Alinha o vocabulário persistido ao conceito eclesial "Organização" (pastorais, movimentos, serviços e conselhos).
-- Renomeações preservam dados, FKs e índices (PostgreSQL mantém os nomes de constraints/índices antigos).
ALTER TABLE calendario.grupo_paroquial RENAME TO organizacao;
ALTER TABLE calendario.membro_grupo RENAME COLUMN grupo_paroquial_id TO organizacao_id;
ALTER TABLE calendario.membro_grupo RENAME COLUMN tipo_grupo TO tipo_organizacao;
ALTER TABLE calendario.locais RENAME COLUMN grupo_paroquial_id TO organizacao_id;
ALTER TABLE calendario.projetos_eventos RENAME COLUMN grupo_paroquial_responsavel_id TO organizacao_responsavel_id;
ALTER TABLE calendario.projeto_solicitacoes_aprovacao RENAME COLUMN solicitante_tipo_grupo_paroquial TO solicitante_tipo_organizacao;
ALTER TABLE calendario.aprovacoes RENAME COLUMN solicitante_tipo_grupo_paroquial TO solicitante_tipo_organizacao;
ALTER TABLE calendario.auditoria_operacoes RENAME COLUMN grupo_paroquial_id TO organizacao_id;
ALTER TABLE calendario.eventos RENAME COLUMN grupo_paroquial_responsavel_id TO organizacao_responsavel_id;
ALTER TABLE calendario.eventos_envolvidos RENAME COLUMN grupo_paroquial_id TO organizacao_id;
ALTER TABLE calendario.ativos_patrimoniais RENAME COLUMN grupo_paroquial_id TO organizacao_id;

COMMENT ON TABLE calendario.organizacao IS 'Organismos eclesiais da paróquia: pastorais, movimentos, serviços e conselhos; distintos das unidades territoriais. Não possuem personalidade jurídica nem caixa próprios.';
