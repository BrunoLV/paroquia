# Implementation Plan: Event Categories Enum

## Phase 1: Domain and Database Migrations [checkpoint: 3ca031a]
- [x] Task: Create the new `CategoriaEvento` Enum in the domain package with the specified values (PASTORAL, SOCIAL, LITURGICO, ADMINISTRATIVO, SACRAMENTAL, FORMATIVO, ASSISTENCIAL) and JSON/String mapping utilities. [fd3726f]
- [x] Task: Create a Flyway migration to add a `categoria` VARCHAR column to the `eventos` table, drop the `categoria_id` foreign key and column, and drop the `categorias` table. [c85f2ad]
- [x] Task: Conductor - User Manual Verification 'Phase 1: Domain and Database Migrations' (Protocol in workflow.md) [3ca031a]

## Phase 2: Entity and Repository Refactoring [checkpoint: a480bd1]
- [x] Task: Delete `CategoriaEntity` and `CategoriaJpaRepository`. [4dae4b9]
- [x] Task: Update `EventoEntity` to replace the `categoriaId` (UUID) field with `categoria` (String or Enum type). Update related getter/setter methods. [bdfd1f3]
- [x] Task: Update any custom queries in `EventoJpaRepository` or fake repositories used in tests that reference `categoriaId` to use the new `categoria` field. [verified: no queries found]
- [x] Task: Conductor - User Manual Verification 'Phase 2: Entity and Repository Refactoring' (Protocol in workflow.md) [a480bd1]

## Phase 3: DTO and Service Refactoring [checkpoint: 2d15498]
- [x] Task: Update event-related DTOs (`CreateEventoRequest`, `UpdateEventoRequest`, `EventoResponse`, and any filter/search requests) to use `CategoriaEvento` or its string representation instead of `categoriaId`. [24746f9]
- [x] Task: Refactor use cases (`CreateEventoUseCase`, `UpdateEventoUseCase`, etc.) and domain services to remove logic that fetches or validates the `CategoriaEntity` from the database. [3eec78d]
- [x] Task: Conductor - User Manual Verification 'Phase 3: DTO and Service Refactoring' (Protocol in workflow.md) [2d15498]

## Phase 4: Test Updates and Final Verification [checkpoint: 2d15498]
- [x] Task: Update all unit and integration tests (e.g., `EventoController*Test`, fake repositories, use case tests) to align with the new DTO structure and the removal of the `categorias` table. Fix all compilation errors caused by the refactoring. [c87aef6]
- [x] Task: Conductor - User Manual Verification 'Phase 4: Test Updates and Final Verification' (Protocol in workflow.md) [2d15498]