# Specification: Fix Event Modals Missing Fields

## Overview
This track addresses two bugs reported after the expansion of the Event registration model. 
1. Supporting organizations (participantes) selected during event creation are not loaded when opening the event for editing.
2. The Event Details view (in the Calendar page) does not display the newly added fields: Local, Projeto, and Organizações de Apoio.

## Functional Requirements
1.  **Backend `EventoResponse` Update:**
    *   The `EventoResponse` DTO must include a `participantes` field (List of UUIDs).
    *   The `EventoMapper` must populate this list by extracting `EventoEnvolvidoEntity` records where the role is 'APOIO' (or equivalent supporting role).
2.  **Frontend Form Pre-filling (Edit Mode):**
    *   The `EventRegistrationModal` must pre-fill the `participantes` state when an existing event is passed via `initialEvent`.
3.  **Frontend Event Details View:**
    *   The Details Modal in `Calendar.tsx` must be updated to display the `Local` (if present).
    *   The Details Modal must display the `Projeto` (if present).
    *   The Details Modal must display the list of `Organizações de Apoio` (Participantes) as Chips (if present).

## Integration & API
*   No new endpoints. Modifies the payload returned by `GET /api/v1/eventos` and `GET /api/v1/eventos/{id}`.

## Out of Scope
*   Adding further new fields to the event model.