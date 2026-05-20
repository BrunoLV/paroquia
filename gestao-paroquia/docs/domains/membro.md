# Domain: Membro

The **Membro** domain manages information about parish members and their participation in various organizations.

## Core Concepts

### Membro (Member)
Represents a person associated with the parish. It includes personal details and contact information.

### Participação (Participation)
Links a **Membro** to one or more **Organizações**. It tracks the member's role and history within pastorals and movements.

## Use Cases

- **CreateMembroUseCase**: Registers a new parish member.
- **UpdateMembroUseCase**: Modifies member details or toggles active status.
- **GetMembroUseCase**: Retrieves detailed information about a specific member.
- **ListMembrosUseCase**: Lists members with filtering (name, status).
- **AddParticipanteUseCase**: Enrolls a member into a specific organization (Pastoral/Movement).
- **ListParticipacoesUseCase**: Lists all organizations a specific member is involved with.

## Key Components

- **MembroEntity**: Persistent representation of a member.
- **ParticipanteOrganizacaoEntity**: Association between a member and an organization.

## Integration

- **IAM**: Members can be linked to User accounts to access the system.
- **Calendário**: Members of organizations can be identified as individuals involved in events.
