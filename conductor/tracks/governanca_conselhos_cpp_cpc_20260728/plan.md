# Implementation Plan: Governança dos Conselhos (CPP e CPC) e Homologação de Atas

## Phase 1: Backend - Banco de Dados, Domínio e Endpoints REST
- [ ] Task: Criar script Flyway SQL `V047__create_cpc_decisoes_table.sql`.
- [ ] Task: Criar entidade `CpcDecisaoEntity`, enum `StatusHomologacao` (`PENDENTE_HOMOLOGACAO`, `HOMOLOGADO`, `VETADO`) e `CpcDecisaoJpaRepository`.
- [ ] Task: Criar Mappers e DTOs (`CreateCpcDecisaoRequest`, `HomologarCpcDecisaoRequest`, `CpcDecisaoResponse`).
- [ ] Task: Criar `CreateCpcDecisaoUseCase`, `HomologarCpcDecisaoUseCase` e `ListCpcDecisoesUseCase`.
- [ ] Task: Criar Controller REST `CpcDecisaoController` (`/api/v1/conselhos/decisoes`) com anotações `@PreAuthorize`.
- [ ] Task: Unit & Integration Tests para o backend (`./gradlew test`).

## Phase 2: Frontend (UI) - Tela de Atas, Modais e Workflow de Homologação
- [ ] Task: Criar cliente de API `conselhosApi.ts` e tipos TypeScript para decisões dos conselhos.
- [ ] Task: Criar página `ConselhosAtas.tsx` com tabela de atas, badges de status, ordenação e filtro por comunidade.
- [ ] Task: Criar modal `CpcDecisaoModal.tsx` para submissão de novas atas/decisões pelos membros do CPC.
- [ ] Task: Criar modal de Homologação / Veto com justificativa obrigatória e atalhos de ação para Pároco/Mesa CPP.
- [ ] Task: Adicionar rota `/conselhos` e item no menu lateral (`Layout.tsx`).
- [ ] Task: Vitest Unit & Component Tests no frontend (`npm test`).

## Phase 3: Validação E2E e Checkpoint
- [ ] Task: Executar suíte completa de verificação no monorepo (`./gradlew test` e `npm test`).
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md).
