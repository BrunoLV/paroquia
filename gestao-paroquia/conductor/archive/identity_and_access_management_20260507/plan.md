# Implementation Plan: Identity and Access Management (IAM) API

## Phase 1: Data Model & Persistence
- [x] Task: Create `UsuarioEntity` and `MembroOrganizacaoEntity` mapping to existing tables. [41aeb0d]
- [x] Task: Create `UsuarioJpaRepository` and `MembroOrganizacaoJpaRepository`. [41aeb0d]
- [x] Task: Implement unit tests for repositories. [41aeb0d]

## Phase 2: Domain Services & Use Cases
- [x] Task: Implement `ManageUsuarioUseCase` for CRUD operations. [41aeb0d]
- [x] Task: Implement `ManageMembershipUseCase` for organization associations. [41aeb0d]
- [x] Task: Add validation for organization IDs and types. [41aeb0d]

## Phase 3: API Layer & Security
- [x] Task: Create `UsuarioController` with the specified endpoints. [41aeb0d]
- [x] Task: Implement `AuthController.getCurrentUser()` (`/api/v1/auth/me`). [41aeb0d]
- [x] Task: Apply security constraints to new endpoints. [41aeb0d]

## Phase 4: Verification
- [x] Task: Create integration tests for User/Membership management. [41aeb0d]
- [x] Task: Verify that new users can log in and have correct authorities. [41aeb0d]
