# Implementation Plan: Escopo Territorial e Vínculo Obrigatório (community_id)

## Phase 1: Shield de Validação e Padronização DTO
- [ ] Task: Adicionar validação `@NotNull` para `community_id` em `CreateUsuarioRequest`, `RegistrarUsuarioRequest` e `CreateEventoRequest`.
- [ ] Task: Garantir mapeamento/aliasing nos DTOs para o campo `community_id`.
- [ ] Task: Unit & Validation Tests para o shield de cadastro.

## Phase 2: Regras de Isolamento em UserScopeAuthorizationService
- [ ] Task: Implementar trava estrita por `community_id` para `COORDENADOR_PASTORAL_LOCAL`.
- [ ] Task: Implementar bypass multicomunidade para `COORDENADOR_PASTORAL_GLOBAL`.
- [ ] Task: Implementar busca por afinidade carismática para `COORDENADOR_MOVIMENTO` em `ListMembrosUseCase`.
- [ ] Task: Integration Tests cobrindo isolamento local vs visão global vs movimentos.

## Phase 3: Validação E2E
- [ ] Task: Executar suite completa de testes de autorização e escopo (`./gradlew test`).
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md).
