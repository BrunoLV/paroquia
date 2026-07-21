# Implementation Plan: Registro de Tipos Pastorais e Movimentos Oficiais

## Phase 1: Database Migration & Seed Data [checkpoint: ]
- [~] Task: Implement Flyway Migration for schema updates
    - [~] Create a new V0xx migration script to create `calendario.pastorais_movimentos_oficiais` and add `pastoral_oficial_id` to `calendario.organizacoes`.
    - [~] Handle rename of `CONSELHO` to `CONSELHO_PASTORAL` and deletion of `LAICATO` in the database.
- [ ] Task: Populate official catalog and link existing seeds
    - [ ] Update `seed-data.sql` to populate `calendario.pastorais_movimentos_oficiais` with the full list of official Catholic pastorals and movements.
    - [ ] Link existing seeded organizations of type `PASTORAL` or `MOVIMENTO` to their corresponding official types.
- [ ] Task: Conductor - User Manual Verification 'Database Migration & Seed Data' (Protocol in workflow.md)

## Phase 2: Backend Development (TDD) [checkpoint: ]
- [ ] Task: Red Phase - Write Backend Unit Tests
    - [ ] Add unit tests for `PastoralMovimentoOficial` CRUD services, repository, and controller.
    - [ ] Add unit tests asserting validation errors when saving `PASTORAL` or `MOVIMENTO` organizations without a valid `pastoralOficialId`.
    - [ ] Add unit tests asserting role-based security (ADMIN role required for mutation endpoints).
- [ ] Task: Green Phase - Implement API & Business Logic
    - [ ] Implement `PastoralMovimentoOficial` JPA entity, repository, service, and controller endpoints under `/api/v1/pastorais-oficiais`.
    - [ ] Refactor Java codebase to use `CONSELHO_PASTORAL` instead of `CONSELHO` and remove `LAICATO` from `TipoOrganizacao` enum.
    - [ ] Update `validateHierarchy` and CRUD actions in `OrganizacaoService.java` to enforce the mandatory association of `PASTORAL`/`MOVIMENTO` with their official types.
- [ ] Task: Conductor - User Manual Verification 'Backend Implementation' (Protocol in workflow.md)

## Phase 3: Frontend Development (TDD) [checkpoint: ]
- [ ] Task: Red Phase - Write Frontend Unit Tests
    - [ ] Add unit tests verifying the administrative CRUD tab for official types in settings.
    - [ ] Add unit tests verifying that the dropdown selector for official types is displayed and mandatory for organizations of type `PASTORAL` / `MOVIMENTO`.
- [ ] Task: Green Phase - Implement UI Changes
    - [ ] Create catalog administration view in `Settings.tsx` (accessible to ADMIN users).
    - [ ] Update `OrganizacaoFormModal.tsx` to include and dynamically filter the `PastoralMovimentoOficial` dropdown selector based on the chosen organization type.
    - [ ] Update searching/filtering functionality in the list of organizations.
- [ ] Task: Conductor - User Manual Verification 'Frontend Implementation' (Protocol in workflow.md)
