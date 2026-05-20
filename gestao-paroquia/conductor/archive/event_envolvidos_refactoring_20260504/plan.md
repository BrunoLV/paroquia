# Implementation Plan: Refactor Event Participation to Envolvidos

## Phase 1: Padronização do Conceito de "Envolvidos"
- [x] Task: Create `EventoEnvolvidoInput` DTO and update `EventoEnvolvidosRequest` and `EventoEnvolvidosResponse`. e4e594c
- [x] Task: Rename and refactor `UpdateEventoParticipantesUseCase` to `UpdateEventoEnvolvidosUseCase`, including support for participation roles. d9c2aea
- [x] Task: Rename and refactor `ClearEventoParticipantesUseCase` to `ClearEventoEnvolvidosUseCase`. e33898e
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Padronização do Conceito de "Envolvidos"' (Protocol in workflow.md)

## Phase 2: Decomposição do Controller
- [x] Task: Create `EventoEnvolvidoController` and migrate involvement endpoints. 10013e6
- [x] Task: Create `EventoRecorrenciaController` and migrate recurrence endpoints. 4e6a068
- [x] Task: Delete `EventoParticipacaoController`. a17fbfa
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Decomposição do Controller' (Protocol in workflow.md)

## Phase 3: Limpeza e Alinhamento
- [x] Task: Update existing tests to reflect the new naming and endpoint structure. 925a650
- [x] Task: Ensure `EventoEnvolvidoEntity` and repository are fully aligned with the new DTOs. 049bb27
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Limpeza e Alinhamento' (Protocol in workflow.md)
