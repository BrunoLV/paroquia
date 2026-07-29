# Implementation Plan: Governança dos Conselhos (CPP e CPC) e Homologação de Atas

## Phase 1: Banco de Dados e Entidades
- [ ] Task: Criar script Flyway SQL `V042__create_cpc_decisoes_table.sql`.
- [ ] Task: Criar entidade `CpcDecisaoEntity`, enum `StatusHomologacao` e `CpcDecisaoJpaRepository`.
- [ ] Task: Mapers e DTOs de requisição/resposta.

## Phase 2: Casos de Uso e Workflow de Homologação
- [ ] Task: Criar `CreateCpcDecisaoUseCase` atribuindo status `PENDENTE_HOMOLOGACAO`.
- [ ] Task: Criar `HomologarCpcDecisaoUseCase` validando permissão do `PAROCO` / `CPP`.
- [ ] Task: Controller REST `CpcDecisaoController` com documentação OpenAPI.
- [ ] Task: Unit & Integration Tests para o fluxo de homologação.

## Phase 3: Validação E2E
- [ ] Task: Executar suite completa de testes (`./gradlew test`).
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md).
