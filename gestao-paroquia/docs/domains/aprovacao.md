# Domain: Aprovação

The **Aprovação** domain implements the governance workflow for the application, handling requests that require review and formal decision-making.

## Core Concepts

### Solicitação de Aprovação (Approval Request)
A request triggered by an action in another domain (e.g., creating an event in a "Locked" calendar or adding an "Extra" event).

### Status de Aprovação
- **PENDENTE**: Awaiting review.
- **APROVADA**: Request granted, triggering the associated action.
- **REPROVADA**: Request denied.

## Workflow

1. **Trigger**: An action (like `CreateEventoUseCase`) determines that approval is required based on business policies (e.g., `CalendarLockPolicy`).
2. **Request Creation**: A `SolicitacaoAprovacao` is created with the necessary context via **CreateSolicitacaoAprovacaoUseCase**.
3. **Review**: An authorized user (e.g., Paroco/Clero) reviews the request using **ListAprovacoesUseCase**.
4. **Decision**: The reviewer provides a status (`APROVADA`/`REPROVADA`) using **DecideSolicitacaoAprovacaoUseCase**.
5. **Action Execution**: If approved, the system automatically executes the originally requested action via specialized activation services.

## Use Cases

- **CreateSolicitacaoAprovacaoUseCase**: Initiates a new approval request.
- **DecideSolicitacaoAprovacaoUseCase**: Records the decision (approve/reject) for a request.
- **ListAprovacoesUseCase**: Lists pending and processed requests for authorized reviewers.
- **ValidateAprovacaoUseCase**: Internally validates if a specific action requires approval.

## Integration

The **Aprovação** domain acts as a gatekeeper for sensitive operations in:
- **Calendário**: For events scheduled in locked periods or with "Extra" status.
- **Projeto**: (Potential future use) for project approvals.
