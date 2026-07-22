# Implementation Plan: Cadastros do Sistema via Importação de Arquivos CSV e Excel (XLS/XLSX)

## Phase 1: Backend File Parser, Validation Engine & Security Enforcement (TDD)
- [x] Task: Red Phase - Write Unit & Security Tests for File Import Endpoints
    - [x] Create unit tests in `ImportacaoServiceTest` verifying parsing of CSV and Excel files for Members, Users, Organizations and Events.
    - [x] Create security tests verifying that non-admin roles receive HTTP 403 Forbidden on import endpoints.
- [x] Task: Green Phase - Implement Importation Endpoints and Services in Backend
    - [x] Add Apache POI dependency to `build.gradle` for Excel parsing (`.xls` / `.xlsx`).
    - [x] Implement `ImportacaoController.java`, `ImportacaoService.java`, and file parsers for CSV and Excel formats.
    - [x] Implement validation engine for data types, email formats, church scoping, and duplicate detection.
    - [x] Run `./gradlew test` to verify zero regression.
- [x] Task: Conductor - User Manual Verification 'Backend File Parser, Validation Engine & Security Enforcement' (Protocol in workflow.md)

## Phase 2: Frontend Multi-Step Import Modal & Template Downloads
- [x] Task: Red Phase - Write Frontend Tests for Import Modal & Preview Table
    - [x] Add unit tests in `ImportModal.test.tsx` verifying template downloads, file selection, preview table with error highlights, and confirmation.
- [x] Task: Green Phase - Implement Import Modal Component and API Integration
    - [x] Create `ImportModal.tsx` supporting multi-entity selection (Membros, Usuários, Organizações, Eventos) and file upload (`.csv`, `.xls`, `.xlsx`).
    - [x] Add "Importar Arquivo" button in admin views of `MembroList.tsx`, `Settings.tsx`, and `EventList.tsx`.
    - [x] Run `npm run build` and `npm test` to verify zero regression.
- [x] Task: Conductor - User Manual Verification 'Frontend Multi-Step Import Modal & Template Downloads' (Protocol in workflow.md)

## Phase 3: End-to-End Validation & Docker Seed Template Alignment
- [x] Task: Verify End-to-End File Import Workflows & Seed Data Templates
    - [x] Verify template CSV/Excel file downloads for all 4 entities.
    - [x] Run full test suites `./gradlew test`, `npm test`, and `npm run build`.
- [x] Task: Conductor - User Manual Verification 'End-to-End Validation & Docker Seed Template Alignment' (Protocol in workflow.md)
