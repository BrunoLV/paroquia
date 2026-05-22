# Implementation Plan: User Organization Context in Session

## Phase 1: Backend API Update [checkpoint: 6f8dbc7]
- [x] Task: Update the `Usuario` and `UsuarioResponseDTO` models in `gestao-paroquia` to include a list of `Organizacao` associations. f2c9270
- [x] Task: Update the `/auth/me` endpoint logic to fetch and map the user's organizations into the response payload. f2c9270
- [x] Task: Write/update unit and integration tests in the backend to ensure the organizations are returned correctly for authenticated users. f2c9270
- [x] Task: Conductor - User Manual Verification 'Phase 1: Backend API Update' (Protocol in workflow.md) 6f8dbc7

## Phase 2: Frontend AuthContext Updates [checkpoint: 3a61267]
- [x] Task: Update the `User` interface in `gestao-paroquia-ui/src/auth/AuthContext.tsx` to include an `organizations` array. be0ef88
- [x] Task: Update the login and session restoration logic in `AuthContext` to correctly parse and store the organizations from the backend response. 2c46799
- [x] Task: Update frontend unit tests for `AuthContext` to verify the new organization state. 2c46799
- [x] Task: Conductor - User Manual Verification 'Phase 2: Frontend AuthContext Updates' (Protocol in workflow.md) 3a61267

## Phase 3: UI Form Restrictions
- [x] Task: Update the `EventRegistrationModal` component to filter the available organizations in the "Responsible Organization" dropdown. If the user is not an `ADMIN`, only display the organizations present in `user.organizations`. 0cf9e60
- [x] Task: Update the unit tests for `EventRegistrationModal` to verify the dropdown options restrict correctly based on the user's roles and organizations. 0cf9e60
- [x] Task: Conductor - User Manual Verification 'Phase 3: UI Form Restrictions' (Protocol in workflow.md) 95dc30a
- [x] Bug Fix: Fixed empty organization list in Dashboard modal. cfaf9b5

## Phase 4: UI Filters and Dashboard Data
- [x] Task: Update the `Calendar.tsx` page to restrict the organization filter dropdown to the user's accessible organizations. cc37947
- [x] Task: Update data fetching logic in `Calendar.tsx` (which also affects Dashboard widgets) to implicitly filter by the user's organizations if they are not an `ADMIN` and no specific filter is selected. cc37947
- [x] Task: Update relevant frontend tests to ensure the filtering logic works as expected. cc37947
- [ ] Task: Conductor - User Manual Verification 'Phase 4: UI Filters and Dashboard Data' (Protocol in workflow.md)