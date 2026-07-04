# Implementation Plan: Scoping Pastorais and Events by Church (Matriz/Comunidade)

## Phase 1: Database Migration [checkpoint: 15bf283]
- [x] Task: Create Flyway migration to add `igreja_vinculo_id` column to `eventos` table. [c06a817]
    - [x] Create `V039__add_event_igreja_vinculo.sql` adding `igreja_vinculo_id` (UUID references `calendario.organizacoes(id)`) to `calendario.eventos`.
- [x] Task: Conductor - User Manual Verification 'Database Schema' (Protocol in workflow.md) [15bf283]

## Phase 2: Backend Development (TDD) [checkpoint: 008a1fd]
- [x] Task: Red Phase - Write Backend Unit Tests
    - [x] Write tests in `AddMembershipUseCaseTest.java` verifying duplicate prevention and community-scoped coordinator unicity rules.
    - [x] Write tests in `AuthorizationPolicyTest.java` verifying that event creation/editing/approvals check church links.
- [x] Task: Green Phase - Implement Repository & Usecase Logic
    - [x] Add query methods to `MembroOrganizacaoJpaRepository` for finding memberships by organization, role, and church link (both with specific UUID and null).
    - [x] Update `AddMembershipUseCase` to prevent duplicate identical memberships and enforce coordinator unicity per church.
    - [x] Update `Evento` JPA entity, Mapper, and DTO classes (`EventoRequest`, `EventoResponse`) to include `igrejaVinculoId`.
    - [x] Update event creation/update logic to validate and save `igrejaVinculoId`.
    - [x] Update `AuthorizationPolicy` to restrict event editing/approval based on coordinator's church link matching the event's `igrejaVinculoId`.
- [x] Task: Conductor - User Manual Verification 'Backend Implementation' (Protocol in workflow.md) [008a1fd]

## Phase 3: Frontend Development (TDD) [checkpoint: 37289f5]
- [x] Task: Red Phase - Write Frontend Unit Tests
    - [x] Add unit tests in `EventRegistrationModal.test.tsx` verifying display and saving of `igrejaVinculoId`.
    - [x] Add unit tests in `Settings.test.tsx` verifying display and saving of community coordinators.
- [x] Task: Green Phase - Implement UI Changes
    - [x] Update `types.ts` to add `igrejaVinculoId` property on `Evento` and related types.
    - [x] Add `igrejaVinculoId` field (optional dropdown) to `EventRegistrationModal.tsx` for Pastorais and Movimentos.
    - [x] Update `Calendar.tsx` to render the concatenations of `Pastoral - Igreja` in event list and detail components.
    - [x] Update `Settings.tsx` Pastoral Edit dialog: replace the single text coordinator fields with a dynamic grid to display and assign the Coordinator and Vice-Coordinator for each active church (Matriz and all Communities).
- [x] Task: Conductor - User Manual Verification 'Frontend Implementation' (Protocol in workflow.md) [37289f5]
