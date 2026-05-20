# Domain: IAM (Identity & Access Management)

The **IAM** domain manages users, authentication, and secure access to the application's resources.

## Core Concepts

### Usuário (User)
Represents an individual with access to the system. Users are authenticated via username and password (or other credentials).

### Authentication
Secure login mechanism integrated with Spring Security. It handles session management and identity verification.

## Security Model

The application uses **Role-Based Access Control (RBAC)** combined with organizational context:
- **Roles**: Define what actions a user can perform (e.g., `ADMIN`, `COORDENADOR`, `MEMBRO`).
- **Organizational Context**: Restricts actions to the user's specific organization (e.g., a Catechism coordinator can only manage Catechism events).

## Use Cases

- **CreateUsuarioUseCase**: Registers a new system user.
- **UpdateUsuarioUseCase**: Modifies user details or status.
- **GetUsuarioUseCase**: Retrieves profile information for a user.
- **ListUsuariosUseCase**: Lists users for administrative purposes.
- **ChangePasswordUseCase**: Allows users to securely update their credentials.

## Key Components

- **UsuarioEntity**: Persistent representation of a user.
- **Security Configuration**: Defines protected endpoints and authorization rules.
- **Actor Context**: A cross-cutting concept that provides information about the currently authenticated user (actor) to the business logic.
