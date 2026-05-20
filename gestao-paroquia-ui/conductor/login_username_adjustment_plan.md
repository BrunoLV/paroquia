# Plan: Adjust Login from Email to Username

## Objective
Update the Gestão Paróquia UI to authenticate users using their `username` instead of an `email` address. This aligns the frontend with the backend Identity and Access Management (IAM) implementation.

## Implementation Steps

### 1. Update `src/auth/AuthContext.tsx`
*   Change the `email` property to `username` in the `User` interface.
*   Update the `login` function signature from `login(email, password)` to `login(username, password)`.
*   Update the payload sent in the `apiClient.post` call to `{ username, password }`.

### 2. Update `src/pages/Login.tsx`
*   Rename the state variable `email` to `username`.
*   Update the `<TextField>` for the username: change `id`, `name`, and `autoComplete` to "username", and `label` to "Nome de Usuário".
*   Update the `login` function call to pass `username`.

### 3. Update Tests
*   Update `mockUser` definitions in `src/auth/AuthContext.test.tsx`, `src/pages/Login.test.tsx`, and `src/auth/ProtectedRoute.test.tsx` to use `username: '...'`.
*   Update queries in `Login.test.tsx` to look for the new label "Nome de Usuário".

## Verification & Testing
*   Start the development server (`npm start`).
*   Verify the login page displays "Nome de Usuário".
*   Log in using a test account.
*   Run `npm test` to ensure tests pass.