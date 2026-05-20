# Track Specification: Approval Flow Refinement

## Description
This track focuses on refining the event approval workflow to improve resilience, extensibility, and user experience. It addresses technical debt and adds safety mechanisms to prevent invalid or expired approvals.

## Requirements

### R1: Execution Resilience and Error Visibility
- Update `AprovacaoStatus` to include `FALHA_EXECUCAO`.
- When an automatic execution fails in `DecideSolicitacaoAprovacaoUseCase`, the status must be updated to `FALHA_EXECUCAO` instead of remaining as `APROVADA` in limbo.
- Capture and store the error message in the approval record for visibility.

### R2: Standardization of Actor Context
- Refactor all approval-related use cases and controllers to use `EventoActorContextResolver` for resolving the current actor.
- Remove manual `Authentication` object parsing in favor of the standardized resolver.

### R3: Strategy Pattern for Automatic Execution
- Refactor the switch-case in `DecideSolicitacaoAprovacaoUseCase` into a Strategy-based approach.
- Allow for easier extension of new approval types in the future.

### R4: Expiration Validation
- Prevent approval of requests where the event's start date is already in the past.
- Throw a meaningful business exception when trying to approve an expired request.

### R5: Notification Hook
- Implement a basic internal event publishing mechanism (using Spring Events) to notify about approval decisions.
- Add a listener that logs the decision for now, preparing for future email/push integrations.

## Definition of Done
- All 5 requirements implemented and verified by tests.
- Unit and integration tests covering error scenarios (execution failure, expiration).
- Code follows project standards and is fully documented.
- 100% pass rate in the project test suite.
