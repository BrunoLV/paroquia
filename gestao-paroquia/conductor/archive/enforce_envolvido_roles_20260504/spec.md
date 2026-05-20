# Track Specification: Enforce Envolvido Roles

## Overview
This track enforces the use of a fixed Enum for the role (papel) of organizations involved in an event. The allowed roles are restricted to `RESPONSÁVEL` (Responsible) and `APOIO` (Support).

## Functional Requirements
- **Enum Creation**: Create `PapelEnvolvido` enum with values `RESPONSAVEL` and `APOIO`.
- **DTO Update**: Update `EventoEnvolvidoInput` and `EventoEnvolvidoOutput` to use the `PapelEnvolvido` enum instead of a raw String.
- **Persistence Update**: Update `EventoEnvolvidoEntity` to use the `PapelEnvolvido` enum with JPA `@Enumerated(EnumType.STRING)`.
- **Validation**: Ensure that only valid enum values are accepted by the API.

## Non-Functional Requirements
- Ensure type safety across all layers.
- Maintain consistency with existing project enum patterns.
