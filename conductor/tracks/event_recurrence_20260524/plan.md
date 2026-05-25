# Implementation Plan: Event Recurrence Implementation

## Phase 1: Backend Enhancements
- [x] Task: Update `EventoEditScope` enum to include `EDIT_ALL`. 1f7618b
- [x] Task: Update `RegraRecorrencia` record and `EventoRecorrenciaRequest` to include `Integer numeroOcorrencias`. 7d0018d
- [x] Task: Implement `DIARIA` frequency logic and `numeroOcorrencias` limit in `RegraRecorrencia.gerarDatas`. ccb0598
- [x] Task: Update `UpdateEventoUseCase` to handle `EDIT_ALL` scope by updating all associated entities sharing the same recurrence ID. fcfed38
- [x] Task: Write unit tests for `RegraRecorrencia` and `UpdateEventoUseCase` (Edit All). ccb0598
- [~] Task: Conductor - User Manual Verification 'Phase 1: Backend Enhancements' (Protocol in workflow.md)

## Phase 2: Frontend Recurrence Form
- [ ] Task: Create a new `RecurrenceForm` component to be used inside the `EventRegistrationModal`.
- [ ] Task: Add UI fields for Frequency, Interval, Days of Week, and Termination (Never, Date, Count).
- [ ] Task: Integrate `RecurrenceForm` into `EventRegistrationModal` with a toggle/expandable section.
- [ ] Task: Update `EventRegistrationModal` to call `PUT /api/v1/eventos/{id}/recorrencia` after a successful event creation if recurrence is enabled.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Frontend Recurrence Form' (Protocol in workflow.md)

## Phase 3: Frontend Edit Workflow
- [ ] Task: Create a `EditScopeDialog` component to prompt the user when editing a recurring event.
- [ ] Task: Update the "Edit" action in the Calendar to show the `EditScopeDialog` if the event has a `recorrenciaId`.
- [ ] Task: Update the `PATCH` call to pass the selected `editScope` in the request body.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Frontend Edit Workflow' (Protocol in workflow.md)

## Phase 4: Verification and Polish
- [ ] Task: Verify recurring events generation via `YearlyRecurrenceGeneratorJob` (or triggering it manually in dev).
- [ ] Task: Final end-to-end testing of creating, editing (all/future), and deleting recurring events.
- [ ] Task: Conductor - User Manual Verification 'Phase 4: Verification and Polish' (Protocol in workflow.md)