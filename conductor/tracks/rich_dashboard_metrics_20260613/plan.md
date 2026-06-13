# Implementation Plan: Rich Dashboard & Managerial Metrics

## Phase 1: Backend Metrics Endpoint & Integration Tests [checkpoint: 391f9b2]
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

## Phase 2: Frontend Dependency Setup & API Client [checkpoint: 0c9a747]
- [x] Task: Install MUI Charts Dependency d235d0f
    - [x] Add `@mui/x-charts` to `package.json` dependencies.
    - [x] Run `npm install` to update the dependencies.
- [x] Task: Write Failing Hook Tests & Define Types (Red Phase) d235d0f
    - [x] Define TypeScript interface `DashboardMetrics` in `src/api/types.ts`.
    - [x] Write failing test in `src/api/dashboard.test.ts` mocking the API response.
    - [x] Run the tests and confirm they fail.
- [x] Task: Implement API Client Fetching (Green Phase) d235d0f
    - [x] Implement `getDashboardMetrics` API fetch function using `apiClient`.
    - [x] Verify that the fetching client successfully retrieves data.
    - [x] Run the tests and confirm they pass.
- [x] Task: Conductor - User Manual Verification 'Phase 2: Frontend Dependency Setup & API client' (Protocol in workflow.md)

## Phase 3: Frontend Dashboard UI Implementation [checkpoint: 21381d9]
- [x] Task: Write Failing Component Tests (Red Phase)
    - [x] Write test suite for the redesigned `Dashboard` page component.
    - [x] Write mock assertions validating presence of PieChart, projects progress bar, weekly timeline, and general cards.
    - [x] Run tests and confirm they fail.
- [x] Task: Redesign Dashboard UI & Build Components (Green Phase)
    - [x] Implement general metrics summary cards with icons and gradients.
    - [x] Implement `EventDistributionPieChart` using PieChart from `@mui/x-charts`.
    - [x] Implement `ActiveProjectsProgressList` displaying `saudeTemporal` and execution status.
    - [x] Implement `WeeklyTimeline` displaying weekly scheduled events.
    - [x] Combine all components into `Dashboard.tsx` with skeleton loaders.
    - [x] Run tests and confirm all tests pass successfully.
- [x] Task: Conductor - User Manual Verification 'Phase 3: Frontend Dashboard UI Implementation' (Protocol in workflow.md)
