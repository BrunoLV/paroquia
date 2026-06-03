# Implementation Plan: Clean and Refactor Export Calendar Use Case

## Phase 1: Configuration Infrastructure & Startup Seeding [checkpoint: ba43491]

- [x] Task: Create Database Migration, JPA Entity, and Repository 721d8cf
    - [x] Create Flyway migration script `V035__create_paroquia_configuracao.sql` in `db/migration`.
    - [x] Create JPA Entity `ParoquiaConfiguracaoEntity` mapped to the table.
    - [x] Create Spring Data Repository `ParoquiaConfiguracaoRepository`.
- [x] Task: Implement Startup Seeding with Classpath Logo 721d8cf
    - [x] Implement `ParoquiaConfiguracaoInitializer` implementing `ApplicationRunner` to seed default values.
    - [x] Write integration test verifying that default row is inserted with logo bytes from classpath when table is empty.
- [x] Task: Conductor - User Manual Verification 'Phase 1: Configuration Infrastructure & Startup Seeding' (Protocol in workflow.md) ba43491

## Phase 2: Exporter Strategy Classes

- [ ] Task: Create Exporter Strategy Interface and Unit Tests (TDD)
    - [ ] Define `CalendarioExporter` interface.
    - [ ] Create `CsvCalendarioExporterTest`, `ExcelCalendarioExporterTest`, and `PdfCalendarioExporterTest` classes to define expectations.
- [ ] Task: Implement Exporter Strategy Classes
    - [ ] Implement `CsvCalendarioExporter` and verify tests.
    - [ ] Implement `ExcelCalendarioExporter` resolving dynamic styling/headers/logo from the database entity.
    - [ ] Implement `PdfCalendarioExporter` (with the dynamic watermark/header/footer helper using dynamic configuration).
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Exporter Strategy Classes' (Protocol in workflow.md)

## Phase 3: Use Case Integration & Verification

- [ ] Task: Refactor and Simplify Use Case
    - [ ] Refactor `ExportacaoCalendarioUseCase` to inject the configuration repository and strategy list.
    - [ ] Delegate the export execution to the matching strategy bean.
- [ ] Task: Verify Project Compilation and Run Automated Tests
    - [ ] Run `./gradlew compileJava compileTestJava` to verify compilation.
    - [ ] Run `./gradlew test` to ensure all tests (including new and legacy tests) pass successfully.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Use Case Integration & Verification' (Protocol in workflow.md)
