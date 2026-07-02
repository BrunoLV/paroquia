# Specification: pastoral_comunidades_concept_20260701

## Overview
Ensure that Pastorais and Movimentos (which are global concepts) can have their coordination structure (Coordinator/Vice-Coordinator) and event responsibilities scoped by church context (Igreja Matriz or specific Comunidade) using the `igreja_vinculo_id` field. Also, allow members to hold multiple active memberships in the same organization across different churches.

## Functional Requirements
1. **Database Schema & Migrations:**
   - Add `igreja_vinculo_id` column to the `calendario.eventos` table referencing `calendario.organizacoes(id)`.
2. **Membership Flow & Constraints:**
   - **Multiple Church Memberships:** Allow a user to participate in the same Pastoral/Movimento in multiple churches (e.g. Matriz and a Community) with different roles.
   - **Membership Uniqueness:** Prevent duplicate memberships for the exact same combination of `{usuarioId, organizacaoId, papel, igrejaVinculoId}`. If one exists and is active, return it. If inactive, reactivate it.
   - **Local Coordinator Unicity:** When assigning a coordinator/vice-coordinator to a Pastoral/Movimento, only deactivate the previous coordinator/vice-coordinator *for that specific church link* (matching `igrejaVinculoId`).
3. **Backend Event Logic & DTOs:**
   - Update Event DTOs (`EventoRequest`, `EventoResponse`) and JPA entities to support `igrejaVinculoId`.
   - Adjust `AuthorizationPolicy` to restrict event creation, editing, and approval: if the event is linked to a church, only coordinators/vice-coordinators of that Pastoral/Movimento *specifically linked to that same church* (or the Parish Priest/Admin) are authorized.
4. **Frontend UI Enhancements:**
   - **Pastoral Management (Settings):** In the Pastoral Edit modal, show a table/section listing all active churches (Matriz + Communities) and allow direct assignment/editing of the Coordinator and Vice-Coordinator for each.
   - **Event Modal:** Add an optional "Igreja de Atuação" selection dropdown when the responsible organization is a Pastoral or Movimento.
   - **Calendar Rendering:** Concatenate and render the responsible field as `Pastoral Name - Church Name` when the event has a church link.

## Non-Functional Requirements
- Maintain backward compatibility for existing events (where `igreja_vinculo_id` remains NULL).
- Full unit test coverage for new backend authorization logic, membership constraints, and frontend rendering.
