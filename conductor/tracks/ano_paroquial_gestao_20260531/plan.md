# Plan: Parish Year Management (Ano Paroquial)

## Phase 1: Backend Implementations, Migrations, and Scheduled Tasks
- [x] Task: Database Migrations and Status Rename (879e1d6)
    - [x] Create a Liquibase SQL migration to rename any existing `PLANEJAMENTO` records and statuses in `anos_paroquiais` to `EM_PLANEJAMENTO`
    - [x] Update Java JPA entities and enums (`AnoParoquialStatus`) to support `EM_PLANEJAMENTO`, `FECHADO`, `CONCLUIDO`
- [x] Task: Database Seeding Logic (1a10b4d)
    - [x] Write failing test to check if current year is seeded when database is empty
    - [x] Implement startup application listener to seed current civil year with `EM_PLANEJAMENTO` if no years are registered
- [x] Task: Year Transition and Temporal Validations (345cadd)
    - [x] Write TDD tests for transition restrictions: Year N to `EM_PLANEJAMENTO` requires Year N-1 to be `CONCLUIDO`
    - [x] Write TDD tests for temporal restrictions: Year N can only transition out of `EM_PLANEJAMENTO` during its calendar year (`LocalDate.now().getYear() == N`)
    - [x] Implement validations in the service/controller layer and confirm tests pass
- [ ] Task: Year Rollover Scheduler
    - [ ] Write tests verifying scheduled cron execution behavior at Dec 31st 23:59:59
    - [ ] Implement a Spring Boot `@Scheduled` rollover cron task to transition current year to `CONCLUIDO` and create Year N+1 in `EM_PLANEJAMENTO`
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Backend Implementations, Migrations, and Scheduled Tasks' (Protocol in workflow.md)

## Phase 2: Frontend UI Screen and Layout Integration
- [ ] Task: API Client and Route Protection
    - [ ] Update frontend API client with GET and PATCH routes for `/anos-paroquiais`
    - [ ] Add route protection rules and `canManageAnoParoquial` permissions helper for `ADMIN`, `PAROCO`, and `CONSELHO` leaders
- [ ] Task: Create Parish Year Management Screen
    - [ ] Create `ParishYears.tsx` component page under `/anos-paroquiais` displaying a structured table of years
    - [ ] Implement toggle switch for `EM_PLANEJAMENTO` vs `FECHADO` status and a "Concluir Ano" button with confirmation modal
    - [ ] Write unit tests for `ParishYears.tsx` validating UI status rendering and button action dispatches
- [ ] Task: Sidebar Layout Integration
    - [ ] Add "Ano Paroquial" or "Configurações" link in `Layout.tsx` visible only to authorized users
    - [ ] Verify that unauthorized users are redirected and cannot see the link
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Frontend UI Screen and Layout Integration' (Protocol in workflow.md)
