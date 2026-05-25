# Implementation Plan: Expand Event Registration Model

## Phase 1: Backend API Validation and Updates
- [x] Task: Check backend `CreateEventoRequest`, `UpdateEventoRequest`, and `EventoResponse` DTOs. If `localId` is missing, write failing unit tests for the use cases to expect it. a6ca5cd
- [x] Task: Update the backend DTOs, Mappers, and Entity logic to properly accept, save, and return `localId`. b8e1f2a
- [x] Task: Conductor - User Manual Verification 'Phase 1: Backend API Validation and Updates' (Protocol in workflow.md) 99e90b3

## Phase 2: Frontend Data Fetching Setup
- [x] Task: Write failing tests for the UI components to verify they attempt to fetch data from `/api/v1/locais` and `/api/v1/projetos`. b2d62dd
- [x] Task: Create or update API client functions to fetch Locais and Projetos. (Using apiClient directly with new shared types). 57475f5
- [x] Task: Update the state management in the components that host the `EventRegistrationModal` (or within the modal itself) to fetch and store these lists. 57475f5
- [~] Task: Conductor - User Manual Verification 'Phase 2: Frontend Data Fetching Setup' (Protocol in workflow.md)

## Phase 3: UI Component Implementation
- [ ] Task: Write failing component tests for `EventRegistrationModal.tsx` verifying the presence and interaction of the new dropdowns and multiselect.
- [ ] Task: Add the "Local" (Physical Space) simple dropdown to the modal form.
- [ ] Task: Add the "Projeto" optional dropdown to the modal form.
- [ ] Task: Add the "Organizações de Apoio" (Participantes) multiselect field to the modal form.
- [ ] Task: Update the `onSubmit` handler in `EventRegistrationModal.tsx` to map and include `localId`, `projetoId`, and `participantes` in the payload sent to the parent.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: UI Component Implementation' (Protocol in workflow.md)