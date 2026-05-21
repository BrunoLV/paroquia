# Implementation Plan: Event Registration Screen

## Phase 1: Setup and Basic Modal UI
- [x] Task: Write failing test for `EventRegistrationModal` rendering and open/close behavior. f468a7f
- [x] Task: Implement `EventRegistrationModal` using Material UI Dialog to pass tests. d25b1eb
- [x] Task: Write failing test for Calendar view integrating the 'New Event' button and modal state. ba6d950
- [x] Task: Implement Calendar integration to pass tests. ba6d950
- [x] Task: Conductor - User Manual Verification 'Phase 1: Setup and Basic Modal UI' (Protocol in workflow.md) fb808c8

## Phase 2: Form Structure and Basic Info
- [x] Task: Write failing tests for Basic Info form fields (Title, Date, Time, Description) and validation. fb808c8
- [x] Task: Implement Basic Info fields within the modal using Material UI to pass tests. fb808c8
- [x] Task: Conductor - User Manual Verification 'Phase 2: Form Structure and Basic Info' (Protocol in workflow.md) fb808c8

## Phase 3: Advanced Form Fields (Location, Involvement, Recurrence)
- [x] Task: Write failing tests for Location, Involvement, and Recurrence form fields. fb808c8
- [x] Task: Implement Location selection field (dropdown/autocomplete) to pass tests. fb808c8
- [x] Task: Implement Involvement selection fields (Pastorals/Movements) to pass tests. fb808c8
- [x] Task: Implement Recurrence rule configuration inputs to pass tests. fb808c8
- [x] Task: Conductor - User Manual Verification 'Phase 3: Advanced Form Fields' (Protocol in workflow.md) fb808c8

## Phase 4: API Integration and Submission Flow
- [x] Task: Write failing tests simulating form submission, loading state, success toast, and modal closing. fb808c8
- [x] Task: Create React Query mutation for event submission and connect it to the form to pass tests. fb808c8
- [x] Task: Implement React Query cache invalidation for the Calendar data upon success. fb808c8
- [x] Task: Conductor - User Manual Verification 'Phase 4: API Integration and Submission Flow' (Protocol in workflow.md) fb808c8