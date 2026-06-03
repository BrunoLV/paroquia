# Specification: Align Package Structure with DDD, Clean Architecture, and SOLID Standards

## Overview
Based on our packaging audit, several modules (`calendario`, `aprovacao`, `projeto`) have classes that reside in `usecase` package directories but are not actually use case interactors (e.g., custom exceptions, listeners, mappers, services, ports, and DTOs). Under Clean Architecture and DDD principles, `usecase` (interactors) should represent singular business actions. Mixing architectural concerns inside the `usecase` directory clutters the packages and compromises the separation of concerns.

The goal of this track is to reorganize the package structure across all modules in the `gestao-paroquia` backend, ensuring that `usecase` packages contain only actual use cases, while exceptions, services, ports, listeners, and DTOs are relocated to their appropriate clean layers.

## Requirements

### 1. Define Target Packaging Structure
Within each module (`br.com.nsfatima.gestao.<module>`):
- **Domain Layer**:
  - `domain.exception`: Contains all custom exceptions (e.g., `PeriodoOperacionalInvalidoException`, `CalendarLockedException`, etc.).
  - `domain.event`: Contains core domain events.
- **Application Layer**:
  - `application.usecase`: **Strictly** contains only use case interactors (classes ending in `UseCase`).
  - `application.service`: Contains application services/orchestrators (e.g., `EventoIdempotencyService`, `ProjetoAgregacaoService`).
  - `application.port.out` (or `application.port`): Contains outgoing port interfaces (e.g., `ApprovalAuditPublisher`, `ApprovalMetricsPublisher`).
  - `application.dto`: Contains DTOs and record payloads used at boundaries (e.g., `ApprovalActionPayload`, `ProjetoCreationResult`).
  - `application.mapper`: Contains mapper utilities (e.g., `ApprovalActionPayloadMapper`).
- **API / Infrastructure Layer**:
  - `application.listener` or `infrastructure.listener` (or `api.listener`): Contains event handlers and listeners (e.g., `ApprovalNotificationListener`). Let's keep it in `application.listener` or `infrastructure.listener` depending on library/spring dependencies.

### 2. File Relocations

#### Module: `calendario`
- Relocate `ReworkRateCalculator` from `usecase.metrics` to `application.service`.
- Relocate `EventoIdempotencyService` from `usecase.evento` to `application.service`.
- Relocate custom exceptions to `domain.exception`:
  - `PeriodoOperacionalInvalidoException`
  - `CalendarLockedException`
  - `ObservacaoNaoEncontradaException`
  - `ObservacaoTipoManualInvalidoException`
  - `ObservacaoAutorInvalidoException`
  - `ObservacaoTipoImutavelException`
  - `IdempotencyConflictException`
- Relocate strategy classes `UpdateEventoExecutionStrategy`, `CancelEventoExecutionStrategy`, and `CreateEventoExecutionStrategy` to a subpackage `application.usecase.aprovacao.strategy`.

#### Module: `aprovacao`
- Relocate `ApprovalNotificationListener` to `application.listener` or `infrastructure.listener`.
- Relocate `ApprovalExecutionStrategy` interface to `application.usecase.strategy`.
- Relocate `ApprovalAuditPublisher` and `ApprovalMetricsPublisher` to `application.port.out`.
- Relocate `ApprovalDecisionEvent` to `domain.event` or `application.event`.
- Relocate `ApprovalActionPayload` to `application.dto`.
- Relocate `ApprovalActionPayloadMapper` to `application.mapper`.
- Relocate custom exceptions to `domain.exception`:
  - `ApprovalExecutionFailedException`
  - `ApprovalAlreadyDecidedException`
  - `ApprovalNotFoundException`

#### Module: `projeto`
- Relocate `ProjetoAgregacaoService` to `application.service`.
- Relocate `ProjetoCreationResult` to `application.dto`.

### 3. Compilation and Reference Updates
- Update package declarations, imports, and references in all java classes and configuration files.
- Restructure corresponding test classes to match the new package structure under `src/test/java`.
- Ensure all tests compile and pass successfully.

## Acceptance Criteria
- Use case packages contain ONLY use case classes (interactors).
- Misplaced services, exceptions, ports, mappers, DTOs, and strategies are relocated to their appropriate clean layers as defined above.
- Corresponding tests are relocated to match the package changes.
- The project builds and compiles with zero errors, and all automated tests pass successfully.
- Layout outputs, database seeding, and REST endpoints work regressions-free.
