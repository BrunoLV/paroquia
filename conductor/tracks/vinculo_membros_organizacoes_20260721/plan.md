# Implementation Plan: Vínculos de Membros com Organizações (Consistência Bidirecional e Sincronismo de Liderança)

## Phase 1: Backend Bidirectional Membership Resolution & Leadership Sync (TDD)
- [x] Task: Red Phase - Write Unit Tests for Bidirectional Organization Members & Leadership Sync
    - [x] Create unit tests in `ListMembrosOrganizacaoUseCaseTest` verifying name resolution for both `usuarioId` and `membroId`.
    - [x] Create unit tests in `UpdateOrganizacaoUseCaseTest` verifying automatic synchronization of `COORDENADOR` and `VICE_COORDENADOR` in `MembroOrganizacaoEntity`.
- [x] Task: Green Phase - Implement Name Resolution and Leadership Sync in Backend
    - [x] Update `ListMembrosOrganizacaoUseCase.java` and `MembroOrganizacaoResponse.java` to fetch and populate `nomeCompleto` from `UsuarioEntity` or `MembroEntity`.
    - [x] Update `CreateOrganizacaoUseCase.java` and `UpdateOrganizacaoUseCase.java` to maintain leadership memberships in `MembroOrganizacaoEntity`.
    - [x] Run `./gradlew test` to verify zero regression.
- [x] Task: Conductor - User Manual Verification 'Backend Bidirectional Membership Resolution & Leadership Sync' (Protocol in workflow.md)

## Phase 2: Frontend Organization Members Modal & Leadership Display Alignment
- [x] Task: Red Phase - Write Frontend Tests for Organization Members Modal
    - [x] Add unit tests in `ParoquiaStructure.test.tsx` and `Settings.test.tsx` verifying that members modal displays resolved names and roles for both users and members.
- [x] Task: Green Phase - Update Frontend Components and API Clients
    - [x] Update `organizacoes.ts` API client interfaces to include `nomeCompleto`.
    - [x] Update `ParoquiaStructure.tsx` and `Settings.tsx` members modals to correctly render member names, roles, and status.
    - [x] Run `npm run build` and `npm test` to verify zero regression.
- [x] Task: Conductor - User Manual Verification 'Frontend Organization Members Modal & Leadership Display Alignment' (Protocol in workflow.md)

## Phase 3: Docker Seed Data Alignment for Organization Memberships & Leadership
- [x] Task: Align Seed Data SQL with Bidirectional Leadership & Memberships
    - [x] Update `docker/seed-data.sql` so all organizations have corresponding coordinator and member entries in `membros_organizacao`.
    - [x] Verify SQL syntax and church scoping integrity.
- [x] Task: Conductor - User Manual Verification 'Docker Seed Data Alignment for Organization Memberships & Leadership' (Protocol in workflow.md)
