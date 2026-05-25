# Implementation Plan: Fix Event Modals Missing Fields

## Phase 1: Backend Fixes
- [x] Task: Update `EventoResponse` DTO to include `List<UUID> participantes`. Write/update unit tests to verify the mapping. 2fd8105
- [x] Task: Update `EventoMapper.java` to map `envolvidos` from `EventoEntity` to the `participantes` list in the response. 2fd8105
- [x] Task: Conductor - User Manual Verification 'Phase 1: Backend Fixes' (Protocol in workflow.md) db6d7fc

## Phase 2: Frontend UI Updates
- [x] Task: Update the `Evento` interface in `src/api/types.ts` to include `participantes?: string[]`. d1e6a9f
- [x] Task: Update `EventRegistrationModal.tsx` to pre-fill the `participantes` state from `initialEvent.participantes`. Update corresponding component tests. a9aa345
- [x] Task: Update the Event Details Dialog in `Calendar.tsx` to display 'Local', 'Projeto', and 'Organizações de Apoio'. Update component tests if necessary. d48375b
- [x] Task: Conductor - User Manual Verification 'Phase 2: Frontend UI Updates' (Protocol in workflow.md) a9aa345