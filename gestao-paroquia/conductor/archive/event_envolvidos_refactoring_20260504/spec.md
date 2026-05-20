# Track Specification: Refactor Event Participation to Envolvidos

## Overview
This track refactors the existing "Participation" concept to "Envolvidos" (Involved Organizations), aligning the API and Application layers with the Database schema and Domain terminology. It also separates the "Recurrence" logic from the participation controller to improve cohesion.

## Functional Requirements
- **Renaming**: Change all occurrences of "Participante" to "Envolvido" in API (DTOs, Controllers) and Application (Use Cases) layers.
- **Participation Roles**: Update the "Envolvidos" API to support specifying the role (e.g., ORGANIZADOR, APOIO) for each involved organization.
- **Controller Separation**: Split `EventoParticipacaoController` into `EventoEnvolvidoController` and `EventoRecorrenciaController`.

## Non-Functional Requirements
- Maintain existing functionality for event involvement and recurrence.
- Ensure consistent naming across all layers (API, Application, Infrastructure, DB).
- Adhere to the project's Clean Architecture and DDD standards.
