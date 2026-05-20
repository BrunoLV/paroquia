# Specification: Project Aggregation Service

## Overview
Implement a new Aggregation Service to provide a consolidated view of a project's execution status, collaboration map, and temporal health. This data will be exposed via a dedicated REST API endpoint.

## Functional Requirements
1.  **Dedicated Endpoint:** Create a new GET endpoint `GET /api/v1/projetos/{projetoId}/resumo` (or similar appropriate path).
2.  **Execution Status (Status de Execução):**
    *   Calculate the total number of events linked to the project.
    *   Calculate the number of events that have already occurred (based on status or date).
    *   Calculate the number of pending/remaining events.
3.  **Collaboration Map (Mapa de Colaboração):**
    *   Extract a unique list of all Pastorals (or organizational groups/users) involved.
    *   This list must include the user/group responsible for the Project itself.
    *   This list must also include all users/groups involved in every individual Event linked to the project.
4.  **Temporal Health (Saúde Temporal):**
    *   Calculate the percentage of time elapsed between the project's start and end dates.
    *   Determine if the project is "at risk" based on a percentage threshold (e.g., > 80% time elapsed) AND the presence of pending events.
5.  **Calculation:** The aggregation must be calculated on-the-fly by querying the database when the endpoint is requested.

## Non-Functional Requirements
*   **Performance:** Optimize database queries to ensure the on-the-fly calculation performs well, even for projects with many events.
*   **Architecture:** The logic should be encapsulated within a dedicated Application Service (e.g., `ProjetoAgregacaoService`).

## Acceptance Criteria
*   Calling the dedicated endpoint for a valid project returns the correct total, completed, and pending event counts.
*   The endpoint returns a deduplicated list of all involved entities (project owner + event participants).
*   The temporal health status accurately reflects whether the project is near its deadline with pending tasks, based on a percentage of time elapsed.
*   If the project does not exist, the endpoint should return a 404 Not Found.