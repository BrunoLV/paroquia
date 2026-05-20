# Implementation Plan: Docker Compose Integration

## Phase 1: Docker Compose Setup [checkpoint: e4b1c3e]
- [x] Task: Create `docker-compose.yml` (355d351)
    - [x] Define the `ui` service using a Node.js image, mapping local files as a volume for hot-reloading.
    - [x] Define the `db` service (PostgreSQL) mirroring the backend's database configuration.
    - [x] Define the `builder` and `api` services pointing their build context to `../gestao-paroquia`.
    - [x] Configure proper environment variables, including `REACT_APP_API_URL` for the `ui` service.
- [x] Task: Add `Dockerfile.dev` for UI (Optional/Recommended) (13c1fb3)
    - [x] If necessary for a cleaner setup, create a `Dockerfile.dev` that handles `npm install` and `npm start` for the `ui` service.
- [x] Task: Conductor - User Manual Verification 'Phase 1: Docker Compose Setup' (Protocol in workflow.md)

## Phase 2: Documentation & Testing [checkpoint: e74818a]
- [x] Task: Update README (9e8c28f)
    - [x] Add instructions to `README.md` on how to use `docker compose up` to run the integrated system.
- [x] Task: Conductor - User Manual Verification 'Phase 2: Documentation & Testing' (Protocol in workflow.md)