# Implementation Plan: Align Package Structure with DDD, Clean Architecture, and SOLID Standards

## Phase 1: Module `calendario` Packaging Restructuring [checkpoint: 8344cc4]

- [x] Task: Relocate Classes and Tests for Calendario d9fd3c7
    - [x] Create packages `domain.exception`, `application.service`, and `application.usecase.aprovacao.strategy` under `calendario`.
    - [x] Relocate exceptions to `domain.exception`: `PeriodoOperacionalInvalidoException`, `CalendarLockedException`, `ObservacaoNaoEncontradaException`, `ObservacaoTipoManualInvalidoException`, `ObservacaoAutorInvalidoException`, `ObservacaoTipoImutavelException`, `IdempotencyConflictException`.
    - [x] Relocate services to `application.service`: `ReworkRateCalculator`, `EventoIdempotencyService`.
    - [x] Relocate strategies to `application.usecase.aprovacao.strategy`: `UpdateEventoExecutionStrategy`, `CancelEventoExecutionStrategy`, `CreateEventoExecutionStrategy`.
    - [x] Restructure all corresponding test classes under `src/test/java` to match.
- [x] Task: Update Package Declarations and Code References d9fd3c7
    - [x] Adjust package statements inside all moved java classes.
    - [x] Update import references in all other classes within `calendario` and controllers.
- [x] Task: Verify Phase 1 Compilation d9fd3c7
    - [x] Run `./gradlew compileJava compileTestJava` to ensure Phase 1 restructuring compiles successfully.
- [x] Task: Conductor - User Manual Verification 'Phase 1: Module calendario Packaging Restructuring' (Protocol in workflow.md) 8344cc4

## Phase 2: Module `aprovacao` Packaging Restructuring

- [~] Task: Relocate Classes and Tests for Aprovacao
    - [ ] Create packages `domain.exception`, `domain.event`, `application.listener`, `application.usecase.strategy`, `application.port.out`, `application.dto`, and `application.mapper` under `aprovacao`.
    - [ ] Relocate exceptions to `domain.exception`: `ApprovalExecutionFailedException`, `ApprovalAlreadyDecidedException`, `ApprovalNotFoundException`.
    - [ ] Relocate events to `domain.event`: `ApprovalDecisionEvent`.
    - [ ] Relocate listener to `application.listener`: `ApprovalNotificationListener`.
    - [ ] Relocate strategy to `application.usecase.strategy`: `ApprovalExecutionStrategy`.
    - [ ] Relocate ports to `application.port.out`: `ApprovalAuditPublisher`, `ApprovalMetricsPublisher`.
    - [ ] Relocate DTO to `application.dto`: `ApprovalActionPayload`.
    - [ ] Relocate mapper to `application.mapper`: `ApprovalActionPayloadMapper`.
    - [ ] Restructure all corresponding test classes under `src/test/java` to match.
- [ ] Task: Update Package Declarations and Code References
    - [ ] Adjust package statements inside all moved java classes.
    - [ ] Update import references in all other classes within `aprovacao` and controllers.
- [ ] Task: Verify Phase 2 Compilation
    - [ ] Run `./gradlew compileJava compileTestJava` to ensure Phase 2 restructuring compiles successfully.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Module aprovacao Packaging Restructuring' (Protocol in workflow.md)

## Phase 3: Module `projeto` Packaging Restructuring & Full Verification

- [ ] Task: Relocate Classes and Tests for Projeto
    - [ ] Create packages `application.service` and `application.dto` under `projeto`.
    - [ ] Relocate service to `application.service`: `ProjetoAgregacaoService`.
    - [ ] Relocate DTO to `application.dto`: `ProjetoCreationResult`.
    - [ ] Restructure all corresponding test classes under `src/test/java` to match.
- [ ] Task: Update Package Declarations and Code References
    - [ ] Adjust package statements inside all moved java classes.
    - [ ] Update import references in all other classes within `projeto` and controllers.
- [ ] Task: Run Automated Tests
    - [ ] Run `./gradlew compileJava compileTestJava` to ensure the entire project compiles successfully.
    - [ ] Run `./gradlew test` to verify that all automated unit and integration tests pass.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Module projeto Packaging Restructuring & Full Verification' (Protocol in workflow.md)
