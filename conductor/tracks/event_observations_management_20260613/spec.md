# Specification: Event Observations Management

## Overview
This track implements the frontend capabilities to view, add, edit, and remove observations (notes/logs) associated with events. The backend API is already implemented (under `/api/v1/eventos/{eventoId}/observacoes`) and enforces validation rules: manual creation/editing/deletion is only allowed for manual notes of type `NOTA`, and editing/deleting requires the user to be the original author.

## Functional Requirements
1. **View Observations in Event Details Dialog**:
   - When the user opens the event details modal, query `GET /api/v1/eventos/{eventoId}/observacoes` to retrieve all notes.
   - Display them in a new section at the bottom of the details modal.
   - Differentiate visually between user notes (`tipo: NOTA`) and system-generated logs (e.g., `CANCELAMENTO`, `APROVACAO`, `REPROVACAO`). Use distinct styling or chips to clarify.
   - For user notes, show the author (if the author is the currently logged-in user, display "Você", otherwise display "Usuário").
   - Display the creation date/time formatted appropriately in local time.

2. **Add New Note**:
   - Directly below the list of observations, display a simple text input box (with a placeholder like "Adicione uma nota...") and an "Adicionar" button.
   - When submitted, call `POST /api/v1/eventos/{eventoId}/observacoes` with `tipo: "NOTA"` and the typed content.
   - Upon success, clear the input box and reload/refresh the observations list.

3. **Edit Note**:
   - For any note of type `NOTA` where the logged-in user is the author (`usuarioId === currentUserId`), display an edit button (icon).
   - Clicking edit replaces the note content with a text field.
   - Saving calls `PATCH /api/v1/eventos/{eventoId}/observacoes/{observacaoId}` with the updated content.
   - Upon success, reload/refresh the observations list.

4. **Delete Note**:
   - For any note of type `NOTA` where the logged-in user is the author (`usuarioId === currentUserId`), display a delete/remove button (icon).
   - Clicking delete triggers a confirmation dialog.
   - Upon confirmation, call `DELETE /api/v1/eventos/{eventoId}/observacoes/{observacaoId}`.
   - Upon success, reload/refresh the observations list.

## Non-Functional Requirements
- **Responsive Layout**: The notes section must fit cleanly in the event details modal on all screen sizes, including mobile.
- **Error Handling**: Standardize error presentations. If a note operation fails, display a clear error message.

## Acceptance Criteria
- [ ] Opening event details displays all existing observations (both manual and system).
- [ ] Users can type and add a note; it appears in the list instantly.
- [ ] Edit/delete icons only appear for notes created by the current user of type `NOTA`.
- [ ] Editing a note updates its text in the list.
- [ ] Deleting a note removes it from the list after confirmation.
- [ ] System notes (like cancellation reason) are displayed but cannot be edited or deleted.
- [ ] Automated tests cover hook fetching and component rendering/interactions.

## Out of Scope
- Listing all observations globally outside of the specific event context.
- Resolving the full name/username of other users (they will be labeled as "Usuário").
