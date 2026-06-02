# Plan: Event Project Period Validation

## Phase 1: Backend API Validation [checkpoint: 1b2255e]
- [x] Task: Write failing unit/integration tests in the backend (`gestao-paroquia`) to assert validation errors when linking events to projects with mismatched dates [728787a]
- [x] Task: Implement the validation logic in the Event creation and update service layer in the backend [728787a]
- [x] Task: Verify that all backend tests pass cleanly [728787a]
- [x] Task: Conductor - User Manual Verification 'Phase 1: Backend API Validation' (Protocol in workflow.md) [1b2255e]

## Phase 2: Frontend UI Validation [checkpoint: e057f9c]
- [x] Task: Write failing tests in the frontend (`gestao-paroquia-ui`) to assert that the event registration modal displays validation errors when dates fall outside the project period [abf74dd]
- [x] Task: Implement the validation logic, inline messages, and save button restriction in the event registration modal (`EventRegistrationModal.tsx`) [abf74dd]
- [x] Task: Verify that all frontend tests pass cleanly [abf74dd]
- [x] Task: Conductor - User Manual Verification 'Phase 2: Frontend UI Validation' (Protocol in workflow.md) [e057f9c]
