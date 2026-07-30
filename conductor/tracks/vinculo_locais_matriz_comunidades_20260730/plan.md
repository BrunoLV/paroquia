# Implementation Plan: Vinculação de Locais a Matriz/Comunidades

Refatorar a entidade `Local` na API (`gestao-paroquia`), no banco PostgreSQL (via Flyway) e na interface Web (`gestao-paroquia-ui`) para associar cada local a uma Organização (Matriz ou Comunidade), atualizando inclusive os scripts de seed do Docker.

## Phase 1: Database Migration & Seed Data Updates

- [x] Task: Criar migração Flyway e ajustar scripts de Seed do Docker a188cbf
    - [x] Criar arquivo `V048__add_organizacao_id_to_locais.sql` adicionando a coluna `organizacao_id UUID REFERENCES calendario.organizacoes(id)`.
    - [x] Atualizar scripts/arquivos de seed Docker vinculando os locais iniciais à Matriz e Comunidades de teste.
- [x] Task: Conductor - User Manual Verification 'Phase 1: Database Migration & Seed Data Updates' (Protocol in workflow.md)

## Phase 2: Backend Domain & API Implementation (`gestao-paroquia`)

- [x] Task: Escrever testes unitários e de integração que falham para Local com Organização (TDD Red) 73ebb53
    - [x] Atualizar testes em `LocalServiceTest`, `LocalControllerTest` e repositórios com asserções para `organizacaoId` e `organizacaoNome`.
    - [x] Executar os testes e confirmar que falham conforme esperado.
- [x] Task: Implementar alterações no domínio, persistência e endpoints REST (TDD Green) 73ebb53
    - [x] Atualizar o modelo de domínio `Local` e a entidade `LocalEntity` para suportar `organizacaoId`.
    - [x] Atualizar DTOs `LocalRequest` e `LocalResponse` e seus mappers.
    - [x] Atualizar `LocalService` e `LocalController` para persistência e filtragem por `organizacaoId`.
    - [x] Reexecutar a suíte de testes do backend (`./gradlew test`) e garantir 100% de aprovação em verde.
- [x] Task: Conductor - User Manual Verification 'Phase 2: Backend Domain & API Implementation' (Protocol in workflow.md)

## Phase 3: Frontend Interface & Types Implementation (`gestao-paroquia-ui`)

- [x] Task: Escrever testes unitários para a interface de Locais (TDD Red) 9c70e2f
    - [x] Atualizar os testes em `LocalFormModal.test.tsx` / `Settings.test.tsx` / `locais.test.ts` para cobrir o campo `organizacaoId` / `organizacaoNome`.
- [x] Task: Implementar formulário e tabela de Locais com vínculo de Organização (TDD Green) 9c70e2f
    - [x] Atualizar a interface `Local` em `types.ts`.
    - [x] Adicionar o componente `<Select>` para escolha da Matriz ou Comunidade (`organizacaoId`) em `LocalFormModal.tsx`.
    - [x] Adicionar a coluna "Matriz / Comunidade" (`organizacaoNome`) na tabela de Locais em `Settings.tsx`.
    - [x] Executar a suíte de testes do frontend (`npm test -- --run`) e garantir aprovação em verde.
- [x] Task: Conductor - User Manual Verification 'Phase 3: Frontend Interface & Types Implementation' (Protocol in workflow.md)

## Phase 4: Full System Integration & Final Quality Check

- [ ] Task: Executar suítes completas de testes e verificação de não-regressão
    - [ ] Executar build e testes do backend (`./gradlew check`).
    - [ ] Executar testes do frontend (`npm test -- --run`).
    - [ ] Validar conformidade com as regras do `AGENTS.md`.
- [ ] Task: Conductor - User Manual Verification 'Phase 4: Full System Integration & Final Quality Check' (Protocol in workflow.md)
