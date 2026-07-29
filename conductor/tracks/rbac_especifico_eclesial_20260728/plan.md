# Implementation Plan: RBAC Eclesial Específico (Eliminação de Papéis Genéricos)

## Phase 1: Domínio e Enumeração de Papéis
- [x] Task: Atualizar `PapelOrganizacional.java` com novos papéis específicos e remover papéis genéricos.
- [x] Task: Atualizar `TipoOrganizacao.java` para incluir `CPP` e `CPC`.
- [x] Task: Unit Tests para os enums e métodos de conversão.

## Phase 2: Refatoração dos Serviços de Autorização e Casos de Uso
- [x] Task: Refatorar `UserScopeAuthorizationService` para checar papéis específicos por escopo.
- [x] Task: Refatorar `AuthorizationPolicy` e `EventoPatchAuthorizationService`.
- [x] Task: Refatorar `ValidateAprovacaoUseCase` e `AddMembershipUseCase`.
- [x] Task: Unit & Integration Tests para autorização.

## Phase 3: Migração de Dados e Validação E2E
- [x] Task: Criar script Flyway SQL `V046__migrate_generic_roles_to_specific.sql`.
- [x] Task: Atualizar controllers e DTOs para rejeitar papéis genéricos.
- [x] Task: Executar suite completa de testes (`./gradlew test`).
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md).

