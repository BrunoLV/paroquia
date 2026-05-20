# Implementation Plan: Gerenciamento de Locais

## Phase 1: Domain and Infrastructure (Database & Entities)
- [x] Task: Create Flyway migration script to create or alter `locais` table with fields: `endereco`, `capacidade`, `status`, `caracteristicas`. 9b92617
- [x] Task: Update `Local` JPA entity with new fields and basic validation annotations. 9b92617
- [x] Task: Update `LocalRepository` with necessary queries (e.g., checking for associated events). 9b92617
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Domain and Infrastructure (Database & Entities)' (Protocol in workflow.md)

## Phase 2: Application Core (Use Cases / Services)
- [x] Task: Implement `LocalService` creation and update logic. 6684799
- [x] Task: Implement `LocalService` deletion/inactivation logic, including validation to block if the local is linked to any Evento. 6684799
- [x] Task: Implement `LocalService` retrieval logic. 6684799
- [x] Task: Write unit tests for `LocalService` covering all rules (especially the "block if in use" rule). 6684799
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Application Core (Use Cases / Services)' (Protocol in workflow.md)

## Phase 3: API Layer (Controllers and Security)
- [x] Task: Create/Update `LocalController` with `POST`, `PUT`, `GET`, and `DELETE` endpoints. 22d7776
- [x] Task: Define Request and Response DTOs for Local operations. 22d7776
- [x] Task: Configure Spring Security rules to ensure only `ROLE_ADMIN` can modify Locais. 22d7776
- [x] Task: Write integration tests to verify REST endpoints, DTO validation, and security constraints. 22d7776
- [ ] Task: Conductor - User Manual Verification 'Phase 3: API Layer (Controllers and Security)' (Protocol in workflow.md)

## Phase: Review Fixes
- [x] Task: Apply review suggestions 779ae0e