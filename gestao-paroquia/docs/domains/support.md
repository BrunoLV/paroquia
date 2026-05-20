# Domain: Support

The **Support** package contains common infrastructure, shared utilities, and base classes used across the entire application to ensure consistency and reduce duplication.

## Key Components

### Persistence Base Classes
- **BaseVersionedEntity**: A base class for entities that require optimistic locking and auditing fields (e.g., `createdAt`, `updatedAt`, `version`).

### Shared Utilities
- **UUID Generators**: Standardized ID generation.
- **Date/Time Helpers**: Utilities for handling UTC conversions and period calculations.

### Common Exceptions
- Defines standard business exceptions (e.g., `NotFoundException`, `ConflictException`) that are automatically mapped to appropriate HTTP status codes.

### Idempotency
- Infrastructure support for ensuring that sensitive operations (like event creation) are idempotent when keys are provided by the client.
