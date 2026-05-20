# Domain: Calendário

The **Calendário** domain is the core of the Gestão Paróquia application, responsible for managing the schedule of events, recurrences, and resources (locations).

## Core Concepts

### Evento (Event)
An `Evento` represents a specific occurrence in the parish calendar. It includes details such as title, description, time period (start/end), category, and status.

- **Status**: Events can be in various states: `PENDENTE`, `CONFIRMADO`, `CANCELADO`, `ADICIONADO_EXTRA`.
- **Categorias**: Events are categorized (e.g., `LITURGICO`, `PASTORAL`, `SOCIAL`, `FORMATIVO`) to help with filtering and organization.
- **Organization**: Every event belongs to a responsible organization (Pastoral/Movement).

### Recorrência (Recurrence)
Events can be part of a recurrence pattern (e.g., weekly Mass, monthly meeting). The system manages recurrence rules and generates individual event instances.

### Envolvidos (Involved Parties)
Organizations or groups involved in an event with specific roles, such as:
- **Responsável**: The main organizer.
- **Apoio**: Supporting group.

### Observações (Observations)
Events can have notes or observations attached for collaboration between staff and administrators.

## Use Cases

### Event Management
- **CreateEventoUseCase**: Create a new event. Triggers conflict detection and approval workflows if necessary.
- **UpdateEventoUseCase**: Modify event details.
- **CancelEventoUseCase**: Cancel a scheduled event with a justification.

### Conflict Detection
Automatically checks for time and location overlaps when scheduling new events.

### Recurrence
- **CreateEventoRecorrenciaUseCase**: Define a recurrence pattern for an event.

### Collaboration
- **CreateObservacaoUseCase**: Add a note or observation to an event.

## Governance & Security
- **Access Control**: Users can generally manage events for their own organizations.
- **Clergy/Council**: Have broader visibility and management rights across all events.
- **Locking**: The parish calendar can be "locked" for a specific year, preventing regular scheduling and requiring a special "Extra" status for new additions.
