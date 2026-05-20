# Implementation Plan: Gestão Administrativa de Usuários e Organizações

## Phase 1: Modularization & Core Infrastructure
- [x] Task: Move `OrganizacaoEntity`, `MembroOrganizacaoEntity` and related repositories to `br.com.nsfatima.gestao.organizacao`. 6e5007f
- [x] Task: Move `UsuarioEntity` and IAM related classes (Security fixtures, details service) to `br.com.nsfatima.gestao.iam`. 6e5007f
- [x] Task: Update all imports across the project to reflect the new modular structure. 6e5007f
- [x] Task: Conductor - User Manual Verification 'Phase 1: Modularization & Core Infrastructure' (Protocol in workflow.md)

## Phase 2: Organization Management (CRUD & Rules)
- [x] Task: Implement `OrganizacaoRepository` and `OrganizacaoService` with complete CRUD. 5b133f1
- [x] Task: Implement validation to block deletion of Organizations with active dependencies (Members/Events). 5b133f1
- [x] Task: Create `OrganizacaoController` with secured endpoints (`ROLE_ADMIN`). 5b133f1
- [x] Task: Write unit and integration tests for Organization management. 5b133f1
- [x] Task: Conductor - User Manual Verification 'Phase 2: Organization Management (CRUD & Rules)' (Protocol in workflow.md)

## Phase 3: IAM Administrative Management (Users & Roles) [checkpoint: fdfc728]
- [x] Task: Implement `UsuarioService` actions: `createByAdmin`, `updateRoles`, `toggleActiveStatus`, `resetPassword`. dd2d4ae
- [x] Task: Fix `UsuarioDetailsService` and `UsuarioDetails` to correctly load global roles from the database.
- [x] Task: Create `UsuarioAuthorizationService` to validate if an actor is an ADMIN or a COORDENADOR of the target user's organization.
- [x] Task: Implement delegation logic in `UsuarioAdminService` and `UsuarioController` endpoints using the new authorization service.
- [x] Task: Create `UsuarioAdminController` with secured endpoints. dd2d4ae
- [x] Task: Write unit and integration tests for administrative user management and delegation.
- [x] Task: Conductor - User Manual Verification 'Phase 3: IAM Administrative Management (Users & Roles)' (Protocol in workflow.md)

## Phase 4: Final Refinement & Security Verification [checkpoint: fdfc728]
- [x] Task: Ensure audit trail is captured for all administrative operations. d03589a
- [x] Task: Perform a full security audit on the new endpoints.
- [x] Task: Conductor - User Manual Verification 'Phase 4: Final Refinement & Security Verification' (Protocol in workflow.md)
