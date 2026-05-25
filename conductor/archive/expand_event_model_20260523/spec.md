# Specification: Expand Event Registration Model

## Overview
This track aims to expand the Event Registration Modal in the React UI to include all remaining fields supported by the backend data model. This ensures that users can fully define an event's properties, including its physical location, associated project, and supporting organizations.

## Functional Requirements
1.  **Local (Physical Space):**
    *   Add a new field to the event registration and editing form to select a `Local` (`localId`).
    *   The field must be a simple dropdown listing all active locations.
    *   The UI must fetch the list of available locations from the backend (`/api/v1/locais`).
2.  **Projetos (Projects):**
    *   Add an optional dropdown field to select a `Projeto` (`projetoId`).
    *   The UI must fetch the list of active projects from the backend (e.g., `/api/v1/projetos`).
3.  **Organizações de Apoio (Participantes):**
    *   Add a multi-select field to choose supporting organizations (`participantes`).
    *   This field will allow selecting multiple organizations from the already loaded organizations list.
    *   The organization currently selected as the "Organização Responsável" should ideally be disabled or excluded from this multi-select list.

## Integration & API
*   Update the API payload in `EventRegistrationModal.tsx` to include `localId`, `projetoId`, and `participantes` when calling `POST` or `PATCH` on `/api/v1/eventos`.
*   *Note:* If `localId` is not currently exposed in the backend's `CreateEventoRequest` / `UpdateEventoRequest` DTOs (despite existing in the Entity), the backend DTOs and Controller must be updated to accept it.

## Out of Scope
*   Creating new locations or projects directly from within the event modal.