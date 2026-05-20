# Domain: Governança

The **Governança** domain defines high-level policies and administrative periods for the parish.

## Core Concepts

### Ano Paroquial (Parish Year)
Represents an administrative cycle (usually a calendar year).

- **Status**: An `AnoParoquial` can be `ABERTO` (open for regular planning) or `FECHADO` (locked).

### Policies
Global rules that affect multiple domains, such as:
- **CalendarLockPolicy**: Determines if an event can be added to a specific year based on its `AnoParoquialStatus`.

## Administrative Actions

The domain provides endpoints for administrative control of the parish calendar cycle:
- **Listar Anos**: Retrieves the history and status of all recorded years.
- **Consultar Ano**: Checks the specific status of a year.
- **Atualizar Status**: Modifies the status of a year (e.g., locking the calendar for a new administrative cycle). This operation is restricted to high-level coordinators (Council/Clergy).

## Integration

- **Calendário**: Directly affects the ability to schedule events and determines if an **Aprovação** is required.
