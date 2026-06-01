# Plan: Project Governance & Status Management (Gestão de Projetos)

## Phase 1: Backend Database, Models, and Project Approval Flow [checkpoint: 87991ae]
- [x] Task: Database Migrations and Entity Status Update (0e5c1ae)
    - [x] Create Liquibase/Flyway migration to add `status` column to `projetos` and initialize existing projects to `EM_ANDAMENTO` or `ABERTO`
    - [x] Create database migration for `projeto_solicitacoes_aprovacao` table to store pending creation and cancellation requests
    - [x] Update `ProjetoEntity` and `ProjetoStatus` enum on the backend
- [x] Task: Year Lock Validation and Pending Creation Requests (66281c2)
    - [x] Write tests verifying that creating a project in a closed year is intercepted and routed to a pending approval request
    - [x] Implement the validation policy and service handler for pending project creations
- [x] Task: Conductor - User Manual Verification 'Phase 1: Backend Database, Models, and Project Approval Flow' (Protocol in workflow.md) (87991ae)

## Phase 2: Project Status Transitions, Cancellation, and Auto-Activation
- [x] Task: Status Transition Actions and Event Linking Policy (2b21d6d)
    - [x] Write tests verifying the manual transition rules (Start, Conclude, Cancel) and role permissions
    - [x] Implement service methods and endpoints to manually start, conclude, and request cancellation for projects
    - [x] Update `ProjetoVincularPolicy` to allow event linking to `ABERTO` and `EM_ANDAMENTO` projects but block `CANCELADO` and `CONCLUIDO` projects
- [x] Task: Project Cancellation Options (Unlink vs. Cancel Events) (2b21d6d)
    - [x] Write integration tests for project cancellation behavior (checking that linked events are either unlinked or cancelled based on user option)
    - [x] Implement the linked events cascade/unlink handling inside the cancellation workflow
- [x] Task: Daily Auto-Activation Cron Job (2b21d6d)
    - [x] Write unit tests verifying that projects whose start date is reached are automatically activated to `EM_ANDAMENTO`
    - [x] Implement the `@Scheduled` daily activation task in Spring Boot
- [x] Task: Conductor - User Manual Verification 'Phase 2: Project Status Transitions, Cancellation, and Auto-Activation' [checkpoint: b3e3e2c]

## Phase 3: Frontend UI Screen and Layout Integration
- [x] Task: UI Details Controls and Actions Modals (9b2302d)
    - [x] Update API client in React to support project actions and approvals
    - [x] Add explicit action buttons ("Iniciar", "Concluir", "Cancelar") on the Projects details screen
    - [x] Implement the cancellation confirmation modal with the checkbox asking if user wants to cancel linked events
- [x] Task: Project Approvals Section (9b2302d)
    - [x] Implement UI views on the Approvals dashboard displaying pending projects and cancellation requests for authorized roles
    - [x] Write frontend unit tests validating rendering and action dispatches on these new UI elements
- [x] Task: Conductor - User Manual Verification 'Phase 3: Frontend UI Screen and Layout Integration' [checkpoint: 9b2302d]
