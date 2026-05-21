# Implementation Plan: Event Registration Screen

## Phase 1: Setup and Basic Modal UI
- [x] Task: Write failing test for `EventRegistrationModal` rendering and open/close behavior. f468a7f
- [ ] Task: Implement `EventRegistrationModal` using Material UI Dialog to pass tests.
- [ ] Task: Write failing test for Calendar view integrating the 'New Event' button and modal state.
- [ ] Task: Implement Calendar integration to pass tests.
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Setup and Basic Modal UI' (Protocol in workflow.md)

## Phase 2: Form Structure and Basic Info
- [ ] Task: Write failing tests for Basic Info form fields (Title, Date, Time, Description) and validation.
- [ ] Task: Implement Basic Info fields within the modal using Material UI to pass tests.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Form Structure and Basic Info' (Protocol in workflow.md)

## Phase 3: Advanced Form Fields (Location, Involvement, Recurrence)
- [ ] Task: Write failing tests for Location, Involvement, and Recurrence form fields.
- [ ] Task: Implement Location selection field (dropdown/autocomplete) to pass tests.
- [ ] Task: Implement Involvement selection fields (Pastorals/Movements) to pass tests.
- [ ] Task: Implement Recurrence rule configuration inputs to pass tests.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Advanced Form Fields' (Protocol in workflow.md)

## Phase 4: API Integration and Submission Flow
- [ ] Task: Write failing tests simulating form submission, loading state, success toast, and modal closing.
- [ ] Task: Create React Query mutation for event submission and connect it to the form to pass tests.
- [ ] Task: Implement React Query cache invalidation for the Calendar data upon success.
- [ ] Task: Conductor - User Manual Verification 'Phase 4: API Integration and Submission Flow' (Protocol in workflow.md)