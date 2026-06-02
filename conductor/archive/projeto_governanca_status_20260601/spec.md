# Specification: Project Governance & Status Management (Gestão de Projetos)

## Overview
This specification details the governance, approval workflow, and status management for Projects (`Projetos`). It implements:
1. An approval flow for projects created in closed parish years.
2. Rich status lifecycles (`ABERTO`, `EM_ANDAMENTO`, `CANCELADO`, `CONCLUIDO`) with custom transition permissions.
3. Automated daily activation of projects when their start date arrives.
4. Flexible cancellation options for linked events.
5. Administrative action buttons on the UI with confirmation modals.

## Functional Requirements

### 1. Project Creation & Year Lock Approval
- When a Project is created:
  - The system checks if its start date (`inicioUtc`) falls within an `AnoParoquial` that is currently in `FECHADO` or `CONCLUIDO` status.
  - If it falls within a locked year:
    - The project is created with a pending state (`ABERTO_PENDENTE_APROVACAO` or similar, or the project itself is held in an approval request table).
    - To keep the system clean, we will create a dedicated `ProjetoApprovalRequest` table to hold pending creation requests until approved.
    - Approvers for this action are: `ADMIN`, `PAROCO`, and `CONSELHO` Coordinators/Vice-Coordinators.
  - If it falls within an open year (e.g., `EM_PLANEJAMENTO`), the project is created directly in `ABERTO` status.

### 2. Status Lifecycle & Transitions
A project can have the following statuses:
- **`ABERTO`**: Initial state. The project is registered but not active.
  - Events **can** be linked to projects in the `ABERTO` status.
- **`EM_ANDAMENTO`**: The project is active. 
  - **Manual Trigger**: Authorized roles (`ADMIN`, `PAROCO`, `CONSELHO` leaders, or Organization Coordinator/Vice-Coordinator of the project's responsible org) can manually start the project.
  - **Automatic Trigger**: A daily cron job starts the project automatically if the current date is $\ge$ `inicioUtc`.
  - Events **can** be linked to projects in the `EM_ANDAMENTO` status.
- **`CANCELADO`**: The project is cancelled.
  - Can only be requested by:
    - Coordinator or Vice-Coordinator of the project's responsible organization.
    - `PAROCO`, `ADMIN`, or `CONSELHO` Coordinator/Vice-Coordinator.
  - **Approval Rule for Cancellation**:
    - If requested by `PAROCO`, `ADMIN`, or `CONSELHO` leaders: Cancelled immediately upon confirmation (no approval needed).
    - If requested by Organization Coordinator/Vice-Coordinator: Requires confirmation and goes to a pending approval request. Once approved by `ADMIN`, `PAROCO`, or `CONSELHO` leaders, it transitions to `CANCELADO`.
- **`CONCLUIDO`**: The project is completed.
  - Done manually (no approval required, just confirmation).
  - Can only be executed by: `ADMIN`, `PAROCO`, `CONSELHO` leaders, or the Coordinator/Vice-Coordinator of the project's responsible organization.

### 3. Linked Events Options upon Cancellation
- When a project is cancelled, the user must be prompted with a modal offering two choices:
  1. **Desvincular (Default)**: Keep the linked events but remove the project association (set `projeto_id = null` on those events).
  2. **Cancelar Eventos**: Automatically cancel all events linked to the project (set their status to `CANCELADO`).

### 4. Automatic Activation Cron Job
- A Spring Boot `@Scheduled` task runs daily (e.g., at 00:00:01 UTC) to query all `ABERTO` projects where `inicioUtc` is $\le$ current date and automatically transitions them to `EM_ANDAMENTO`.

## User Interface (UI)
- Update the project details/edit page to include action buttons:
  - **Iniciar Projeto** (shown for `ABERTO` status).
  - **Concluir Projeto** (shown for `EM_ANDAMENTO` status).
  - **Cancelar Projeto** (shown for `ABERTO` or `EM_ANDAMENTO` status).
- The "Cancelar Projeto" action opens a dialog modal with a checkbox: *"Deseja cancelar também todos os eventos vinculados a este projeto?"*.
- Add a new "Solicitações de Projetos" or "Aprovações de Projetos" view or section in the Approvals page for authorized users to approve/reject pending projects or cancellation requests.

## Acceptance Criteria
- [ ] Attempting to link an event to a `CANCELADO` or `CONCLUIDO` project throws a validation error.
- [ ] Attempting to link an event to an `ABERTO` or `EM_ANDAMENTO` project is allowed and succeeds.
- [ ] Attempting to create a project starting in a closed year blocks direct creation and creates a pending approval request.
- [ ] Cancelling a project as a pastoral coordinator places it in a pending approval state, while an admin or parish priest cancels it immediately.
- [ ] Auto-activation cron job correctly starts projects.
