# Implementation Plan: Full Implementation of Project Concept

## Phase 1: Data Model and Infrastructure Evolution [checkpoint: a40412a]
- [x] Task: SQL Migration to add `organizacao_responsavel_id`, `inicio_utc`, and `fim_utc` to `projetos_eventos`. 152dc69
- [x] Task: Update `ProjetoEventoEntity` with new fields and JPA mappings. d6924f0
- [x] Task: Update `ProjetoCreateRequest` and `ProjetoResponse` DTOs. 08330fc
- [x] Task: Refactor `CreateProjetoUseCase` to validate dates and persist the responsible organization. a697886
- [x] Task: Conductor - User Manual Verification 'Phase 1: Data Model and Infrastructure Evolution' (Protocol in workflow.md) a40412a

## Phase 2: Establishing the Link and Constraints [checkpoint: a88535d]
- [x] Task: SQL Migration to add `projeto_id` to `eventos` table. 24762f8
- [x] Task: Update `EventoEntity` and event-related DTOs. 04da73c
- [x] Task: Implement `ProjetoIdValidationPolicy` to ensure project existence and status. 04da73c
- [x] Task: Enforce recurrence exclusion rule in event creation and project linking. 04da73c
- [x] Task: Implement temporal consistency validation (event dates within project dates). 04da73c
- [x] Task: Conductor - User Manual Verification 'Phase 2: Establishing the Link and Constraints' (Protocol in workflow.md) a88535d

## Phase 3: Governance and Visibility [checkpoint: d1bc1f3]
- [x] Task: Implement `ProjetoAuthorizationService` (actor-based security). f6ed721
- [x] Task: Update `ProjetoController` to use the new authorization service. f6ed721
- [x] Task: Add project-based filtering to event list API. f6ed721
- [x] Task: Include project details in event responses. f6ed721
- [x] Task: Conductor - User Manual Verification 'Phase 3: Governance and Visibility' (Protocol in workflow.md) d1bc1f3
