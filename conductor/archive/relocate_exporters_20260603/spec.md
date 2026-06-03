# Specification: Relocate Exporters and Address Package Smells

## Overview
Currently, the exporter classes (`CalendarioExporter` interface and its implementations: `CsvCalendarioExporter`, `ExcelCalendarioExporter`, `PdfCalendarioExporter`) reside in the package `br.com.nsfatima.gestao.calendario.application.usecase.evento`. Because exporters are document/file generation utilities and rely on external libraries, they are not application Use Cases. Having them in the `usecase` package violates Clean Architecture and domain separation principles.

The goal of this track is to:
1. Relocate the `CalendarioExporter` interface to a clean application package (`br.com.nsfatima.gestao.calendario.application.export`).
2. Relocate concrete implementations (`CsvCalendarioExporter`, `ExcelCalendarioExporter`, `PdfCalendarioExporter`) to a clean infrastructure package (`br.com.nsfatima.gestao.calendario.infrastructure.export`).
3. Relocate all corresponding test classes (`CsvCalendarioExporterTest`, `ExcelCalendarioExporterTest`, `PdfCalendarioExporterTest`) to match the new package structure under `src/test/java`.
4. Update references in the controllers, use case (`ExportacaoCalendarioUseCase`), configurations, and any other files referencing them.
5. Document any other package anomalies identified in the codebase for future remediation.

## Requirements
- Move the interface `CalendarioExporter` from `br.com.nsfatima.gestao.calendario.application.usecase.evento` to `br.com.nsfatima.gestao.calendario.application.export`.
- Move the implementations `CsvCalendarioExporter`, `ExcelCalendarioExporter`, and `PdfCalendarioExporter` from `br.com.nsfatima.gestao.calendario.application.usecase.evento` to `br.com.nsfatima.gestao.calendario.infrastructure.export`.
- Move the test files `CsvCalendarioExporterTest`, `ExcelCalendarioExporterTest`, and `PdfCalendarioExporterTest` from `br.com.nsfatima.gestao.calendario.application.usecase.evento` to their matching test packages:
  - `CsvCalendarioExporterTest` -> `br.com.nsfatima.gestao.calendario.infrastructure.export`
  - `ExcelCalendarioExporterTest` -> `br.com.nsfatima.gestao.calendario.infrastructure.export`
  - `PdfCalendarioExporterTest` -> `br.com.nsfatima.gestao.calendario.infrastructure.export`
- Update package declarations, imports, and references in the codebase.
- Ensure that the application builds successfully and all automated tests pass.
- Maintain regression-free document exports with exact layout matching.
- Identify and document other classes in `usecase` directories that are not use cases (e.g., custom exceptions, listeners, services) in a dedicated analysis section/document.

## Acceptance Criteria
- No exporter interface or implementation classes exist in the `br.com.nsfatima.gestao.calendario.application.usecase.evento` package.
- `CalendarioExporter` is located in `br.com.nsfatima.gestao.calendario.application.export`.
- Implementations are located in `br.com.nsfatima.gestao.calendario.infrastructure.export`.
- Tests are relocated to matching directories under `src/test/java`.
- All compilation tasks succeed and all unit/integration tests pass.
- A list of other identified package design issues/anomalies is compiled and documented.

## Out of Scope
- Modifying the functional export logic, styles, fields, or templates.
- Relocating other identified package anomalies under this specific track (they will be analyzed and documented, but actual relocation is out of scope to avoid scope drift).
