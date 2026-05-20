# Domain: Local

The **Local** domain manages the physical spaces and resources of the parish.

## Core Concepts

### Local (Space/Location)
Represents a physical area in the parish, such as the "Matriz", "Salão Paroquial", or "Capela".

- **Attributes**: Includes name, capacity, and characteristics.

## Administrative Actions

The domain provides a complete set of operations for managing parish spaces:
- **Cadastrar Local**: Defines a new space with its capacity and characteristics.
- **Atualizar Local**: Modifies information for an existing space.
- **Remover Local**: Deletes a space (if not currently in use).
- **Listar Locais**: Provides a list of all available spaces for event planning.
- **Consultar Detalhes**: Retrieves the full information of a specific location.

## Key Components

- **LocalEntity**: Persistent representation of a location.

## Integration

- **Calendário**: Events are scheduled at a specific **Local**. The system uses this information for conflict detection (preventing two events at the same location and time).
- **Projeto**: Projects may involve activities at specific locations.
