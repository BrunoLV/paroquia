# Implementation Plan: Vínculos de Membros com Organizações (Consistência Bidirecional e Sincronismo de Liderança)

## Phase 1: Backend Bidirectional Membership Resolution & Leadership Sync (TDD)
- [ ] Task: Red Phase - Write Unit Tests for Bidirectional Organization Members & Leadership Sync
    - [ ] Create unit tests in `ListMembrosOrganizacaoUseCaseTest` verifying name resolution for both `usuarioId` and `membroId`.
    - [ ] Create unit tests in `UpdateOrganizacaoUseCaseTest` verifying automatic synchronization of `COORDENADOR` and `VICE_COORDENADOR` in `MembroOrganizacaoEntity`.
- [ ] Task: Green Phase - Implement Name Resolution and Leadership Sync in Backend
    - [ ] Update `ListMembrosOrganizacaoUseCase.java` and `MembroOrganizacaoResponse.java` to fetch and populate `nomeCompleto` from `UsuarioEntity` or `MembroEntity`.
    - [ ] Update `CreateOrganizacaoUseCase.java` and `UpdateOrganizacaoUseCase.java` to maintain leadership memberships in `MembroOrganizacaoEntity`.
    - [ ] Run `./gradlew test` to verify zero regression.
- [ ] Task: Conductor - User Manual Verification 'Backend Bidirectional Membership Resolution & Leadership Sync' (Protocol in workflow.md)

## Phase 2: Frontend Organization Members Modal & Leadership Display Alignment
- [ ] Task: Red Phase - Write Frontend Tests for Organization Members Modal
    - [ ] Add unit tests in `ParoquiaStructure.test.tsx` and `Settings.test.tsx` verifying that members modal displays resolved names and roles for both users and members.
- [ ] Task: Green Phase - Update Frontend Components and API Clients
    - [ ] Update `organizacoes.ts` API client interfaces to include `nomeCompleto`.
    - [ ] Update `ParoquiaStructure.tsx` and `Settings.tsx` members modals to correctly render member names, roles, and status.
    - [ ] Run `npm run build` and `npm test` to verify zero regression.
- [ ] Task: Conductor - User Manual Verification 'Frontend Organization Members Modal & Leadership Display Alignment' (Protocol in workflow.md)

## Phase 3: Docker Seed Data Alignment for Organization Memberships & Leadership
- [ ] Task: Align Seed Data SQL with Bidirectional Leadership & Memberships
    - [ ] Update `docker/seed-data.sql` so all organizations have corresponding coordinator and member entries in `membros_organizacao`.
    - [ ] Verify SQL syntax and church scoping integrity.
- [ ] Task: Conductor - User Manual Verification 'Docker Seed Data Alignment for Organization Memberships & Leadership' (Protocol in workflow.md)
