# Plan: Project Governance & Status Management (Gestão de Projetos)

## Phase 1: Backend Database, Models, and Project Approval Flow
- [x] Task: Database Migrations and Entity Status Update (0e5c1ae)
    - [x] Create Liquibase/Flyway migration to add `status` column to `projetos` and initialize existing projects to `EM_ANDAMENTO` or `ABERTO`
    - [x] Create database migration for `projeto_solicitacoes_aprovacao` table to store pending creation and cancellation requests
    - [x] Update `ProjetoEntity` and `ProjetoStatus` enum on the backend
- [x] Task: Year Lock Validation and Pending Creation Requests (66281c2)
    - [x] Write tests verifying that creating a project in a closed year is intercepted and routed to a pending approval request
    - [x] Implement the validation policy and service handler for pending project creations
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Backend Database, Models, and Project Approval Flow' (Protocol in workflow.md)

## Phase 2: Project Status Transitions, Cancellation, and Auto-Activation
- [ ] Task: Status Transition Actions and Event Linking Policy
    - [ ] Write tests verifying the manual transition rules (Start, Conclude, Cancel) and role permissions
    - [ ] Implement service methods and endpoints to manually start, conclude, and request cancellation for projects
    - [ ] Update `ProjetoVincularPolicy` to allow event linking to `ABERTO` and `EM_ANDAMENTO` projects but block `CANCELADO` and `CONCLUIDO` projects
- [ ] Task: Project Cancellation Options (Unlink vs. Cancel Events)
    - [ ] Write integration tests for project cancellation behavior (checking that linked events are either unlinked or cancelled based on user option)
    - [ ] Implement the linked events cascade/unlink handling inside the cancellation workflow
- [ ] Task: Daily Auto-Activation Cron Job
    - [ ] Write unit tests verifying that projects whose start date is reached are automatically activated to `EM_ANDAMENTO`
    - [ ] Implement the `@Scheduled` daily activation task in Spring Boot
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Project Status Transitions, Cancellation, and Auto-Activation' (Protocol in workflow.md)

## Phase 3: Frontend UI Screen and Layout Integration
- [ ] Task: UI Details Controls and Actions Modals
    - [ ] Update API client in React to support project actions and approvals
    - [ ] Add explicit action buttons ("Iniciar", "Concluir", "Cancelar") on the Projects details screen
    - [ ] Implement the cancellation confirmation modal with the checkbox asking if user wants to cancel linked events
- [ ] Task: Project Approvals Section
    - [ ] Implement UI views on the Approvals dashboard displaying pending projects and cancellation requests for authorized roles
    - [ ] Write frontend unit tests validating rendering and action dispatches on these new UI elements
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Frontend UI Screen and Layout Integration' (Protocol in workflow.md)
