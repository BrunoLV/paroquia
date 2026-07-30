# Implementation Plan: Escopo Territorial e Vínculo Obrigatório (community_id)

## Phase 1: Backend - Shield de Validação e Regras de Autorização por Escopo
- [x] Task: Adicionar validação `@NotNull` para `community_id` em `CreateUsuarioRequest`, `RegistrarUsuarioRequest` e `CreateEventoRequest`.
- [x] Task: Garantir mapeamento/aliasing nos DTOs para o campo `community_id` e tratamento de exceções de validação.
- [x] Task: Refatorar `UserScopeAuthorizationService` para trava estrita de `COORD_PASTORAL_LOCAL` na sua comunidade.
- [x] Task: Implementar bypass multicomunidade para `COORD_PASTORAL_GLOBAL` e busca por afinidade em `COORD_MOVIMENTO`.
- [x] Task: Unit & Integration Tests para o backend (`./gradlew test`).

## Phase 2: Frontend (UI) - Vínculo Obrigatório, Componentes e Filtros de Escopo
- [x] Task: Atualizar formulários (`EventRegistrationModal`, `EventEditModal`, formulários de usuários/membros em `Settings`) para tornar o campo de comunidade (`community_id` / `igrejaVinculoId`) obrigatório e validado.
- [x] Task: Implementar regras de UI para perfis de escopo local (`COORD_PASTORAL_LOCAL`), fixando a comunidade do usuário logado e desabilitando troca não autorizada.
- [x] Task: Ajustar componentes de filtro e listagem de eventos/membros para respeitar a visualização por escopo (local vs global vs movimentos).
- [x] Task: Vitest Unit & Component Tests no frontend (`npm test`).

## Phase 3: Validação E2E e Checkpoint
- [x] Task: Executar suíte completa de verificação no monorepo (`./gradlew test` e `npm test`).
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md).
