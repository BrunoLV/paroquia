# Specification: Clean and Refactor Export Calendar Use Case

## Overview
Currently, `ExportacaoCalendarioUseCase` is a large class (~490 lines) carrying multiple responsibilities: parsing periods, querying database entities, mapping entity IDs to names, and generating three different output formats (CSV, Excel, PDF). Additionally, styling/branding assets, color palettes (Marian Blue), parish name, address, and diocese details are hardcoded within the source.

The objective of this track is to refactor this use case to separate the export generation logic using the Strategy Pattern and delegate parish metadata/branding configuration (including the logo) to a database settings table.

## Requirements

### 1. Strategy-based Export Architectures
- Define a `CalendarioExporter` interface:
  ```java
  public interface CalendarioExporter {
      boolean supports(String formato);
      byte[] export(List<EventoEntity> eventos, 
                    Map<UUID, String> localMap, 
                    Map<UUID, String> orgMap, 
                    Instant start, 
                    Instant end, 
                    ParoquiaConfiguracaoEntity config);
  }
  ```
- Implement the interface in three separate component classes:
  - `CsvCalendarioExporter`: Generates CSV byte arrays.
  - `ExcelCalendarioExporter`: Generates Excel spreadsheets using Apache POI.
  - `PdfCalendarioExporter`: Generates PDFs (with page event headers, footers, and watermarks) using OpenPDF.

### 2. Database-Driven Configuration
- Create a Flyway migration `V035__create_paroquia_configuracao.sql` for the settings table `paroquia_configuracao` in the `calendario` schema.
- Fields:
  - `id` (UUID or fixed PK)
  - `nome` VARCHAR(255) NOT NULL
  - `diocese` VARCHAR(255)
  - `endereco` VARCHAR(500)
  - `logo_bytes` BYTEA
  - `cor_primaria_hex` VARCHAR(7)
  - `cor_secundaria_hex` VARCHAR(7)
- Define a `ParoquiaConfiguracaoEntity` and `ParoquiaConfiguracaoRepository`.

### 3. Startup Initialization & Fallback
- Create an `ApplicationRunner` / startup bean that checks if the `paroquia_configuracao` database table is populated.
- If empty, it seeds a default row containing:
  - Nome: "Paróquia Nossa Senhora de Fátima"
  - Diocese: "Diocese de São Miguel Paulista"
  - Endereco: "Rua Gijoca, 7 - Vila Curuçá Velha - São Paulo/SP"
  - Primary color: "#1A237E" (Marian Blue)
  - Secondary color: "#808080" (Gray)
  - Logo: Loaded from classpath (`/logo-paroquia.png`) and converted to raw bytes (`byte[]`).

### 4. Refactoring Use Case
- Update `ExportacaoCalendarioUseCase` to:
  - Inject the `ParoquiaConfiguracaoRepository` and a list/registry of `CalendarioExporter` beans.
  - Load the active configuration.
  - Retrieve the events, local, and organization mappings.
  - Match the requested format against the appropriate `CalendarioExporter` and execute the generation.

## Acceptance Criteria
- `ExportacaoCalendarioUseCase` contains no PDF, Excel, or CSV generation details or hardcoded parish info/styling.
- Output layout, styling, and metadata from the refactored exporters matches original outputs exactly.
- All existing tests pass, and new tests verify each exporter and the configuration loading/seeding mechanism.
