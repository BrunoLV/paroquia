# Implementation Plan: Registro de Tipos Pastorais e Movimentos Oficiais

## Phase 1: Database Migration & Seed Data [checkpoint: c5bfc1e]
- [x] Task: Implement Flyway Migration for schema updates (c5bfc1e)
    - [x] Create a new V0xx migration script to create `calendario.pastorais_movimentos_oficiais` and add `pastoral_oficial_id` to `calendario.organizacoes`. (c5bfc1e)
    - [x] Handle rename of `CONSELHO` to `CONSELHO_PASTORAL` and deletion of `LAICATO` in the database. (c5bfc1e)
- [x] Task: Populate official catalog and link existing seeds (c5bfc1e)
    - [x] Update `seed-data.sql` to populate `calendario.pastorais_movimentos_oficiais` with the full list of official Catholic pastorals and movements. (c5bfc1e)
    - [x] Link existing seeded organizations of type `PASTORAL` or `MOVIMENTO` to their corresponding official types. (c5bfc1e)
- [x] Task: Conductor - User Manual Verification 'Database Migration & Seed Data' (c5bfc1e)

## Phase 2: Backend Development (TDD) [checkpoint: 297bd49]
- [x] Task: Red Phase - Write Backend Unit Tests
    - [x] Add unit tests for `PastoralMovimentoOficial` CRUD services, repository, and controller.
    - [x] Add unit tests asserting validation errors when saving `PASTORAL` or `MOVIMENTO` organizations without a valid `pastoralOficialId`.
    - [x] Add unit tests asserting role-based security (ADMIN role required for mutation endpoints).
- [x] Task: Green Phase - Implement API & Business Logic [6f429d2]
    - [x] Implement `PastoralMovimentoOficial` JPA entity, repository, service, and controller endpoints under `/api/v1/pastorais-oficiais`.
    - [x] Refactor Java codebase to use `CONSELHO_PASTORAL` instead of `CONSELHO` and remove `LAICATO` from `TipoOrganizacao` enum.
    - [x] Update `validateHierarchy` and CRUD actions in `OrganizacaoService.java` to enforce the mandatory association of `PASTORAL`/`MOVIMENTO` with their official types.
- [x] Task: Conductor - User Manual Verification 'Backend Implementation' (Protocol in workflow.md) [297bd49]

## Phase 3: Frontend Development (TDD) [checkpoint: ]
- [x] Task: Red Phase - Write Frontend Unit Tests [3c2a405]
    - [x] Add unit tests verifying the administrative CRUD tab for official types in settings.
    - [x] Add unit tests verifying that the dropdown selector for official types is displayed and mandatory for organizations of type `PASTORAL` / `MOVIMENTO`.
- [~] Task: Green Phase - Implement UI Changes
    - [~] Create catalog administration view in `Settings.tsx` (accessible to ADMIN users).
    - [~] Update `OrganizacaoFormModal.tsx` to include and dynamically filter the `PastoralMovimentoOficial` dropdown selector based on the chosen organization type.
    - [~] Update searching/filtering functionality in the list of organizations.
- [ ] Task: Conductor - User Manual Verification 'Frontend Implementation' (Protocol in workflow.md)
