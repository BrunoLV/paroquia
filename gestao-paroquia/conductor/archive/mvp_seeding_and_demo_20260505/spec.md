# Specification: MVP Seeding and Demo Script

## Overview
To facilitate local execution and testing of the Calendar MVP, provide a Docker database initialization script to seed foundational test data, and a comprehensive shell script to demonstrate the full application lifecycle against the local Docker environment.

## Functional Requirements
1.  **Database Seeding (Docker Init Script):**
    *   Create a SQL script (e.g., `init-data.sql`).
    *   Update `docker-compose.yml` to mount this script into the PostgreSQL container's `/docker-entrypoint-initdb.d/` directory so it runs automatically when the database is created.
    *   Ensure the `calendario.organizacoes` table exists.
    *   Seed default Locations (e.g., Igreja Principal, Salão Paroquial, Salas de Catequese).
    *   Seed default Organizations/Pastorals (e.g., Catequese, Liturgia, Pascom).
    *   Seed default Users and their memberships, mapping them correctly in `membros_organizacao`.
2.  **Demo Script (`demo_mvp.sh`):**
    *   Create an executable Bash script that interacts with the running API at `http://localhost:8080`.
    *   Execute a Full Lifecycle Demo:
        *   Create a Project.
        *   Create standard Events.
        *   Create an Event requiring approval (sensitive changes).
        *   Retrieve pending approvals and simulate an Approval execution.
        *   Query the Project Aggregation endpoint to show the consolidated status.
    *   Provide clear console output (using `echo` and formatted `curl` responses via `jq` if possible) to explain each step.

## Non-Functional Requirements
*   **Usability:** The demo script should be runnable with a single command (`./demo_mvp.sh`) and automatically extract and pass IDs between requests.

## Acceptance Criteria
*   Running `docker compose up` with a clean volume successfully seeds the initial data.
*   Executing `./demo_mvp.sh` successfully traverses the primary application flows without errors, demonstrating the MVP features.