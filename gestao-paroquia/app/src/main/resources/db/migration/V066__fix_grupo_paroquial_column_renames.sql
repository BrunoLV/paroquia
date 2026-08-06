-- Corrige a nomenclatura de colunas deixada pendente pela renomeação Organizacao -> GrupoParoquial (track
-- linguagem_ubiqua_dominio): as entidades JPA já mapeiam essas colunas com o novo nome, mas o schema físico
-- ainda usava "organizacao_*". Também ajusta/remove FKs que se tornaram inválidas com o split
-- GrupoParoquial/UnidadeTerritorial: algumas colunas passaram a ser referências polimórficas (podem apontar
-- tanto para grupo_paroquial quanto para unidade_territorial, dependendo do caso) e não há mais uma única
-- tabela-alvo possível para uma FK simples; outras passaram a apontar exclusivamente para unidade_territorial
-- (Matriz/Comunidade).

-- 1. eventos.organizacao_responsavel_id: alvo agora é polimórfico (GrupoParoquial para grupos pastorais,
--    UnidadeTerritorial para solenidades litúrgicas da Matriz/Comunidade). A integridade referencial passa a
--    ser garantida em nível de aplicação (repositórios consultados antes de persistir).
ALTER TABLE calendario.eventos DROP CONSTRAINT IF EXISTS fk_eventos_organizacao;
ALTER TABLE calendario.eventos RENAME COLUMN organizacao_responsavel_id TO grupo_paroquial_responsavel_id;

-- 2. eventos.igreja_vinculo_id: sempre uma UnidadeTerritorial (Matriz/Comunidade) — redireciona a FK.
ALTER TABLE calendario.eventos ADD COLUMN igreja_vinculo_id_new UUID REFERENCES calendario.unidade_territorial(id);
UPDATE calendario.eventos SET igreja_vinculo_id_new = igreja_vinculo_id;
ALTER TABLE calendario.eventos DROP COLUMN igreja_vinculo_id;
ALTER TABLE calendario.eventos RENAME COLUMN igreja_vinculo_id_new TO igreja_vinculo_id;

-- 3. eventos_envolvidos.organizacao_id: alvo polimórfico (grupo pastoral ou unidade territorial envolvida).
ALTER TABLE calendario.eventos_envolvidos DROP CONSTRAINT IF EXISTS fk_eventos_envolvidos_organizacao;
ALTER TABLE calendario.eventos_envolvidos RENAME COLUMN organizacao_id TO grupo_paroquial_id;

-- 4. locais.organizacao_id: sempre uma UnidadeTerritorial (Matriz/Comunidade) — redireciona a FK.
ALTER TABLE calendario.locais ADD COLUMN grupo_paroquial_id UUID REFERENCES calendario.unidade_territorial(id);
UPDATE calendario.locais SET grupo_paroquial_id = organizacao_id;
ALTER TABLE calendario.locais DROP COLUMN organizacao_id;

-- 5. grupo_paroquial.parent_id: alvo polimórfico (UnidadeTerritorial para CONSELHO, GrupoParoquial para
--    PASTORAL/MOVIMENTO) — a validação de hierarquia já é feita em GrupoParoquialService.validateHierarchy,
--    não há mais uma única tabela-alvo possível para a FK.
ALTER TABLE calendario.grupo_paroquial ADD COLUMN parent_id_new UUID;
UPDATE calendario.grupo_paroquial SET parent_id_new = parent_id;
ALTER TABLE calendario.grupo_paroquial DROP COLUMN parent_id;
ALTER TABLE calendario.grupo_paroquial RENAME COLUMN parent_id_new TO parent_id;

-- 6. grupo_paroquial.igreja_vinculo_id: coluna nunca mapeada por nenhuma entidade JPA (morta desde a
--    criação em V038) — remove.
ALTER TABLE calendario.grupo_paroquial DROP COLUMN IF EXISTS igreja_vinculo_id;

-- 7. patrimonio: colunas sem FK, apenas renomear para o nome que as entidades já esperam.
ALTER TABLE calendario.ativos_patrimoniais RENAME COLUMN organizacao_id TO grupo_paroquial_id;
ALTER TABLE calendario.alocacoes_ativos RENAME COLUMN organizacao_origem_id TO grupo_paroquial_origem_id;
ALTER TABLE calendario.alocacoes_ativos RENAME COLUMN organizacao_destino_id TO grupo_paroquial_destino_id;

-- 8. auditoria_operacoes: sem FK, apenas renomear.
ALTER TABLE calendario.auditoria_operacoes RENAME COLUMN organizacao_id TO grupo_paroquial_id;

-- 9. projetos_eventos: sem FK, apenas renomear.
ALTER TABLE calendario.projetos_eventos RENAME COLUMN organizacao_responsavel_id TO grupo_paroquial_responsavel_id;
