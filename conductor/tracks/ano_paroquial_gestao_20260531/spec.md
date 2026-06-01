# Specification: Parish Year Management (Ano Paroquial)

## Overview
This specification outlines the business logic and user interface requirements for managing the Parish Year (Ano Paroquial).
The Parish Year controls whether event registrations are locked or open. Currently, the system supports a partial backend implementation for checking locks. This track completes the management workflow:
1. Standardizes status values to EM_PLANEJAMENTO, FECHADO, and CONCLUIDO.
2. Restricts actions to authorized users (ADMIN, PAROCO, and CONSELHO Coordinators/Vice-Coordinators).
3. Adds automated transition validation (a year N can only start if N-1 is finished).
4. Automates transition to CONCLUIDO upon year change (Dec 31st to Jan 1st), creating the subsequent year in EM_PLANEJAMENTO.
5. Creates a frontend administration screen in the Settings/Governance section of the UI.

## Functional Requirements

### 1. Status Standardization & Transitions
- The status enum `AnoParoquialStatus` must support:
  - `EM_PLANEJAMENTO` (planning mode: events and projects can be registered normally).
  - `FECHADO` (locked mode: only `ADICIONADO_EXTRA` events with justifications are allowed).
  - `CONCLUIDO` (finished mode: historical read-only status).
- **Rule of Succession**: Any year N can only transition to or start as `EM_PLANEJAMENTO` if year N-1 has status `CONCLUIDO`.
- **Temporal Enforcement**:
  - Future years (e.g., Year N+1) are allowed to exist with the status `EM_PLANEJAMENTO` to permit advanced planning.
  - A year N can **only** transition out of `EM_PLANEJAMENTO` (i.e., transition to `FECHADO` or `CONCLUIDO`) during its corresponding calendar year (i.e. when `LocalDate.now().getYear() == N`).
  - Closing or concluding a future year in advance is blocked.
- **Read-Only Lock**: Once a year status is `CONCLUIDO`, it can never be edited or updated.
- **Initialization/Seed**: When the application runs and the `anos_paroquiais` database table is empty, the system must automatically seed the current civil year with the status `EM_PLANEJAMENTO`.

### 2. Year Rollover Automation
- **Transition Trigger**:
  - **Manual**: An authorized user can click a "Concluir Ano" button on the UI, which calls a PATCH endpoint to transition Year N to `CONCLUIDO`.
  - **Automated**: A Spring Boot scheduled cron job runs automatically on the turn of the year (December 31st at 23:59:59 UTC/Local) to transition the year to `CONCLUIDO`.
- **Rollover Action**: When Year N is transitioned to `CONCLUIDO` (either manually or automatically), the system must automatically create Year N+1 in the database with the status `EM_PLANEJAMENTO`.

### 3. Role-Based Authorization
- Only `ADMIN`, `PAROCO` (Parish Priest), and `CONSELHO` Coordinators and Vice-Coordinators can manage the Parish Year.
- Standard local coordinators or members of regular pastorals are not allowed to view the management UI or perform PATCH requests on `AnoParoquial` endpoints.

### 4. User Interface (UI)
- Create a new screen `/anos-paroquiais` (or accessible under Settings/Governance menu) for authorized roles.
- The screen should show a table of all registered years.
- For active years (`EM_PLANEJAMENTO` or `FECHADO`), show controls to:
  - Switch status between `EM_PLANEJAMENTO` and `FECHADO` (via a Toggle/Switch or Button).
  - A button to "Concluir Ano" (which transitions it to `CONCLUIDO` and initializes the next year).
- Show warning confirmation dialogs before concluding a year.
