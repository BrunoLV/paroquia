# Specification: Event Registration Screen

## Overview
Implement a new Event Registration screen in the `gestao-paroquia-ui` application. This interface will allow users to create new parish events directly from the Calendar view, providing a streamlined process for scheduling.

## Functional Requirements
- **Entry Point:** The registration form must be accessible from the Calendar view (e.g., via a "New Event" button or clicking on a specific date).
- **UI Pattern:** The form will be presented as a Modal/Dialog overlaying the Calendar, allowing users to keep context of their schedule while creating an event.
- **Form Fields:** The form must collect the following data:
  - **Basic Info:** Title, Date, Time, Description.
  - **Location:** Selection of a specific Parish space/room.
  - **Involvement:** Assignment of Pastorals/Movements and their respective roles in the event.
  - **Recurrence:** Configuration for repeating events (e.g., weekly, monthly).
- **Post-Submission:**
  - Upon successful creation, the modal should close.
  - A success toast/notification should be displayed to the user.
  - The underlying Calendar view should automatically refresh/update to show the newly created event.

## Non-Functional Requirements
- **UX/Accessibility:** The modal must be responsive, easily dismissible (e.g., via Escape key or backdrop click), and accessible via keyboard navigation.
- **State Management:** Use React Query for handling the API submission and cache invalidation for the calendar data.
- **Validation:** Form fields must have appropriate client-side validation before submission.

## Acceptance Criteria
- [ ] A user can open the Event Registration modal from the Calendar view.
- [ ] All specified fields (Basic Info, Location, Involvement, Recurrence) are present and functional.
- [ ] Submitting the form with valid data creates the event via the backend API.
- [ ] After successful creation, the modal closes, a success message is shown, and the calendar reflects the new event.
- [ ] Invalid data prevents submission and highlights errors.

## Out of Scope
- Implementing the backend API endpoints (assuming they exist or are handled in a separate track).
- Complex event approval workflows within this specific UI (if not covered by basic API interaction).