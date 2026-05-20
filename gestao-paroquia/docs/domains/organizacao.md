# Domain: Organização

The **Organização** domain defines the structure of the parish, including pastorals, movements, and administrative bodies.

## Core Concepts

### Organização (Organization)
A group or entity within the parish. Examples include "Catequese", "Liturgia", "Conselho Administrativo", and "Clero".

### Tipos de Organização (Organization Types)
Organizations are classified into types to determine their role and visibility:
- **PASTORAL**: Standard parish pastoral groups.
- **MOVIMENTO**: Religious movements.
- **CONSELHO**: Administrative councils (e.g., CPP, CAEP).
- **CLERO**: The parish priests and official leadership.

### Papéis Organizacionais (Organizational Roles)
Defines the responsibility level within an organization:
- **COORDENADOR**: High-level responsibility for the group.
- **MEMBRO**: Standard group member.

## Use Cases

- **AddMembershipUseCase**: Assigns a user to an organization with a specific role.
- **ListMembershipsUseCase**: Lists all memberships for an organization or user.
- **RemoveMembershipUseCase**: Removes a user's association with an organization.

## Key Components

- **Organizacao**: Domain model representing a group.
- **Hierarquia**: While currently mostly flat, the system supports organizational relationships for reporting and management.

## Integration

The **Organização** domain is fundamental to other domains:
- **IAM**: Provides context for user permissions.
- **Calendário**: Defines who is responsible for which events.
