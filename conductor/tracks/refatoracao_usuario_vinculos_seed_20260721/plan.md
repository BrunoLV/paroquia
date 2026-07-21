# Implementation Plan: Refatoração do Modelo de Usuário, Vínculos com Organizações e Seed Docker

## Phase 1: User Data Model Refactoring & Flyway Migration (TDD)
- [ ] Task: Red Phase - Write Backend Unit Tests for Usuario igrejaId & Clean Registration Flow
    - [ ] Create unit tests in `UsuarioServiceTest` and `RegistrarUsuarioUseCaseTest` asserting mandatory `igrejaId` validation and clean registration flow without legacy pastoral requests.
- [ ] Task: Green Phase - Implement Usuario igrejaId, Flyway V043 Migration & DTO Cleanups
    - [ ] Create Flyway migration `V043__add_usuario_igreja_id.sql` adding `igreja_id` column to `calendario.usuarios`.
    - [ ] Update `UsuarioEntity.java`, `UsuarioResponse.java`, `RegistrarUsuarioRequest.java`, and `RegistrarUsuarioUseCase.java` to handle `igrejaId` cleanly.
- [ ] Task: Conductor - User Manual Verification 'User Data Model Refactoring & Flyway Migration' (Protocol in workflow.md)

## Phase 2: Frontend User Model Cleanup & User Memberships Modal (TDD)
- [ ] Task: Red Phase - Write Frontend Tests for Registration & User Memberships Management
    - [ ] Add unit tests in `Register.test.tsx` and `Settings.test.tsx` for simplified registration and user memberships modal rendering.
- [ ] Task: Green Phase - Refactor Register Component & Implement User Memberships Modal
    - [ ] Refactor `Register.tsx` / `Login.tsx` to require only `igrejaId` on user registration.
    - [ ] Create `UserMembershipsModal.tsx` in `Settings.tsx` to display and manage user organization memberships with roles.
    - [ ] Update `OrganizacaoFormModal.tsx` to dynamically re-filter coordinator options when `parentId` changes.
- [ ] Task: Conductor - User Manual Verification 'Frontend User Model Cleanup & User Memberships Modal' (Protocol in workflow.md)

## Phase 3: Docker Seed Data Update & End-to-End Verification
- [ ] Task: Update Docker SQL Seed Script with Test Hierarchy & Memberships
    - [ ] Update `V002__seed_data.sql` / seed scripts to include Matriz, Comunidade Santo Agostinho, pre-configured users with `igreja_id`, and active memberships.
- [ ] Task: Conductor - User Manual Verification 'Docker Seed Data Update & End-to-End Verification' (Protocol in workflow.md)
