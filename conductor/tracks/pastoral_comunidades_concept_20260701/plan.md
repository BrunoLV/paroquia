# Implementation Plan: Scoping Pastorais and Events by Church (Matriz/Comunidade)

## Phase 1: Database Migration [checkpoint: 15bf283]
- [x] Task: Create Flyway migration to add `igreja_vinculo_id` column to `eventos` table. [c06a817]
    - [x] Create `V039__add_event_igreja_vinculo.sql` adding `igreja_vinculo_id` (UUID references `calendario.organizacoes(id)`) to `calendario.eventos`.
- [x] Task: Conductor - User Manual Verification 'Database Schema' (Protocol in workflow.md) [15bf283]

## Phase 2: Backend Development (TDD)
- [x] Task: Red Phase - Write Backend Unit Tests
    - [x] Write tests in `AddMembershipUseCaseTest.java` verifying duplicate prevention and community-scoped coordinator unicity rules.
    - [x] Write tests in `AuthorizationPolicyTest.java` verifying that event creation/editing/approvals check church links.
- [ ] Task: Green Phase - Implement Repository & Usecase Logic
    - [ ] Add query methods to `MembroOrganizacaoJpaRepository` for finding memberships by organization, role, and church link (both with specific UUID and null).
    - [ ] Update `AddMembershipUseCase` to prevent duplicate identical memberships and enforce coordinator unicity per church.
    - [ ] Update `Evento` JPA entity, Mapper, and DTO classes (`EventoRequest`, `EventoResponse`) to include `igrejaVinculoId`.
    - [ ] Update event creation/update logic to validate and save `igrejaVinculoId`.
    - [ ] Update `AuthorizationPolicy` to restrict event editing/approval based on coordinator's church link matching the event's `igrejaVinculoId`.
- [ ] Task: Conductor - User Manual Verification 'Backend Implementation' (Protocol in workflow.md)

## Phase 3: Frontend Development (TDD)
- [ ] Task: Red Phase - Write Frontend Unit Tests
    - [ ] Add unit tests in `EventRegistrationModal.test.tsx` verifying display and saving of `igrejaVinculoId`.
    - [ ] Add unit tests in `Settings.test.tsx` verifying display and saving of community coordinators.
- [ ] Task: Green Phase - Implement UI Changes
    - [ ] Update `types.ts` to add `igrejaVinculoId` property on `Evento` and related types.
    - [ ] Add `igrejaVinculoId` field (optional dropdown) to `EventRegistrationModal.tsx` for Pastorais and Movimentos.
    - [ ] Update `Calendar.tsx` to render the concatenations of `Pastoral - Igreja` in event list and detail components.
    - [ ] Update `Settings.tsx` Pastoral Edit dialog: replace the single text coordinator fields with a dynamic grid to display and assign the Coordinator and Vice-Coordinator for each active church (Matriz and all Communities).
- [ ] Task: Conductor - User Manual Verification 'Frontend Implementation' (Protocol in workflow.md)
