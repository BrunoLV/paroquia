# Implementation Plan: MVP Seeding and Demo Script

## Phase 1: Database Seed Script [checkpoint: 65e0d42]
- [x] Task: Create a `docker/init-data.sql` script with `INSERT` statements for basic setup (Igreja, Salão, Catequese, Liturgia, and default users/memberships). Ensure `calendario.organizacoes` table is created here since it's an external dependency. [0951be7]
- [x] Task: Update `docker-compose.yml` to mount `docker/init-data.sql` to `/docker-entrypoint-initdb.d/` in the PostgreSQL container. [7eda1f4]
- [x] Task: Conductor - User Manual Verification 'Phase 1: Database Seed Script' (Protocol in workflow.md) [65e0d42]

## Phase 2: Demo Script [checkpoint: 65e0d42]
- [x] Task: Create `demo_mvp.sh` in the project root. This script will use `curl` to interact with `http://localhost:8080`. [efc6f66]
- [x] Task: Implement the flows in `demo_mvp.sh`: Create Project, Create standard Events, Update Event (triggering approval), Execute Approval, and Fetch Project Aggregation. Extract IDs from JSON responses to chain the requests. [efc6f66]
- [x] Task: Ensure `demo_mvp.sh` is executable (`chmod +x`). [efc6f66]
- [x] Task: Conductor - User Manual Verification 'Phase 2: Demo Script' (Protocol in workflow.md) [65e0d42]