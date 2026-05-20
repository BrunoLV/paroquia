# Architecture

Gestão Paróquia follows **Clean Architecture** and **Domain-Driven Design (DDD)** principles to promote a maintainable, testable, and loosely coupled codebase.

## Monolithic Structure

The application is built as a modular monolith. Each "theme" or "domain" (e.g., `calendario`, `membro`, `iam`) is organized into its own top-level package, containing its own internal layers.

## Package Structure (Per Domain)

Each domain package is divided into four main layers:

### 1. Domain (`...<domain>.domain`)
The heart of the system. Contains business logic that is independent of any framework or external tool.
- **Entities & Aggregates**: Core business objects (e.g., `Evento`, `Membro`).
- **Value Objects**: Objects defined by their attributes (e.g., `Periodo`, `LocalId`).
- **Domain Services**: Logic that doesn't naturally fit into a single entity.
- **Repository Interfaces**: Definitions of how to persist and retrieve aggregates.

### 2. Application (`...<domain>.application`)
Contains the application-specific business rules. It orchestrates the flow of data to and from the domain layer.
- **Use Cases**: Individual operations (e.g., `AddEventoUseCase`, `ApproveRequestUseCase`).
- **DTOs**: Data Transfer Objects for input/output of use cases.
- **Ports/Interfaces**: Interfaces for external services (e.g., Email, Messaging).

### 3. API (`...<domain>.api`)
The entry point for external interactions.
- **Controllers**: REST endpoints that handle HTTP requests and map them to Use Cases.
- **Mappers**: Convert between API Request/Response objects and Application DTOs.

### 4. Infrastructure (`...<domain>.infrastructure`)
Contains the implementation of the interfaces defined in the Domain and Application layers.
- **Persistence**: JPA/Hibernate implementations of repositories.
- **External Services**: Implementations for third-party APIs or system-level services.
- **Configuration**: Spring `@Configuration` classes specific to the domain.

## Cross-Cutting Concerns

- **Support (`...gestao.support`)**: Contains shared utilities, base classes, and common exceptions used across multiple domains.
- **Security**: Centralized configuration for authentication and authorization using Spring Security.
- **Observability**: Standardized logging, auditing, and health checks.

## Tech Stack

- **Backend**: Java 21, Spring Boot 3.
- **Database**: PostgreSQL (Migrations with Flyway).
- **Documentation**: Docsify (this site) and SpringDoc OpenAPI (Swagger).
