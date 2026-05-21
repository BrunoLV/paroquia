# Specification: Dashboard Screen

## Overview
Implement a new Dashboard screen as the primary landing page after user login. The dashboard will provide a unified, role-aware summary of parish activities, replacing the Calendar as the default initial view.

## Functional Requirements
- **Routing:**
  - Create a new route for the Dashboard at `/dashboard`.
  - Update the login redirection logic to navigate to `/dashboard` upon successful authentication.
  - Update the root path (`/`) to redirect to `/dashboard` instead of `/calendar`.
  - Ensure the Calendar remains accessible via the navigation menu at `/calendar`.
- **Dashboard Components:**
  - **Summary Cards:** Display key metrics (e.g., total upcoming events this month, pending approvals count).
  - **Quick Actions:** Provide quick access buttons for frequent tasks (e.g., "Novo Evento").
  - **Upcoming Events:** List the closest upcoming events relevant to the user.
  - **Pending Approvals:** Show a list of items requiring the user's attention/approval.
- **Role-Awareness (Unified Layout):**
  - All users share the same base dashboard layout.
  - The data populated within the components (events, approvals, metrics) must be filtered based on the user's permissions and role (e.g., Admins see all pending approvals, regular members only see their organization's data).

## Non-Functional Requirements
- Adhere to the existing Material UI design system and color palette.
- Ensure the dashboard is responsive and works well on both desktop and mobile views.

## Acceptance Criteria
- [ ] After logging in, the user is redirected to the `/dashboard` route.
- [ ] Navigating to the root `/` URL redirects the user to `/dashboard`.
- [ ] The dashboard renders Summary Cards, Quick Actions, Upcoming Events, and Pending Approvals components.
- [ ] The data shown in the dashboard accurately reflects the logged-in user's permissions.
- [ ] The Calendar page is still functional and accessible via the sidebar navigation.