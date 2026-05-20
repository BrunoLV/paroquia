# Specification: Identity and Access Management (IAM) API

## Problem Statement
The current system has the database structure and security filters for RBAC, but lacks an API to manage users and their memberships in organizations. Currently, users can only be added via manual SQL or seeding.

## Proposed Solution
Implement a set of administrative endpoints to manage users and memberships.

## Key Features
- **User Management**: CRUD operations for users.
- **Membership Management**: Associate users with organizations (Catequese, Liturgia, etc.) and assign roles (COORDENADOR, MEMBRO).
- **Self-Service**: Endpoint for users to see their own profile and roles.

## Technical Details
- **Entities**: `UsuarioEntity`, `MembroOrganizacaoEntity` (JPA).
- **Repositories**: `UsuarioJpaRepository`, `MembroOrganizacaoJpaRepository`.
- **Security**: All management endpoints must require `ROLE_ADMIN` or `ROLE_CONSELHO`.
- **Password Handling**: Use `BCrypt` (via `PasswordEncoder`) for password hashing.

## API Endpoints
- `GET /api/v1/usuarios`
- `POST /api/v1/usuarios`
- `GET /api/v1/usuarios/{id}`
- `PATCH /api/v1/usuarios/{id}`
- `POST /api/v1/usuarios/{id}/password`
- `GET /api/v1/usuarios/{id}/membros`
- `POST /api/v1/usuarios/{id}/membros`
- `DELETE /api/v1/usuarios/{id}/membros/{membershipId}`
- `GET /api/v1/auth/me`
