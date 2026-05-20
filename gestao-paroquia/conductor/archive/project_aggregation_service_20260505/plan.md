# Implementation Plan: Project Aggregation Service

## Phase 1: Data Transfer Objects (DTOs) and Domain Logic [checkpoint: 83a9c51]
- [x] Task: Create DTOs to represent the aggregation response (`ProjetoResumoDTO`, `StatusExecucaoDTO`, `MapaColaboracaoDTO`, `SaudeTemporalDTO`) [2b1d312]
- [x] Task: Write unit tests for any specific domain logic required for the aggregation (e.g., a utility function or domain service for calculating the percentage of time elapsed and evaluating the "Saúde Temporal" thresholds) [762048f]
- [x] Task: Implement the domain logic to make the tests pass. [3283279]
- [x] Task: Conductor - User Manual Verification 'Phase 1: Data Transfer Objects (DTOs) and Domain Logic' (Protocol in workflow.md) [83a9c51]

## Phase 2: Persistence Layer (Queries) [checkpoint: 518e58c]
- [x] Task: Write integration tests for new repository queries needed to fetch the aggregation data efficiently (e.g., count events by project and status, fetch distinct groups involved in a project and its events). [8680c69]
- [x] Task: Implement the required repository methods (likely in `ProjetoRepository` or `EventoRepository` using Spring Data JPA custom queries or JPQL) to make the tests pass. [8680c69]
- [x] Task: Conductor - User Manual Verification 'Phase 2: Persistence Layer (Queries)' (Protocol in workflow.md) [518e58c]

## Phase 3: Application Service [checkpoint: 8119e4c]
- [x] Task: Write unit tests for the new `ProjetoAgregacaoService`. Mock the repository dependencies to verify the orchestration, aggregation logic, and mapping to DTOs. [400ea8b]
- [x] Task: Implement `ProjetoAgregacaoService` to fulfill the business logic and make the tests pass. [04b3ce0]
- [x] Task: Conductor - User Manual Verification 'Phase 3: Application Service' (Protocol in workflow.md) [8119e4c]

## Phase 4: API Endpoint [checkpoint: f132503]
- [x] Task: Write API integration tests (e.g., using `@WebMvcTest` and `MockMvc`) for the new `GET /api/v1/projetos/{projetoId}/resumo` endpoint. [f273293]
- [x] Task: Implement the endpoint in `ProjetoController` (or a dedicated controller) and wire it to the `ProjetoAgregacaoService`. [15f7510]
- [x] Task: Conductor - User Manual Verification 'Phase 4: API Endpoint' (Protocol in workflow.md) [f132503]