# Track Specification: Vinculação de Locais a Matriz/Comunidades (`vinculo_locais_matriz_comunidades`)

## Overview
Atualmente, a entidade `Local` (Espaço Paroquial) é cadastrada no sistema sem um vínculo explícito de subordinação territorial/eclesial. Esta track refatora o modelo de dados, API backend (`gestao-paroquia`), scripts de seed do Docker e a interface frontend (`gestao-paroquia-ui`) para associar obrigatoriamente cada `Local` a uma Organização de subordinação — seja ela a Matriz (Paróquia) ou uma Comunidade subordinada (`organizacao_id` / `organizacaoId`).

## Functional Requirements
1. **Modelo de Dados & Migração (Database & Backend Entity):**
   - Criar migração Flyway `V048__add_organizacao_id_to_locais.sql` adicionando a coluna `organizacao_id UUID` na tabela `calendario.locais`, com chave estrangeira apontando para `calendario.organizacoes(id)`.
   - Atualizar a entidade JPA `LocalEntity` e o modelo de domínio `Local` para incluir os campos `organizacaoId` (UUID) e `organizacaoNome` (String).
2. **DTOs & API REST (`gestao-paroquia`):**
   - Atualizar `LocalRequest` e `LocalResponse` para trafegar `organizacaoId` e `organizacaoNome`.
   - Atualizar os mappers, repositories e `LocalService` para persistir e consultar o vínculo com a Organização (Matriz/Comunidade).
   - Permitir a filtragem de locais por `organizacaoId` na listagem via endpoint `/api/v1/locais`.
3. **Scripts de Seed Docker (`gestao-paroquia` / Docker):**
   - Atualizar os scripts de carga/seed (`demo_mvp.sh` / scripts de inicialização) para que todos os locais preexistentes sejam devidamente vinculados à Matriz e Comunidades ativas.
4. **Interface do Usuário (`gestao-paroquia-ui`):**
   - Atualizar os tipos TypeScript (`Local`, `LocalFormValues`) em `src/api/types.ts`.
   - Atualizar o modal `LocalFormModal.tsx` para exibir um campo de seleção (Select) de Organizações (Matriz / Comunidades) ativas para subordinação do local.
   - Exibir no grid/tabela de Locais em `Settings.tsx` a coluna "Matriz / Comunidade" informando a subordinação.
5. **Governança & Escopo Territorial:**
   - Garantir que o cálculo de disponibilidade de locais e atribuição em eventos respeite o escopo territorial/organizacional configurado.

## Non-Functional Requirements & Code Standards
- Adimplência estrita às diretrizes do `AGENTS.md` (TDD com Vitest e JUnit, Clean Architecture / Hexagonal em DDD).
- Cobertura de testes unitários e de integração > 80% nos componentes e use cases alterados.

## Acceptance Criteria
- [ ] Nova migração Flyway executada com sucesso adicionando `organizacao_id` em `calendario.locais`.
- [ ] Endpoints de CRUD de Locais recebem e retornam `organizacaoId` e `organizacaoNome`.
- [ ] Scripts de Seed do Docker atualizados para refletir a nova estrutura de dados com `organizacao_id`.
- [ ] `LocalFormModal.tsx` permite selecionar a Matriz/Comunidade ao criar/editar um local.
- [ ] Tabela de gestão de locais em `Settings.tsx` lista a Matriz/Comunidade associada a cada local.
- [ ] Todos os testes unitários e de integração no backend (`./gradlew test`) e frontend (`npm test`) executam com 100% de aprovação em verde.

## Out of Scope
- Alterações no fluxo de aprovação de eventos por prioridade de agenda (já coberto em tracks anteriores).
