# Implementation Plan: Rich Dashboard & Managerial Metrics

## Phase 1: Backend Metrics Endpoint & Integration Tests
- [ ] Task: Create Repository Queries & DTOs for Metrics
    - [ ] Create response DTOs (`DashboardMetricsDTO`, `GeneralSummaryDTO`, `EventDistributionDTO`, `ProjectDetailDTO`, `WeeklyEventDTO`) in module `projeto` or `support`.
    - [ ] Implement query methods in Evento and Projeto repositories to fetch count of events in current month, counts per organization, and events for current week.
- [ ] Task: Write Failing Tests for Dashboard Metrics (Red Phase)
    - [ ] Create `DashboardMetricsUseCaseTest` and `DashboardMetricsControllerTest` with mocks.
    - [ ] Write tests validating that the endpoint returns correct metrics, filters by user's organization memberships when user is not admin, and throws unauthorized when appropriate.
    - [ ] Run the tests and confirm they fail.
- [ ] Task: Implement UseCase and Controller (Green Phase)
    - [ ] Create `GetDashboardMetricsUseCase` orchestrating query and data aggregation.
    - [ ] Create `DashboardMetricsController` exposing `GET /api/v1/dashboard/metrics`.
    - [ ] Implement membership verification and filtering using `EventoActorContextResolver`.
    - [ ] Run the test suite and confirm all tests pass.
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Backend Metrics Endpoint & Integration Tests' (Protocol in workflow.md)

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
