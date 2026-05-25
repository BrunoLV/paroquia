# Specification: Event Recurrence Implementation (UI & Backend)

## Overview
This track implements a complete event recurrence system, expanding the current backend capabilities and providing a user-friendly UI in the React application. Users will be able to create repeating events (daily, weekly, monthly) with flexible termination rules and manage changes to repeating series.

## Functional Requirements

### 1. Backend Enhancements
*   **Recurrence Rules**: Update `RegraRecorrencia` and `EventoRecorrenciaRequest` to include a `numeroOcorrencias` (Count) field.
*   **Occurrence Logic**: Update `RegraRecorrencia.gerarDatas` and `YearlyRecurrenceGeneratorJob` to respect both `dataLimite` and `numeroOcorrencias`.
*   **Edit Scope**: Implement support for `EDIT_ALL` in the `EventoEditScope` enum and update `UpdateEventoUseCase` to handle updating all instances in a series.
*   **Daily Recurrence**: Ensure `RegraRecorrencia.gerarDatas` correctly handles `DIARIA` frequency.

### 2. Frontend UI
*   **Registration Modal**:
    *   Add a "Repetir Evento" section.
    *   Fields: Frequency (Daily, Weekly, Monthly), Interval ("A cada X"), Days of Week (for Weekly/Monthly).
    *   Termination: "Nunca", "Até uma data", "Após X ocorrências".
*   **Edit Workflow**:
    *   When editing a recurring event, show a dialog asking: "Apenas este evento", "Este e os próximos", "Todos os eventos da série".
    *   Send the selected `editScope` to the `PATCH /api/v1/eventos/{id}` endpoint.
*   **API Integration**:
    *   After event creation, if recurrence is set, call `PUT /api/v1/eventos/{id}/recorrencia`.
    *   Update event details to show recurrence rule summary.

## Acceptance Criteria
*   User can create a weekly event on Tuesdays for 10 occurrences.
*   User can edit all titles of a recurring series at once.
*   User can set an event to repeat daily with no end date.
*   Recurrence options are hidden by default and expandable.