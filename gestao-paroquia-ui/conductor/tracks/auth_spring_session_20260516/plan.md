# Implementation Plan: Migrate Authentication to Spring Session

## Phase 1: API Client Adjustment [checkpoint: d7e1f0b]
- [x] Task: Configure Axios for Cookies and remove JWT Interceptor (d1ec18d)
    - [x] Update `interceptors.test.ts` to expect `withCredentials: true` and remove header injection tests.
    - [x] Update `src/api/client.ts` to enable `withCredentials` and remove the request interceptor.
- [x] Task: Conductor - User Manual Verification 'Phase 1: API Client Adjustment' (Protocol in workflow.md)

## Phase 2: AuthContext Migration [checkpoint: 6eaffcc]
- [x] Task: Update AuthContext Login and Logout (c39f2b1)
    - [x] Update `AuthContext.test.tsx` to remove token assertions and verify API calls to `/auth/logout`.
    - [x] Refactor `AuthContext.tsx` to remove `localStorage` token management and add API call for logout.
- [x] Task: Implement Session Verification on Load (32d64d4)
    - [x] Update `AuthContext.test.tsx` to verify the call to `/api/v1/auth/me` on initialization.
    - [x] Refactor `useEffect` in `AuthContext.tsx` to check session validity via the `/me` endpoint instead of `localStorage`.
- [x] Task: Conductor - User Manual Verification 'Phase 2: AuthContext Migration' (Protocol in workflow.md)

## Phase 3: Login View Adjustment [checkpoint: a6070d9]
- [x] Task: Refactor Login Integration (32d64d4)
    - [x] Ensure `Login.test.tsx` reflects the updated `login` function signature (if changed) and response handling.
    - [x] Refactor `src/pages/Login.tsx` if necessary to align with the new context behavior.
- [x] Task: Conductor - User Manual Verification 'Phase 3: Login View Adjustment' (Protocol in workflow.md)