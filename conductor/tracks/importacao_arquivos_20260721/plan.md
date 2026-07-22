# Implementation Plan: Cadastros do Sistema via Importação de Arquivos CSV e Excel (XLS/XLSX)

## Phase 1: Backend File Parser, Validation Engine & Security Enforcement (TDD)
- [ ] Task: Red Phase - Write Unit & Security Tests for File Import Endpoints
    - [ ] Create unit tests in `ImportacaoServiceTest` verifying parsing of CSV and Excel files for Members, Users, Organizations and Events.
    - [ ] Create security tests verifying that non-admin roles receive HTTP 403 Forbidden on import endpoints.
- [ ] Task: Green Phase - Implement Importation Endpoints and Services in Backend
    - [ ] Add Apache POI dependency to `build.gradle` for Excel parsing (`.xls` / `.xlsx`).
    - [ ] Implement `ImportacaoController.java`, `ImportacaoService.java`, and file parsers for CSV and Excel formats.
    - [ ] Implement validation engine for data types, email formats, church scoping, and duplicate detection.
    - [ ] Run `./gradlew test` to verify zero regression.
- [ ] Task: Conductor - User Manual Verification 'Backend File Parser, Validation Engine & Security Enforcement' (Protocol in workflow.md)

## Phase 2: Frontend Multi-Step Import Modal & Template Downloads
- [ ] Task: Red Phase - Write Frontend Tests for Import Modal & Preview Table
    - [ ] Add unit tests in `ImportModal.test.tsx` verifying template downloads, file selection, preview table with error highlights, and confirmation.
- [ ] Task: Green Phase - Implement Import Modal Component and API Integration
    - [ ] Create `ImportModal.tsx` supporting multi-entity selection (Membros, Usuários, Organizações, Eventos) and file upload (`.csv`, `.xls`, `.xlsx`).
    - [ ] Add "Importar Arquivo" button in admin views of `MembroList.tsx`, `Settings.tsx`, and `EventList.tsx`.
    - [ ] Run `npm run build` and `npm test` to verify zero regression.
- [ ] Task: Conductor - User Manual Verification 'Frontend Multi-Step Import Modal & Template Downloads' (Protocol in workflow.md)

## Phase 3: End-to-End Validation & Docker Seed Template Alignment
- [ ] Task: Verify End-to-End File Import Workflows & Seed Data Templates
    - [ ] Verify template CSV/Excel file downloads for all 4 entities.
    - [ ] Run full test suites `./gradlew test`, `npm test`, and `npm run build`.
- [ ] Task: Conductor - User Manual Verification 'End-to-End Validation & Docker Seed Template Alignment' (Protocol in workflow.md)
