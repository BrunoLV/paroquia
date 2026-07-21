# Implementation Plan: Sistema de Autorização, Papéis e Alcance de Permissões

## Phase 1: Database Migration & Schema Updates [checkpoint: f14b354]
- [x] Task: Red Phase - Write Unit Tests for Schema & Role Enum Migration 9f2f387
    - [x] Add unit tests verifying `Papel` enum value mapping and migration script integrity. 9f2f387
- [x] Task: Green Phase - Flyway Migration V042 for User Roles 9f2f387
    - [x] Create V042 Flyway script updating `calendario.usuarios` role definitions and enum values (`PAROCO`, `VIGARIO`, `COORDENADOR`, `VICE_COORDENADOR`, `PADRE`, `MEMBRO`, `SECRETARIO`, `ADMIN`). 9f2f387
- [x] Task: Conductor - User Manual Verification 'Database Migration & Schema Updates' (Protocol in workflow.md) f14b354

## Phase 2: Backend Security & RBAC Scope Evaluators (TDD)
- [x] Task: Red Phase - Write Backend Security Scope Unit Tests d60eee5
    - [x] Create tests in `OrganizacaoSecurityTest` asserting scope boundaries for Paroco, Vigario, Conselhos (Community scope), Pastorais (Pastoral scope), Padre/Secretario (Read-only + Observations) and Membro. d60eee5
- [x] Task: Green Phase - Implement Security Scope Evaluators & Service Checks d60eee5
    - [x] Implement `@PreAuthorize` evaluators / Permission Evaluators in `gestao-paroquia` validating target organization against current user's membership scope. d60eee5
    - [ ] Protect mutation endpoints (`/api/v1/eventos`, `/api/v1/organizacoes`, `/api/v1/projetos`, etc.) according to role scopes.
- [~] Task: Conductor - User Manual Verification 'Backend Security & RBAC Scope Evaluators' (Protocol in workflow.md)

## Phase 3: Frontend Permissions Framework & UI Action Controls (TDD)
- [ ] Task: Red Phase - Write Frontend Permission Unit Tests
    - [ ] Add unit tests in `permissions.test.ts` for scope-based check functions (e.g. `canEditEvent`, `canApproveEvent`, `canAddObservation`).
- [ ] Task: Green Phase - Refactor UI Permissions & Action Buttons
    - [ ] Refactor `gestao-paroquia-ui/src/auth/permissions.ts` to implement scope checking.
    - [ ] Update UI components (Calendar, Events, Organizations, Projects, Observations, Settings) to show/hide/disable actions based on user role and organization scope.
- [ ] Task: Conductor - User Manual Verification 'Frontend Permissions Framework & UI Action Controls' (Protocol in workflow.md)
