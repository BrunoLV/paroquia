# Implementation Plan: Vinculação de Locais a Matriz/Comunidades

Refatorar a entidade `Local` na API (`gestao-paroquia`), no banco PostgreSQL (via Flyway) e na interface Web (`gestao-paroquia-ui`) para associar cada local a uma Organização (Matriz ou Comunidade), atualizando inclusive os scripts de seed do Docker.

## Phase 1: Database Migration & Seed Data Updates

- [x] Task: Criar migração Flyway e ajustar scripts de Seed do Docker a188cbf
    - [x] Criar arquivo `V048__add_organizacao_id_to_locais.sql` adicionando a coluna `organizacao_id UUID REFERENCES calendario.organizacoes(id)`.
    - [x] Atualizar scripts/arquivos de seed Docker vinculando os locais iniciais à Matriz e Comunidades de teste.
- [x] Task: Conductor - User Manual Verification 'Phase 1: Database Migration & Seed Data Updates' (Protocol in workflow.md)

## Phase 2: Backend Domain & API Implementation (`gestao-paroquia`)

- [ ] Task: Escrever testes unitários e de integração que falham para Local com Organização (TDD Red)
    - [ ] Atualizar testes em `LocalServiceTest`, `LocalControllerTest` e repositórios com asserções para `organizacaoId` e `organizacaoNome`.
    - [ ] Executar os testes e confirmar que falham conforme esperado.
- [ ] Task: Implementar alterações no domínio, persistência e endpoints REST (TDD Green)
    - [ ] Atualizar o modelo de domínio `Local` e a entidade `LocalEntity` para suportar `organizacaoId`.
    - [ ] Atualizar DTOs `LocalRequest` e `LocalResponse` e seus mappers.
    - [ ] Atualizar `LocalService` e `LocalController` para persistência e filtragem por `organizacaoId`.
    - [ ] Reexecutar a suíte de testes do backend (`./gradlew test`) e garantir 100% de aprovação em verde.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Backend Domain & API Implementation' (Protocol in workflow.md)

## Phase 3: Frontend Interface & Types Implementation (`gestao-paroquia-ui`)

- [ ] Task: Escrever testes unitários para a interface de Locais (TDD Red)
    - [ ] Atualizar os testes em `src/api/locais.test.ts` e `src/pages/Settings.test.tsx` para validação do vínculo de Organização.
    - [ ] Confirmar que os testes falham.
- [ ] Task: Implementar formulário e tabela de Locais com vínculo de Organização (TDD Green)
    - [ ] Atualizar as interfaces TypeScript em `src/api/types.ts` e `src/api/locais.ts`.
    - [ ] Atualizar `LocalFormModal.tsx` adicionando Select de Organizações (Matriz / Comunidades).
    - [ ] Atualizar a tabela de locais em `src/pages/Settings.tsx` exibindo a coluna "Matriz / Comunidade".
    - [ ] Reexecutar os testes do frontend (`npm test`) e garantir 100% de aprovação em verde.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Frontend Interface & Types Implementation' (Protocol in workflow.md)

## Phase 4: Full System Integration & Final Quality Check

- [ ] Task: Executar suítes completas de testes e verificação de não-regressão
    - [ ] Executar build e testes do backend (`./gradlew check`).
    - [ ] Executar testes do frontend (`npm test -- --run`).
    - [ ] Validar conformidade com as regras do `AGENTS.md`.
- [ ] Task: Conductor - User Manual Verification 'Phase 4: Full System Integration & Final Quality Check' (Protocol in workflow.md)
