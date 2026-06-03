# Implementation Plan: Relocate Exporters and Address Package Smells

## Phase 1: Code and Test Files Relocation [checkpoint: 47bf464]

- [x] Task: Relocate Exporter Interfaces and Implementations 71c88d8
    - [x] Move `CalendarioExporter.java` from `br.com.nsfatima.gestao.calendario.application.usecase.evento` to `br.com.nsfatima.gestao.calendario.application.export` package directory.
    - [x] Move `CsvCalendarioExporter.java`, `ExcelCalendarioExporter.java`, and `PdfCalendarioExporter.java` to `br.com.nsfatima.gestao.calendario.infrastructure.export` package directory.
    - [x] Adjust package statements inside all moved Java classes.
- [x] Task: Relocate Exporter Test Files 71c88d8
    - [x] Move `CsvCalendarioExporterTest.java`, `ExcelCalendarioExporterTest.java`, and `PdfCalendarioExporterTest.java` from test source `usecase.evento` package to `br.com.nsfatima.gestao.calendario.infrastructure.export` test package directory.
    - [x] Adjust package statements inside all moved test classes.
- [x] Task: Conductor - User Manual Verification 'Phase 1: Code and Test Files Relocation' (Protocol in workflow.md) 47bf464

## Phase 2: Reference Updates & Compilation [checkpoint: eb894aa]

- [x] Task: Update Code References & Imports da39e2a
    - [x] Update imports in `ExportacaoCalendarioUseCase.java`.
    - [x] Update imports in `ExportacaoCalendarioController.java`.
    - [x] Update imports/package references in any configuration or spring wiring context.
- [x] Task: Verify Project Compilation da39e2a
    - [x] Run `./gradlew compileJava compileTestJava` to ensure all package relocations and import changes compile successfully.
- [x] Task: Conductor - User Manual Verification 'Phase 2: Reference Updates & Compilation' (Protocol in workflow.md) eb894aa

## Phase 3: Verification & Package Audit Report

- [ ] Task: Run Automated Tests
    - [ ] Run `./gradlew test` to verify that all relocated exporter tests and all existing test suites pass.
- [ ] Task: Compile Package Audit Report
    - [ ] Perform a full scan of all `usecase` package directories.
    - [ ] Document all classes that do not follow clean usecase architecture (exceptions, mappers, domain services, etc.) in a markdown report.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Verification & Package Audit Report' (Protocol in workflow.md)
