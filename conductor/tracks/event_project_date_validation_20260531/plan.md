# Plan: Event Project Period Validation

## Phase 1: Backend API Validation
- [x] Task: Write failing unit/integration tests in the backend (`gestao-paroquia`) to assert validation errors when linking events to projects with mismatched dates [728787a]
- [x] Task: Implement the validation logic in the Event creation and update service layer in the backend [728787a]
- [x] Task: Verify that all backend tests pass cleanly [728787a]
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Backend API Validation' (Protocol in workflow.md)

## Phase 2: Frontend UI Validation
- [ ] Task: Write failing tests in the frontend (`gestao-paroquia-ui`) to assert that the event registration modal displays validation errors when dates fall outside the project period
- [ ] Task: Implement the validation logic, inline messages, and save button restriction in the event registration modal (`EventRegistrationModal.tsx`)
- [ ] Task: Verify that all frontend tests pass cleanly
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Frontend UI Validation' (Protocol in workflow.md)
