# Implementation Plan: Motor de Validação da Agenda Comunitária (Níveis de Prioridade 1-4 & Bypass)

## Phase 1: Mapeamento de Prioridade e Exceção Customizada
- [x] Task: Mapear níveis de prioridade (1 a 4) em `CategoriaEvento`. (6f4ee8f)
- [x] Task: Criar exceção `ConflitoAgendaEclesialException` e tratá-la em `GlobalExceptionHandler` para retornar HTTP 400. (2321875)
- [x] Task: Adicionar campo `bypassAgendaConflict` nos DTOs `CreateEventoRequest` e `UpdateEventoRequest`. (f6bd0f1)

## Phase 2: Serviço de Validação e Auto-Cancelamento
- [x] Task: Criar `AgendaPriorityValidatorService` implementando as regras dos Níveis 1 a 4. (3dabb19)
- [x] Task: Implementar lógica de auto-cancelamento/bloqueio para eventos Nível 1. (8bb9c8e)
- [x] Task: Integrar a validação em `CreateEventoUseCase` e `UpdateEventoUseCase`. (beb43d1)
- [x] Task: Validar permissão da role `PAROCO` ao processar `bypassAgendaConflict == true`. (2bde991)

## Phase 3: Testes de Integração e Validação E2E
- [x] Task: Escrever testes unitários e de integração cobrindo os 4 níveis de prioridade e o fluxo de bypass. (f65afc8)
- [x] Task: Atualizar tipagens TypeScript (`types.ts`) com `bypassAgendaConflict`. (efa6ecc)
- [x] Task: Atualizar componente `EventRegistrationModal.tsx` para suporte ao bypass (apenas role Pároco) e tratamento de erros de conflito. (0d2c277)
- [ ] Task: Executar suite completa de testes (`./gradlew test` e `npm test`).
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md).
