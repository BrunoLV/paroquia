# Implementation Plan: Approval Workflow UI Integration

## Phase 1: API Integration and Route Protection
- [x] Task: TDD - Define API client functions and mock handlers for Approvals in `src/api/` e52b3e9
    - [x] Write failing test for approvals API endpoints (`GET /api/v1/aprovacoes`, `PATCH /api/v1/aprovacoes/{id}`)
    - [x] Implement client integration in `src/api/` using axios/apiClient
    - [x] Verify test passes and mock returns correct structure
- [x] Task: TDD - Implement Route Guarding for Approvals page in `src/AppRoutes.tsx` cf4810e
    - [x] Write failing routing tests ensuring only ADMIN, CONSELHO_COORDENADOR, and PAROCO can access `/aprovacoes`
    - [x] Implement role-based navigation and route protection using the active organization context and user roles
    - [x] Verify tests pass and unauthorized users are redirected
- [x] Task: Conductor - User Manual Verification 'Phase 1: API Integration and Route Protection' (Protocol in workflow.md)

## Phase 2: Approvals Page Listing and Menu Integration [checkpoint: 6e34627]
- [x] Task: TDD - Add "Aprovações" link in `src/components/Layout.tsx` f360c33
    - [x] Write failing test to check link visibility only for authorized roles (ADMIN, CONSELHO_COORDENADOR, PAROCO)
    - [x] Implement conditional rendering of the sidebar menu link based on user roles
    - [x] Verify link is shown/hidden correctly under different roles
- [x] Task: TDD - Create the Approvals List component and Page layout in `src/pages/Approvals.tsx` 0ff9e99
    - [x] Write failing tests verifying the page renders a paginated list of pending approvals
    - [x] Implement `Approvals` page utilizing React Query to load pending approvals from backend
    - [x] Implement status filtering UI controls (tabs/filters for PENDENTE, APROVADA, REPROVADA, etc.)
    - [x] Verify tests pass and status switching works correctly
- [x] Task: Conductor - User Manual Verification 'Phase 2: Approvals Page Listing and Menu Integration' (Protocol in workflow.md)

## Phase 3: Approvals Detail & Decisions Workflow
- [x] Task: TDD - Create Detail Panel/Drawer component in approvals view bc3e4ba
    - [x] Write failing tests verifying details dialog/drawer displays solicitor info, request type, time, and event description
    - [x] Implement details visualization component
    - [x] Verify tests pass
- [~] Task: TDD - Implement Approve and Reject actions with observations
    - [ ] Write failing tests for patch approval/rejection request with observation field validation
    - [ ] Implement action buttons with a text field for observation (limit 2000 characters)
    - [ ] Send patch decision request to backend and refresh list state
    - [ ] Verify decision workflow works and list is updated
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Approvals Detail & Decisions Workflow' (Protocol in workflow.md)

## Phase 4: Calendar Integration and Visual Indicators
- [ ] Task: TDD - Update Event status rendering in Calendar
    - [ ] Write failing tests verifying events with pending status are visually distinguished on the calendar
    - [ ] Update `Calendar.tsx` to identify pending events (e.g. check status or flag) and style them with opacity and a "Pendente" indicator
    - [ ] Update the event details tooltip/popover inside the calendar to display a clear warning if the event is pending coordination approval
    - [ ] Verify tests pass and pending events are styled appropriately
- [ ] Task: Conductor - User Manual Verification 'Phase 4: Calendar Integration and Visual Indicators' (Protocol in workflow.md)
