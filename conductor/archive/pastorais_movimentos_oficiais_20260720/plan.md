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

## Phase 3: Frontend Development (TDD) [checkpoint: 5cde2a7]
- [x] Task: Red Phase - Write Frontend Unit Tests [3c2a405]
    - [x] Add unit tests verifying the administrative CRUD tab for official types in settings.
    - [x] Add unit tests verifying that the dropdown selector for official types is displayed and mandatory for organizations of type `PASTORAL` / `MOVIMENTO`.
- [x] Task: Green Phase - Implement UI Changes [156f44e]
    - [x] Create catalog administration view in `Settings.tsx` (accessible to ADMIN users).
    - [x] Update `OrganizacaoFormModal.tsx` to include and dynamically filter the `PastoralMovimentoOficial` dropdown selector based on the chosen organization type.
    - [x] Update searching/filtering functionality in the list of organizations.
- [x] Task: Refactor - Remove Church Coordination Matrix [5cde2a7]
    - [x] Remove the coordination matrix layout and sequential membership linking logic in frontend.
    - [x] Expose standard Coordinator and Vice-Coordinator text fields unconditionally for all types (including Pastoral/Movimento) for independent church registrations.
    - [x] Update frontend tests to remove matrix assertions and test direct coordination field updates.
- [x] Task: Bug Fix - Database version column type mismatch [4026f9b]
    - [x] Create V041 migration SQL script altering version column type of calendario.pastorais_movimentos_oficiais to BIGINT.
- [x] Task: Refactor - Show dropdown for coordinator based on parent church members [9f65ca6]
    - [x] Fetch church members dynamically based on the chosen parent organization's parent church/community.
    - [x] Render Coordinator and Vice-Coordinator as Select dropdown fields displaying the names of members participating in the church.
    - [x] Keep standard text input fields as fallback if the parent organization does not resolve to a church.
    - [x] Wrap and update tests in `OrganizacaoFormModal.test.tsx` to assert members dropdown rendering.
- [x] Task: Conductor - User Manual Verification 'Frontend Implementation' (Protocol in workflow.md) [89d5285]
