# Implementation Plan: Implement Project Management API Refinement

## Phase 1: DTO and Validation Refinement [checkpoint: 5186ccf]
- [x] Task: Update Project DTOs with standard Bean Validation annotations [48fba23]
    - [x] Add `@NotBlank` and `@Size(max = 160)` to `ProjetoCreateRequest`.
    - [x] Add `@Size(min = 1, max = 160)` to `ProjetoPatchRequest`.
- [x] Task: Add OpenAPI annotations to Project Controller and DTOs [2f815f7]
    - [x] Document all endpoints in `ProjetoController`.
    - [x] Add `@Schema` descriptions to Project DTOs.
- [x] Task: Conductor - User Manual Verification 'Phase 1: DTO and Validation Refinement' (Protocol in workflow.md)

## Phase 2: Core Implementation [checkpoint: d34a8fd]
- [x] Task: Implement actual logic for Project listing and patching [5870cc1]
    - [x] Update `ProjetoController.list()` to use a `ListProjetosUseCase`.
    - [x] Update `ProjetoController.patch()` to use an `UpdateProjetoUseCase`.
- [x] Task: Conductor - User Manual Verification 'Phase 2: Core Implementation' (Protocol in workflow.md)

## Phase 3: Integration Testing [checkpoint: 99b20b1]
- [x] Task: Create comprehensive integration tests for Project API [a7b1067]
    - [x] Implement `ProjetoIntegrationTest` covering CRUD operations.
    - [x] Verify validation and error handling for projects.
- [x] Task: Conductor - User Manual Verification 'Phase 3: Integration Testing' (Protocol in workflow.md)
