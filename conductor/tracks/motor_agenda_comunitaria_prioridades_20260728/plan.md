# Implementation Plan: Motor de Validação da Agenda Comunitária (Níveis de Prioridade 1-4 & Bypass)

## Phase 1: Mapeamento de Prioridade e Exceção Customizada
- [x] Task: Mapear níveis de prioridade (1 a 4) em `CategoriaEvento`. (6f4ee8f)
- [ ] Task: Criar exceção `ConflitoAgendaEclesialException` e tratá-la em `GlobalExceptionHandler` para retornar HTTP 400.
- [ ] Task: Adicionar campo `bypassAgendaConflict` nos DTOs `CreateEventoRequest` e `UpdateEventoRequest`.

## Phase 2: Serviço de Validação e Auto-Cancelamento
- [ ] Task: Criar `AgendaPriorityValidatorService` implementando as regras dos Níveis 1 a 4.
- [ ] Task: Implementar lógica de auto-cancelamento/bloqueio para eventos Nível 1.
- [ ] Task: Integrar a validação em `CreateEventoUseCase` e `UpdateEventoUseCase`.
- [ ] Task: Validar permissão da role `PAROCO` ao processar `bypassAgendaConflict == true`.

## Phase 3: Testes de Integração e Validação E2E
- [ ] Task: Escrever testes unitários e de integração cobrindo os 4 níveis de prioridade e o fluxo de bypass.
- [ ] Task: Executar suite completa de testes (`./gradlew test`).
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md).
