# Plan: UI Error Presentation Standardization

## Phase 1: Notification Context and Core Pages Standardization
- [x] Task: Update the global NotificationContext Alert styling to support multi-line text wraps [f675de7]
    - [x] Add `whiteSpace: 'pre-line'` to the Alert styling inside `NotificationProvider` in `NotificationContext.tsx` [f675de7]
    - [x] Update `NotificationContext.test.tsx` to verify standard rendering [f675de7]
- [ ] Task: Standardize error and success messages in Calendar.tsx
    - [ ] Remove `errorMsg` and `successMsg` local states and their corresponding `<Alert>` component blocks from `Calendar.tsx`
    - [ ] Replace local alerts with `showSuccess` or `showError` calls from `NotificationContext` for calendar load and action callbacks
    - [ ] Verify `Calendar.test.tsx` passes and update if necessary
- [ ] Task: Standardize inline validation and API error presentation in Login.tsx
    - [ ] Add username and password validation error states to `Login.tsx`
    - [ ] Render helper text under `username` input when empty on submit ("O nome de usuário é obrigatório.")
    - [ ] Render helper text under `password` input when empty on submit ("A senha é obrigatória.")
    - [ ] Clear field-specific error states when inputs are edited
    - [ ] Ensure credentials validation errors from API continue to use global Toast
    - [ ] Update `Login.test.tsx` to verify inline helper texts and API error handling
- [ ] Task: Standardize error presentation in Approvals.tsx
    - [ ] Ensure that decision actions (approve/reject) only use the standardized Toast (`showError` / `showSuccess`) for errors and successes
    - [ ] Ensure `Approvals.test.tsx` passes and check for any remaining local alerts
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Notification Context and Core Pages Standardization' (Protocol in workflow.md)

## Phase 2: Dialog Forms and Modals Inline Validation
- [ ] Task: Standardize inline validations and API errors in Projects.tsx
    - [ ] Add validation states (nome, orgId, inicio, fim) in `Projects.tsx`
    - [ ] Replace `showError` alerts with inline helper texts under their respective form fields for client-side validation
    - [ ] Clear error states when fields are updated by the user
    - [ ] Update the `catch` block in `handleSave` to format multiple API validation errors into a bulleted list starting with `"Por favor, corrija os seguintes erros:"` separated by newlines
    - [ ] Verify `Projects.test.tsx` passes and update tests to cover inline helper texts
- [ ] Task: Standardize inline validations and API errors in EventRegistrationModal.tsx
    - [ ] Add validation states (titulo, orgId, inicio, fim, recorrencia) in `EventRegistrationModal.tsx`
    - [ ] Replace `showError` alerts with inline helper texts under their respective fields for client-side validation
    - [ ] Display `projectValidationError` inline as a helper text on the project field instead of an inline error `Alert` block
    - [ ] Clear error states when fields are updated by the user
    - [ ] Update event submission catch block to format multiple API validation errors into a bulleted list in a single Toast notification
    - [ ] Verify `EventRegistrationModal.test.tsx` passes and update tests to cover inline helper texts and Toast notifications
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Dialog Forms and Modals Inline Validation' (Protocol in workflow.md)
