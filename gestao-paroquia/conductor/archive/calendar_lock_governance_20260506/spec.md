# Specification: Calendar Lock Governance (Ano Paroquial)

## Background
The current system allows users to manually choose between `CONFIRMADO` and `ADICIONADO_EXTRA` status. While `ADICIONADO_EXTRA` triggers a mandatory justification and approval flow, there is no systemic enforcement that prevents users from creating "confirmed" events after the official planning phase for a year has ended.

## Goal
Implement a systemic "Lock" mechanism per civil year. When a year is locked, any new event creation for that year must use the `ADICIONADO_EXTRA` status.

## Business Rules

### 1. Ano Paroquial (Parish Year)
- A new entity `AnoParoquial` represents a civil year in the parish context.
- **States**:
    - `PLANEJAMENTO` (Planning): Default state. Users can create events with any valid status (subject to general role permissions).
    - `FECHADO` (Closed/Locked): The official calendar for this year is finalized.
- **Ownership**: The `PAROCO` (any organization) or the `COORDENADOR` of the `CONSELHO` can change the status of an `AnoParoquial`.

### 2. Event Creation Enforcement
- When creating an event for a year that is `FECHADO`:
    - If the requested status is **NOT** `ADICIONADO_EXTRA`, the request is rejected with a `400 Bad Request` (Validation Error).
    - Specific error code: `CALENDAR_LOCKED_FOR_YEAR`.
    - If the status is `ADICIONADO_EXTRA`, the creation proceeds as usual (triggering justification and approval flow).
- This rule applies to `POST /eventos` and `POST /eventos/recorrencia`.

### 3. Automatic Discovery
- If an `AnoParoquial` record does not exist for a given year when an event is created, the system should consider it in `PLANEJAMENTO` by default.

## Technical Design

### Data Model
- **Table**: `calendario.anos_paroquiais`
    - `ano` (INTEGER, Primary Key)
    - `status` (VARCHAR(32), NOT NULL)
    - `data_fechamento_utc` (TIMESTAMP WITH TIME ZONE, NULL)
    - `updated_at` (TIMESTAMP WITH TIME ZONE)

### API Endpoints
- `GET /anos-paroquiais`: List all years and their statuses.
- `GET /anos-paroquiais/{ano}`: Get status of a specific year.
- `PATCH /anos-paroquiais/{ano}`: Update status.
    - Security: Requires (`PapelOrganizacional.PAROCO`) OR (`TipoOrganizacao.CONSELHO` AND `PapelOrganizacional.COORDENADOR`).
    - Payload: `{"status": "FECHADO"}`

### Domain Policies
- `CalendarLockPolicy`: New policy service to validate if an operation is allowed based on the year's lock status.
- Integration point: `CreateEventoUseCase` and `CreateEventoRecorrenciaUseCase`.

## User Persona Interaction
1. **Pároco** or **Coordenador do Conselho**: After the annual assembly, navigates to "Manage Years", selects "2027", and clicks "Lock Calendar".
2. **Coordenador de Pastoral**: Tries to add a "Confirmed" event for July 2027 in March 2027. The system denies it, informing that 2027 is locked.
3. **Coordenador de Pastoral**: Resubmits the event as `ADICIONADO_EXTRA` with a justification. The system accepts it and sends it to the Pároco for approval.
