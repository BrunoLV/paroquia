# Implementation Plan: Rich Dashboard & Managerial Metrics

## Phase 1: Backend Metrics Endpoint & Integration Tests
- [x] Task: Create Repository Queries & DTOs for Metrics 07e61db
    - [x] Create response DTOs (`DashboardMetricsDTO`, `GeneralSummaryDTO`, `EventDistributionDTO`, `ProjectDetailDTO`, `WeeklyEventDTO`) in module `projeto` or `support`.
    - [x] Implement query methods in Evento and Projeto repositories to fetch count of events in current month, counts per organization, and events for current week.
- [x] Task: Write Failing Tests for Dashboard Metrics (Red Phase) 07e61db
    - [x] Create `DashboardMetricsUseCaseTest` and `DashboardMetricsControllerTest` with mocks.
    - [x] Write tests validating that the endpoint returns correct metrics, filters by user's organization memberships when user is not admin, and throws unauthorized when appropriate.
    - [x] Run the tests and confirm they fail.
- [x] Task: Implement UseCase and Controller (Green Phase) 07e61db
    - [x] Create `GetDashboardMetricsUseCase` orchestrating query and data aggregation.
    - [x] Create `DashboardMetricsController` exposing `GET /api/v1/dashboard/metrics`.
    - [x] Implement membership verification and filtering using `EventoActorContextResolver`.
    - [x] Run the test suite and confirm all tests pass.
- [x] Task: Conductor - User Manual Verification 'Phase 1: Backend Metrics Endpoint & Integration Tests' (Protocol in workflow.md)

## Phase 2: Frontend Dependency Setup & API Client
- [ ] Task: Install MUI Charts Dependency
    - [ ] Add `@mui/x-charts` to `package.json` dependencies.
    - [ ] Run `npm install` to update the dependencies.
- [ ] Task: Write Failing Hook Tests & Define Types (Red Phase)
    - [ ] Define TypeScript interface `DashboardMetrics` in `src/api/types.ts`.
    - [ ] Write failing test in `src/hooks/useDashboardMetrics.test.ts` mocking the API response.
    - [ ] Run the tests and confirm they fail.
- [ ] Task: Implement API Client Fetching (Green Phase)
    - [ ] Implement `useDashboardMetrics` custom hook using React Query and `apiClient`.
    - [ ] Verify that the fetching hook successfully retrieves and caches data.
    - [ ] Run the tests and confirm they pass.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Frontend Dependency Setup & API client' (Protocol in workflow.md)

## Phase 3: Frontend Dashboard UI Implementation
- [ ] Task: Write Failing Component Tests (Red Phase)
    - [ ] Write test suite for the redesigned `Dashboard` page component.
    - [ ] Write mock assertions validating presence of PieChart, projects progress bar, weekly timeline, and general cards.
    - [ ] Run tests and confirm they fail.
- [ ] Task: Redesign Dashboard UI & Build Components (Green Phase)
    - [ ] Implement general metrics summary cards with icons and gradients.
    - [ ] Implement `EventDistributionPieChart` using PieChart from `@mui/x-charts`.
    - [ ] Implement `ActiveProjectsProgressList` displaying `saudeTemporal` and execution status.
    - [ ] Implement `WeeklyTimeline` displaying weekly scheduled events.
    - [ ] Combine all components into `Dashboard.tsx` with skeleton loaders.
    - [ ] Run tests and confirm all tests pass successfully.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Frontend Dashboard UI Implementation' (Protocol in workflow.md)
