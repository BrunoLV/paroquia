# Track Specification: Migrate Authentication to Spring Session

## Overview
This track focuses on migrating the React application's authentication mechanism from a stateless JWT approach to a stateful, cookie-based session approach using Spring Session. This aligns the frontend with the existing backend configuration in `@gestao-paroquia`.

## Functional Requirements
- Configure Axios to send and receive cookies (`withCredentials: true`).
- Remove JWT token extraction and storage from the login process.
- Remove the Axios request interceptor that injects the `Authorization` header.
- Update `AuthContext` to fetch the current user profile from `/api/v1/auth/me` on application load to verify session validity.
- Update the login function to handle the JSON response containing user details (without a token).
- Implement the logout function to call the backend `/api/v1/auth/logout` endpoint to invalidate the server-side session.

## Non-Functional Requirements
- Maintain >80% test coverage using TDD.
- Ensure all existing tests are updated to reflect the new cookie-based architecture.

## Acceptance Criteria
- User can successfully log in and a session cookie (`JSESSIONID`) is managed by the browser.
- The application correctly identifies an active session upon page reload via the `/me` endpoint.
- User can successfully log out, invalidating the session on both the client and server.
- The application redirects to `/login` when a 401 Unauthorized response is received (e.g., session expired).