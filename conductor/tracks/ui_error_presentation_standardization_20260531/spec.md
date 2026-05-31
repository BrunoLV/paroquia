# Specification: UI Error Presentation Standardization

## Overview
This track defines the standard for presenting validation, informational, success, and error messages across the entire Gestão Paroquial React application (`gestao-paroquia-ui`). 
Previously, error and validation messages were shown inconsistently: some screens used local alerts, some used toast notifications, and forms relied on toasts/alerts for inline field validations. 

This track establishes a unified user experience:
1. **Form/Input Field Validation Errors**: Exhibited directly under the input fields using standard MUI `helperText` and `error` properties, styled with a consistent red color (Nossa Senhora de Fátima visual palette).
2. **Global Operations and API Errors/Successes**: Exhibited exclusively via the floating Toast (Snackbar) provider (`NotificationContext`).
3. **Multiple API/Server Validation Errors**: Grouped and presented in a single Toast notification formatted as a readable bulleted list using line breaks.

This standard will be implemented and verified across all pages and modals: `Login`, `Dashboard`, `Calendar`, `EventRegistrationModal`, `Projects`, and `Approvals`.

## Functional Requirements

### 1. Toast Notification Layout and Formatting (`NotificationContext.tsx`)
- Enhance the global `NotificationContext` (`Alert` component inside `Snackbar`) to support multi-line text wraps by setting the CSS property `whiteSpace: 'pre-line'`.
- Set default duration to `5000ms`.

### 2. Standardized Form Field Validation
Forms must not trigger a Toast or modal alert for local validation errors (e.g., missing required fields, date mismatches). Instead:
- Set specific error states for each input.
- Render helper text directly under the invalid field: e.g., `<TextField error={!!errors.title} helperText={errors.title} ... />`.
- Clear the field-level error as soon as the user edits or corrects the field.

### 3. Standardized API Validation Error Presentation
- When the API returns a bad request with field validation details (`data.errors` array containing field and message), the client must catch the error, format it into a bulleted list starting with `"Por favor, corrija os seguintes erros:"`, and display it in a single Toast notification:
  ```text
  Por favor, corrija os seguintes erros:
  • Campo A: Mensagem de erro A
  • Campo B: Mensagem de erro B
  ```
- If the error contains a single message or is unmapped, fallback to `showError(err.response?.data?.message || 'Erro inesperado')`.

### 4. Screen-by-Screen Implementation Scope

#### A. Login Screen (`Login.tsx`)
- Implement inline field validation for Username and Password.
- Show helper text under Username if empty ("O nome de usuário é obrigatório.").
- Show helper text under Password if empty ("A senha é obrigatória.").
- Do not make a request or call `showError` for client-side empty fields.
- Keep the Toast error for invalid credentials API failures.

#### B. Projects Screen (`Projects.tsx`)
- Replace current `showError` calls for client-side form validation (Nome, Organização, Datas) with inline helper texts under their respective fields.
- Convert the catch block to parse multiple API errors and display them as a bulleted list in a single Toast notification.

#### C. Event Registration Modal (`EventRegistrationModal.tsx`)
- Replace current `showError` calls for form fields (Título, Organização, Início, Fim/Recorrência) with inline helper texts under the respective inputs.
- Replace the inline `Alert` banner for project date conflicts (`projectValidationError`) with an inline warning badge or helper text in the project field, and prevent saving with proper helper text, standardizing the validation messages.
- Format server-side event creation/update validation failures into a bulleted list in a single Toast.

#### D. Calendar Screen (`Calendar.tsx`)
- Remove the local `successMsg` and `errorMsg` state variables and their corresponding `<Alert>` component blocks.
- Redirect all calendar data loading, event creation, editing, and cancellation success or error messages to `showSuccess` or `showError` from `NotificationContext`.

#### E. Approvals Screen (`Approvals.tsx`)
- Keep inline `Alert` only for data loading errors where there is no UI to show.
- Ensure API action errors (approving/rejecting) are caught and displayed via `showError` using the standardized Toast.

## Non-Functional Requirements
- **Consistency**: Keep identical border radius (0px, square theme), fonts (MUI standard), and crimson/red highlights for error states.
- **A11y**: Ensure screen readers announce form helper texts using proper aria-describedby associations (handled automatically by MUI's `TextField`).

## Acceptance Criteria
- [ ] UI contains no raw inline alert banners for form validation errors.
- [ ] Inputs in `Login.tsx`, `Projects.tsx`, and `EventRegistrationModal.tsx` display red helper texts when empty/invalid upon submit.
- [ ] Local Alert banners in `Calendar.tsx` are removed; all actions trigger the floating Toast notifications.
- [ ] API validation failures with multiple field errors are presented as a multi-line bulleted list in a single Toast.
- [ ] All existing test suites pass, and updated/new tests are added to verify standard error presentations.

## Out of Scope
- Backend modification to error response payloads.
- Modifying structural status components (like pending approval warnings inside details dialogs).
