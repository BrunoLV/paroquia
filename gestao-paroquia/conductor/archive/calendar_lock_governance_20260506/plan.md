# Implementation Plan: Calendar Lock Governance (Ano Paroquial)

This plan outlines the steps to implement the civil year locking mechanism, leveraging existing roles for management.

## Phase 1: Persistence and Domain Model

### 1. Database Migration
- Create `V026__create_anos_paroquiais.sql`.
- Table `calendario.anos_paroquiais`.
- Seed current and next year in `PLANEJAMENTO` status.

### 2. Entity and Repository
- Create `AnoParoquialEntity`.
- Create `AnoParoquialJpaRepository`.
- Create `AnoParoquialStatus` enum (`PLANEJAMENTO`, `FECHADO`).

### 3. Domain Policy
- Create `CalendarLockPolicy`.
    - Method `checkLock(Instant eventDate, EventoStatus status)`.
    - Throws `CalendarLockedException` if year is `FECHADO` and status is not `ADICIONADO_EXTRA`.

## Phase 2: Integration with Use Cases

### 1. Update CreateEventoUseCase
- Inject `CalendarLockPolicy`.
- Call `checkLock` before proceeding with creation logic.

### 2. Update CreateEventoRecorrenciaUseCase
- Apply the same logic. Check the first date of the recurrence.

### 3. Exception Handling
- Update `DomainExceptionHandler` to handle `CalendarLockedException`.
- Return `400 Bad Request` with code `CALENDAR_LOCKED_FOR_YEAR`.

## Phase 3: Management API

### 1. DTOs
- `AnoParoquialResponse`.
- `UpdateAnoParoquialRequest`.

### 2. Authorization Service
- Criterion: `PAROCO` or `COORDENADOR` of `CONSELHO`.

### 3. Controller
- `AnoParoquialController`.
- Implement `GET` (list/detail) and `PATCH` (update status).

## Phase 4: Verification and Demo

### 1. Unit/Integration Tests
- Test `CalendarLockPolicy`.
- Test `CreateEventoUseCase` with locked year.
- Test `AnoParoquialController` security.

### 2. Update Demo Script
- Add step: `coord_conselho` locks the year.
- Show failed `CONFIRMADO` creation.
- Show successful `ADICIONADO_EXTRA` creation.
