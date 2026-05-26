# Implementation Plan: Event Recurrence Implementation

## Phase 1: Backend Enhancements
- [x] Task: Update `EventoEditScope` enum to include `EDIT_ALL`. 1f7618b
- [x] Task: Update `RegraRecorrencia` record and `EventoRecorrenciaRequest` to include `Integer numeroOcorrencias`. 7d0018d
- [x] Task: Implement `DIARIA` frequency logic and `numeroOcorrencias` limit in `RegraRecorrencia.gerarDatas`. ccb0598
- [x] Task: Update `UpdateEventoUseCase` to handle `EDIT_ALL` scope by updating all associated entities sharing the same recurrence ID. fcfed38
- [x] Task: Write unit tests for `RegraRecorrencia` and `UpdateEventoUseCase` (Edit All). ccb0598
- [x] Task: Conductor - User Manual Verification 'Phase 1: Backend Enhancements' (Protocol in workflow.md) 50e9213

## Phase 2: Frontend Recurrence Form
- [x] Task: Create a new `RecurrenceForm` component to be used inside the `EventRegistrationModal`. ac4f28b
- [x] Task: Add UI fields for Frequency, Interval, Days of Week, and Termination (Never, Date, Count). ac4f28b
- [x] Task: Integrate `RecurrenceForm` into `EventRegistrationModal` with a toggle/expandable section. 2fd8105
- [x] Task: Update `EventRegistrationModal` to call `PUT /api/v1/eventos/{id}/recorrencia` after a successful event creation if recurrence is enabled. 2fd8105
- [x] Task: Conductor - User Manual Verification 'Phase 2: Frontend Recurrence Form' (Protocol in workflow.md) 3176b06

## Phase 3: Frontend Edit Workflow
- [x] Task: Create a `EditScopeDialog` component to prompt the user when editing a recurring event. 3fcd7fe
- [x] Task: Update the "Edit" action in the Calendar to show the `EditScopeDialog` if the event has a `recorrenciaId`. 3fcd7fe
- [x] Task: Update the `PATCH` call to pass the selected `editScope` in the request body. 3fcd7fe
- [x] Task: Conductor - User Manual Verification 'Phase 3: Frontend Edit Workflow' (Protocol in workflow.md) 3fcd7fe

## Phase 4: Verification and Polish
- [x] Task: Verify recurring events generation via `YearlyRecurrenceGeneratorJob` (or triggering it manually in dev). ba312da
- [x] Task: Final end-to-end testing of creating, editing (all/future), and deleting recurring events. ba312da
- [x] Task: Conductor - User Manual Verification 'Phase 4: Verification and Polish' (Protocol in workflow.md) ba312da
- [x] Task: Implement base event re-anchoring to first valid occurrence and separate duration UI. (User requested refinement). 3020612
- [x] Task: Final code review and standard import cleanup. 3020612