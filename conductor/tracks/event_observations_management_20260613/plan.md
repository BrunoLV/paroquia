# Implementation Plan: Event Observations Management

## Phase 1: API Integration & Client Testing [checkpoint: ]
- [ ] Task: Define TypeScript Types and Client Methods
    - [ ] Add `Observacao` type and `TipoObservacao` enum in `src/api/types.ts`
- [ ] Task: Write Failing API Client Tests (Red Phase)
    - [ ] Create `src/api/observacoes.test.ts`
    - [ ] Write tests mocking API calls for listing, creating, updating, and deleting event observations
    - [ ] Run the tests and verify they fail
- [ ] Task: Implement API Client Logic (Green Phase)
    - [ ] Implement the fetching, creation, updating, and deletion client logic in a new file `src/api/observacoes.ts`
    - [ ] Run the tests and verify they pass
- [ ] Task: Conductor - User Manual Verification 'Phase 1: API Integration & Client Testing' (Protocol in workflow.md)

## Phase 2: Event Details Dialog UI & Component Testing [checkpoint: ]
- [ ] Task: Write Failing UI Component Tests (Red Phase)
    - [ ] Create `src/components/EventObservations.test.tsx`
    - [ ] Write failing test cases verifying listing of notes, typing a new note, rendering edit/delete buttons, and confirming deletions
    - [ ] Run tests and verify they fail
- [ ] Task: Implement EventObservations Component & Integrate into Details Dialog (Green Phase)
    - [ ] Create a reusable `EventObservations.tsx` component that lists notes, differentiates system notes, and allows CRUD operations
    - [ ] Integrate `EventObservations.tsx` into the Details Dialog modal in `Calendar.tsx`
    - [ ] Run the frontend tests and verify all tests pass
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Event Details Dialog UI & Component Testing' (Protocol in workflow.md)
