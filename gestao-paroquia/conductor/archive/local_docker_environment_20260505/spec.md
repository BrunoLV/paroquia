# Specification: Local Docker Environment with SonarQube Builder

## Overview
Provide a complete, containerized local execution environment using Docker Compose. The setup will orchestrate the database, SonarQube, a build process that runs tests and static analysis, and the final lightweight API container.

## Functional Requirements
1.  **Database Container (`db`):**
    *   PostgreSQL (latest Alpine).
    *   Expose port 5432.
2.  **SonarQube Container (`sonar`):**
    *   SonarQube community edition.
    *   Expose port 9000.
    *   Configure healthcheck to ensure it's fully started before the build begins.
3.  **Builder Container (`builder`):**
    *   Uses `eclipse-temurin:21-jdk-alpine`.
    *   Waits for `db` and `sonar` to be healthy.
    *   Executes `./gradlew build sonar` targeting the local SonarQube container.
    *   Copies the compiled `.jar` to a shared Docker volume.
4.  **Application Container (`app`):**
    *   Uses `eclipse-temurin:21-jre-alpine` (lightweight runtime).
    *   Waits for the `builder` container to finish successfully (`service_completed_successfully`).
    *   Reads the `.jar` from the shared Docker volume and runs it.
    *   Exposes port 8080.
5.  **Optimization:**
    *   Create a `.dockerignore` file to exclude unnecessary folders (`build/`, `.gradle/`, etc.) from being copied into the builder container.

## Acceptance Criteria
*   Running `docker compose up` starts `db` and `sonar`. Once healthy, the `builder` runs tests and SonarQube analysis. Finally, the `app` container starts with the compiled `.jar`.
*   The SonarQube dashboard (`http://localhost:9000`) displays the project analysis.
*   The application is accessible at `http://localhost:8080`.