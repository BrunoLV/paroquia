# Codebase Package Audit Report

## 1. Context
During the refactoring track `relocate_exporters_20260603`, we identified that the `usecase` package directories in several modules contained classes that are not application use cases (interactors). In Clean Architecture, a `usecase` (or application service representing a command/query) should represent a single business transaction/action. Placing utility components, strategy implementations, mappers, DTOs, and event listeners directly under `usecase` can lead to package clutter and architectural dilution.

This report documents all classes that violate this clean separation, along with recommendations for their proper package placement.

## 2. Identified Packaging Anomalies

### Module: `calendario`

| Class | Current Package | Type / Responsibility | Proposed Package / Remediation |
| :--- | :--- | :--- | :--- |
| `ReworkRateCalculator` | `calendario.application.usecase.metrics` | Calculator helper for rework rate metrics. | `calendario.application.service` or `calendario.domain.service` |
| `EventoIdempotencyService` | `calendario.application.usecase.evento` | Business logic service to ensure event idempotency. | `calendario.application.service` |
| `UpdateEventoExecutionStrategy` | `calendario.application.usecase.aprovacao` | Approval action execution strategy. | `calendario.application.usecase.aprovacao.strategy` |
| `CancelEventoExecutionStrategy` | `calendario.application.usecase.aprovacao` | Approval action execution strategy. | `calendario.application.usecase.aprovacao.strategy` |
| `CreateEventoExecutionStrategy` | `calendario.application.usecase.aprovacao` | Approval action execution strategy. | `calendario.application.usecase.aprovacao.strategy` |
| `PeriodoOperacionalInvalidoException` | `calendario.application.usecase.metrics` | Business/validation exception. | `calendario.domain.exception` |
| `CalendarLockedException` | `calendario.application.usecase.metrics` | Business/validation exception. | `calendario.domain.exception` |
| `ObservacaoNaoEncontradaException` | `calendario.application.usecase.observacao` | Entity not found exception. | `calendario.domain.exception` |
| `ObservacaoTipoManualInvalidoException` | `calendario.application.usecase.observacao` | Validation exception. | `calendario.domain.exception` |
| `ObservacaoAutorInvalidoException` | `calendario.application.usecase.observacao` | Validation exception. | `calendario.domain.exception` |
| `ObservacaoTipoImutavelException` | `calendario.application.usecase.observacao` | Validation exception. | `calendario.domain.exception` |
| `IdempotencyConflictException` | `calendario.application.usecase.evento` | Validation/idempotency conflict exception. | `calendario.domain.exception` |

### Module: `aprovacao`

| Class | Current Package | Type / Responsibility | Proposed Package / Remediation |
| :--- | :--- | :--- | :--- |
| `ApprovalNotificationListener` | `aprovacao.application.usecase` | Event listener handling approval notifications. | `aprovacao.application.listener` or `aprovacao.infrastructure.listener` |
| `ApprovalExecutionStrategy` | `aprovacao.application.usecase` | Strategy interface for executing approval decisions. | `aprovacao.application.usecase.strategy` (or in domain if purely domain model) |
| `ApprovalAuditPublisher` | `aprovacao.application.usecase` | Outgoing port interface for audit publishing. | `aprovacao.application.port.out` |
| `ApprovalMetricsPublisher` | `aprovacao.application.usecase` | Outgoing port interface for metrics publishing. | `aprovacao.application.port.out` |
| `ApprovalDecisionEvent` | `aprovacao.application.usecase` | Application / domain event representing a decision. | `aprovacao.domain.event` or `aprovacao.application.event` |
| `ApprovalActionPayload` | `aprovacao.application.usecase` | DTO representing the action payload. | `aprovacao.application.dto` |
| `ApprovalActionPayloadMapper` | `aprovacao.application.usecase` | Mapper utility for payload DTO. | `aprovacao.application.mapper` |
| `ApprovalExecutionFailedException` | `aprovacao.application.usecase` | Execution exception. | `aprovacao.domain.exception` |
| `ApprovalAlreadyDecidedException` | `aprovacao.application.usecase` | Business rule exception. | `aprovacao.domain.exception` |
| `ApprovalNotFoundException` | `aprovacao.application.usecase` | Entity not found exception. | `aprovacao.domain.exception` |

### Module: `projeto`

| Class | Current Package | Type / Responsibility | Proposed Package / Remediation |
| :--- | :--- | :--- | :--- |
| `ProjetoAgregacaoService` | `projeto.application.usecase` | Service coordinating project aggregations. | `projeto.application.service` |
| `ProjetoCreationResult` | `projeto.application.usecase` | DTO/Record representing the result of project creation. | `projeto.application.dto` or nested inside the use case class itself |

## 3. General Recommendations
1. **Differentiate Use Cases from Services:** Classes that coordinate multiple models or handle cross-cutting aspects (e.g., idempotency, aggregation) should reside in `application.service` instead of `application.usecase`.
2. **Standardize Exception Packages:** Relocate domain/business exceptions to `domain.exception` and application/technical exceptions to `application.exception`.
3. **Isolate Ports:** Interfaces representing outgoing gateways (e.g., audit publishers, notification portals) should be placed in `application.port.out` (or simply `application.port`).
4. **Isolate Data Transfer Objects (DTOs) and Mappers:** Place structures meant solely for carrying data across boundary lines in a dedicated `dto` package.
5. **Group Strategy Implementations:** Clean up package directories by moving strategies and execution algorithms to a `.strategy` subpackage.
