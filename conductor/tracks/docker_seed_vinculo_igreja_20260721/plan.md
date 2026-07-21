# Implementation Plan: Atualização dos Arquivos Docker, Validação de Vínculo por Igreja e Seed Data

## Phase 1: Backend Strict Church Membership Validation (TDD)
- [x] Task: Red Phase - Write Unit Tests for Church Membership Scoping in AddMembershipUseCase & Controller
    - [x] Create unit tests in `AddMembershipUseCaseTest` verifying that linking a user to an organization of a different church throws `IllegalArgumentException`.
- [x] Task: Green Phase - Implement Strict Church Membership Validation in Backend
    - [x] Update `AddMembershipUseCase.java` and `OrganizacaoController.java` to fetch user's `igrejaId` and reject memberships when user's `igrejaId` differs from organization's `igrejaVinculoId`.
- [x] Task: Conductor - User Manual Verification 'Backend Strict Church Membership Validation' (Protocol in workflow.md)

## Phase 2: Frontend Scoped Membership Selectors & Validation
- [x] Task: Red Phase - Write Frontend Tests for Church-Scoped Membership Options in Settings
    - [x] Add unit tests in `Settings.test.tsx` verifying that membership dropdowns only present users/organizations matching the selected church.
- [x] Task: Green Phase - Implement Scoped Selectors in Settings.tsx
    - [x] Update `Settings.tsx` to filter user dropdown in Organization Members modal by user `igrejaId` matching organization's `igrejaVinculoId`.
    - [x] Update `Settings.tsx` to filter organization dropdown in User Memberships modal by organization's `igrejaVinculoId` matching user `igrejaId`.
- [x] Task: Conductor - User Manual Verification 'Frontend Scoped Membership Selectors & Validation' (Protocol in workflow.md)

## Phase 3: Docker Files Modernization & Seed Data Alignment
- [ ] Task: Modernize Docker Compose Files (Compose V2)
    - [ ] Update `docker-compose.yml` to remove deprecated `version:` top-level attribute and align service definitions with Compose V2 standards.
- [ ] Task: Align docker/seed-data.sql with Strict Church Membership Rules
    - [ ] Audit and update `docker/seed-data.sql` so all user `igreja_id` and membership `igreja_vinculo_id` records satisfy the strict church matching rule.
- [ ] Task: Conductor - User Manual Verification 'Docker Files Modernization & Seed Data Alignment' (Protocol in workflow.md)
