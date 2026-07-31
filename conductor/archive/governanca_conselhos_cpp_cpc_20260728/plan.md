# Implementation Plan: Governança dos Conselhos (CPP e CPC) e Homologação de Atas

## Phase 1: Backend - Banco de Dados, Domínio e Endpoints REST
- [x] Task: Criar script Flyway SQL `V047__create_cpc_decisoes_table.sql`.
- [x] Task: Criar entidade `CpcDecisaoEntity`, enum `StatusHomologacao` (`PENDENTE_HOMOLOGACAO`, `HOMOLOGADO`, `VETADO`) e `CpcDecisaoJpaRepository`.
- [x] Task: Criar Mappers e DTOs (`CreateCpcDecisaoRequest`, `HomologarCpcDecisaoRequest`, `CpcDecisaoResponse`).
- [x] Task: Criar `CreateCpcDecisaoUseCase`, `HomologarCpcDecisaoUseCase` e `ListCpcDecisoesUseCase`.
- [x] Task: Criar Controller REST `CpcDecisaoController` (`/api/v1/conselhos/decisoes`) com anotações `@PreAuthorize`.
- [x] Task: Unit & Integration Tests para o backend (`./gradlew test`).

## Phase 2: Frontend (UI) - Tela de Atas, Modais e Workflow de Homologação
- [x] Task: Criar cliente de API `conselhosApi.ts` e tipos TypeScript para decisões dos conselhos.
- [x] Task: Criar página `ConselhosAtas.tsx` com tabela de atas, badges de status, ordenação e filtro por comunidade.
- [x] Task: Criar modal `CpcDecisaoModal.tsx` para submissão de novas atas/decisões pelos membros do CPC.
- [x] Task: Criar modal de Homologação / Veto com justificativa obrigatória e atalhos de ação para Pároco/Mesa CPP.
- [x] Task: Adicionar rota `/conselhos` e item no menu lateral (`Layout.tsx`).
- [x] Task: Vitest Unit & Component Tests no frontend (`npm test`).

## Phase 3: Validação E2E e Checkpoint
- [x] Task: Executar suíte completa de verificação no monorepo (`./gradlew test` e `npm test`).
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md).
