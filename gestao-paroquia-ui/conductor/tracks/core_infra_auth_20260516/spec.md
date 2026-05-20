# Track Specification: Implement core React app infrastructure and authentication flow

## Overview
This track focuses on scaffolding the core React application using Create React App with TypeScript, setting up the basic routing and global state management, and implementing the authentication flow (login, token handling, protected routes) to integrate with the `@gestao-paroquia` API.

## Functional Requirements
- Initialize the React application with TypeScript.
- Set up Material UI for styling and component foundation.
- Implement React Query and Context API for state management.
- Create a Login page that authenticates users via the backend API.
- Secure the application using JWT (storage, interceptors for API calls).
- Implement protected routes that redirect unauthenticated users to the Login page.

## Non-Functional Requirements
- Maintain >80% test coverage using TDD.
- Ensure strict TypeScript typing and linting checks.
- Code should be modular and follow the defined styling guidelines.

## Acceptance Criteria
- Application compiles and runs locally without errors.
- Users can log in using valid credentials and receive a JWT.
- Unauthorized users cannot access protected routes (e.g., Dashboard).
- The `Authorization` header is automatically appended to outgoing API requests using an Axios/Fetch interceptor.