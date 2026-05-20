# Track Specification: Evento Recorrencia Flow

## Overview
Conceptually, recurring events are simply normal events that have a defined repetition rule attached to them. This eliminates the need for manual, individual registration for predictable events (e.g., a Sunday mass that is known to always occur). The system will rely on a yearly scheduled job to pre-calculate and generate the individual event instances for the upcoming year based on these rules.

## Functional Requirements
1. **Core Event Rules Equivalence:** 
   - Since recurring events are fundamentally normal events, their generated instances are subject to the EXACT SAME business rules. This includes validations, role-based authorization, the approval workflow, auditing trails, and metrics generation.
2. **Recurrence Definition:** 
   - Users can define recurrence rules using a Custom JSON structure (handling frequency, intervals, and specific days/patterns).
   - Recurring events can have a specific end date or be marked as indefinite.
3. **Instance Generation (Yearly Job):**
   - A Spring `@Scheduled` job will run automatically on the first day of the year.
   - This job will parse the Custom JSON rules and generate the individual `EventoEntity` database entries for the entire year.
4. **Edit Scopes:**
   - **Only This Instance (Punctual Edit):** Users can modify a single generated event. This instance becomes an exception to the rule, retaining its changes without affecting the rest of the series.
   - **This and Following (Propagated Edit):** Users can modify an instance and apply the changes forward. This effectively ends the current recurrence rule at the previous instance and creates a new recurrence rule starting from the edited instance.

## Non-Functional Requirements
- **Cluster Safety:** The Spring `@Scheduled` job must be implemented with concurrency controls (e.g., database locks or ShedLock) to ensure it only runs once per year, even if multiple instances of the application are running.
