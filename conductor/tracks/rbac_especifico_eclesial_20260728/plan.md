# Implementation Plan: RBAC Eclesial Específico (Eliminação de Papéis Genéricos)

## Phase 1: Domínio e Enumeração de Papéis
- [ ] Task: Atualizar `PapelOrganizacional.java` com novos papéis específicos e remover papéis genéricos.
- [ ] Task: Atualizar `TipoOrganizacao.java` para incluir `CPP` e `CPC`.
- [ ] Task: Unit Tests para os enums e métodos de conversão.

## Phase 2: Refatoração dos Serviços de Autorização e Casos de Uso
- [ ] Task: Refatorar `UserScopeAuthorizationService` para checar papéis específicos por escopo.
- [ ] Task: Refatorar `AuthorizationPolicy` e `EventoPatchAuthorizationService`.
- [ ] Task: Refatorar `ValidateAprovacaoUseCase` e `AddMembershipUseCase`.
- [ ] Task: Unit & Integration Tests para autorização.

## Phase 3: Migração de Dados e Validação E2E
- [ ] Task: Criar script Flyway SQL `V041__migrate_generic_roles_to_specific.sql`.
- [ ] Task: Atualizar controllers e DTOs para rejeitar papéis genéricos.
- [ ] Task: Executar suite completa de testes (`./gradlew test`).
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md).
