# Specification: Event Editing and Cancellation UI

## Overview
Implement the ability for authorized users to edit and cancel existing events directly from the `gestao-paroquia-ui` application. This feature must strictly adhere to the authorization rules and workflows already established in the backend API.

## Functional Requirements
- **Entry Points:** 
  - An 'Edit' option must be accessible directly on the calendar event pill (e.g., via a small edit icon or right-click menu, if applicable, or just rely on the Details Modal). *Refinement: Since pills are small, a clear 'Edit' button inside the existing Details Modal is the primary path, with an optional quick-action on the pill if space permits.*
  - A 'Cancel' option must be accessible from the Details Modal or the Edit Modal.
- **Editing UI:** 
  - Reuse and adapt the existing `EventRegistrationModal` to support an 'Edit Mode'.
  - When in Edit Mode, the form must pre-fill with the selected event's current data.
  - Submissions in Edit Mode should use the `PATCH /api/v1/eventos/{eventoId}` endpoint.
- **Cancellation Flow:** 
  - Clicking 'Cancel Event' must open a secondary Confirmation Dialog.
  - This dialog MUST include a mandatory text input for the "Cancel Reason" (Justificativa), as required by the backend API.
  - Submissions should use the resilient `POST /api/v1/eventos/{eventoId}/cancel` endpoint.
- **UI Permissions (Authorization):**
  - The UI must evaluate the current user's roles against the event's `organizacaoResponsavelId`.
  - If the user is an `ADMIN` or holds an editor role (e.g., `COORDENADOR`, `VICE_COORDENADOR`) for that specific organization, the Edit and Cancel buttons should be visible.
  - If the user lacks these permissions, the Edit and Cancel buttons MUST be completely hidden from the UI.

## Non-Functional Requirements
- **State Management:** Leverage React Query / API Client state to invalidate the calendar data cache upon successful edit or cancellation, ensuring the view updates immediately.
- **Validation:** Ensure the cancellation reason field is validated as required on the client side before allowing the API call.

## Acceptance Criteria
- [ ] An authorized user can open the edit modal and see the event's current data pre-filled.
- [ ] Submitting the edit form successfully updates the event and refreshes the calendar view.
- [ ] An authorized user can click 'Cancel', provide a mandatory reason in a confirmation dialog, and successfully cancel the event.
- [ ] Users without permission for a specific event cannot see the Edit or Cancel buttons for that event.
- [ ] The resilient `POST .../cancel` endpoint is used for cancellations instead of the deprecated `DELETE`.

## Out of Scope
- Modifying backend API permission rules or endpoints.
- Managing recurring event series (editing a single instance vs the whole series) if not already supported explicitly by the backend PATCH endpoint.