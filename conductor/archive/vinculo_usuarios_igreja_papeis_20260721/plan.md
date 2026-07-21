# Implementation Plan: Vínculos de Usuários/Membros, Igrejas, Organizações e Papéis

## Phase 1: Backend Domain Logic & Role Transition Services (TDD) [checkpoint: 395556d]
- [x] Task: Red Phase - Write Unit Tests for Church Member Scoping & Auto Role Transition 52e5986
    - [x] Create unit tests in `OrganizacaoServiceTest` and `UsuarioServiceTest` asserting church scoping validation and automatic role transition on coordinator change. 52e5986
- [x] Task: Green Phase - Implement Church Scoping Validation & Coordinator Role Transition c74ad44
    - [x] Update `OrganizacaoService.java` to enforce that members linked as coordinator/vice-coordinator belong to the same church. c74ad44
    - [x] Implement automatic transition logic: when a coordinator/vice-coordinator changes, demote previous coordinator to `MEMBRO` and promote new coordinator to `COORDENADOR`/`VICE_COORDENADOR`. c74ad44
- [x] Task: Conductor - User Manual Verification 'Backend Domain Logic & Role Transition Services' (Protocol in workflow.md) 395556d

## Phase 2: Frontend Member Scoping & Automatic Role Association (TDD) [checkpoint: 7bbad3b]
- [x] Task: Red Phase - Write Frontend Tests for Member Scoping & Zero Access State e47ab5d
    - [x] Add unit tests in `OrganizacaoFormModal.test.tsx` and `App.test.tsx` verifying church-scoped member dropdown filtering and zero-access state rendering. e47ab5d
- [x] Task: Green Phase - Update Form Modals & Implement Zero Access UI Component e47ab5d
    - [x] Update `OrganizacaoFormModal.tsx` and `Settings.tsx` to strictly filter user options by the parent church ID. e47ab5d
    - [x] Implement `UnlinkedUserBanner` / Zero-Access view in `App.tsx` / `MainLayout.tsx` for logged-in users without active organization memberships. e47ab5d
- [x] Task: Conductor - User Manual Verification 'Frontend Member Scoping & Automatic Role Association' (Protocol in workflow.md) 7bbad3b
