# Implementation Plan: Event Editing and Cancellation UI

## Phase 1: UI Permission Logic
- [x] Task: Write failing tests for a utility function (e.g., `canManageEvent`) that determines if a user can edit/cancel an event based on their roles and the event's organization. 2f8521e
- [x] Task: Implement the `canManageEvent` utility function to match the backend's authorization rules. 2f8521e
- [ ] Task: Conductor - User Manual Verification 'Phase 1: UI Permission Logic' (Protocol in workflow.md)

## Phase 2: Adapting EventRegistrationModal for Editing
- [ ] Task: Write failing tests for `EventRegistrationModal` to ensure it correctly handles an 'edit mode' (e.g., pre-filling data, changing modal title to "Editar Evento").
- [ ] Task: Update `EventRegistrationModal` to accept an optional existing event object and populate its initial state accordingly.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Adapting EventRegistrationModal for Editing' (Protocol in workflow.md)

## Phase 3: Cancellation Flow
- [ ] Task: Write failing tests for a new `EventCancelDialog` component, ensuring the reason field is mandatory.
- [ ] Task: Implement the `EventCancelDialog` component using Material UI Dialog.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Cancellation Flow' (Protocol in workflow.md)

## Phase 4: Calendar Integration and API Calls
- [ ] Task: Write failing tests for the Calendar page to verify that Edit/Cancel buttons are conditionally rendered in the Details Modal based on permissions.
- [ ] Task: Update the Calendar page to integrate the 'edit mode' of the `EventRegistrationModal` and handle the `PATCH` API call.
- [ ] Task: Update the Calendar page to integrate the `EventCancelDialog` and handle the `POST /{eventoId}/cancel` API call.
- [ ] Task: Conductor - User Manual Verification 'Phase 4: Calendar Integration and API Calls' (Protocol in workflow.md)