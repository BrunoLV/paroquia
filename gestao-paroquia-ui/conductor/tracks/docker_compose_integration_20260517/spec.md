# Track Specification: Docker Compose Integration

## Overview
This track implements an integrated local development environment using Docker Compose. It provisions a `docker-compose.yml` file within the UI repository (`gestao-paroquia-ui`) that spins up the React frontend alongside the backend API and PostgreSQL database (located in `../gestao-paroquia`).

## Functional Requirements
- **UI Service**: Run the React frontend in development mode (`npm start`) with hot-reloading enabled via volume mapping.
- **Backend Integration**: Define services for the database and the backend API by referencing the build context of the adjacent `gestao-paroquia` repository.
- **Network & Environment**: Ensure the frontend container can communicate with the backend API seamlessly. The frontend should have `REACT_APP_API_URL` correctly configured.
- **Dependencies**: Provide a clear startup sequence (e.g., UI depends on API, API depends on DB).

## Non-Functional Requirements
- Maintain ease of use: A single `docker compose up` command should start the entire integrated system.
- Do not negatively impact the existing backend `docker-compose.yml`.

## Acceptance Criteria
- Running `docker compose up` at the root of `gestao-paroquia-ui` successfully starts the DB, the Backend API, and the React UI.
- Changes to the React source code reflect immediately in the browser (Hot Module Replacement).
- The React UI can successfully authenticate and fetch data from the Backend API in the Docker environment.