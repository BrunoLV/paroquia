# Specification: User Organization Context in Session

## Overview
Ensure that when a user logs in, the system correctly identifies and loads the organizations the user belongs to. This context must be available throughout the session to enforce data isolation, UI presentation rules, and access control. Users with global permissions (like `ADMIN`) will bypass organizational restrictions.

## Functional Requirements
- **Backend Updates (`gestao-paroquia`):**
  - Update the authentication payload and/or the `/auth/me` endpoint to include a list of `organizations` the user belongs to.
  - Ensure the user entity has a relation to their associated organizations.
- **Frontend Session State (`gestao-paroquia-ui`):**
  - Update the `AuthContext` to store the user's organizations upon successful login and session recovery.
  - Implement utility functions to easily check if a user is an `ADMIN` or belongs to specific organizations.
- **UI Adjustments (Combined Context):**
  - **Form Restrictions:** In forms (e.g., `EventRegistrationModal`), the "Responsible Organization" dropdown must only list organizations the user belongs to. If the user is an `ADMIN`, list all active organizations.
  - **Filter Dropdowns:** In screens like the Calendar, organization filters should be limited to the user's accessible organizations.
  - **Dashboard Data:** Widgets (Upcoming Events, Pending Approvals) must implicitly filter their data requests by the user's organizations, ensuring they only see relevant data (unless `ADMIN`).
- **Role Permissions:**
  - `ADMIN` and other roles with global read/write permissions operate without organization constraints.
  - Standard users operate under a "Combined Context," meaning they act on behalf of *all* their associated organizations simultaneously without needing to manually toggle an active context.

## Non-Functional Requirements
- Maintain backwards compatibility for `ADMIN` flows.
- Ensure the API responses remain performant when joining organization data to the user session.

## Acceptance Criteria
- [ ] Backend API `/auth/me` includes the user's organizations.
- [ ] Frontend `AuthContext` correctly parses and stores the organizations.
- [ ] A non-admin user can only select their own organizations when creating an event.
- [ ] A non-admin user only sees their own organizations in calendar filters.
- [ ] An `ADMIN` user can see and select all active organizations in forms and filters.