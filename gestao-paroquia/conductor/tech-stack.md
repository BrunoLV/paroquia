# Tech Stack

## Programming Language
- **Java 21**: The primary language for backend development, leveraging modern features and performance.

## Frameworks & Libraries
- **Spring Boot 3.3.13**: The core framework for building the REST API, handling dependency injection, security, and more.
- **Spring Data JPA**: For efficient data persistence and retrieval.
- Spring Security: To implement robust authentication and authorization.
- **SpringDoc OpenAPI**: For automated API documentation and interactive UI (Swagger).

## Database & Persistence
- **PostgreSQL**: The production database for reliable and scalable data storage.
- **Flyway**: For managing database schema migrations.
- **H2**: An in-memory database used for fast and isolated integration testing.

## Build & Dependency Management
- **Gradle (Kotlin DSL)**: For building the project and managing dependencies.
- **Sonar (SonarScanner for Gradle)**: For static code analysis and quality tracking.

## Testing
- **JUnit 5**: The standard testing framework for Java.
- **Spring Boot Test**: For comprehensive integration and slice testing.
- **JaCoCo**: For code coverage analysis and reporting.

## Architecture
- **Monolithic Architecture**: Consolidating all parish management themes into a single, maintainable application.
- **DDD / Clean Architecture**: Organizing the code into Domain, Application, API, and Infrastructure layers within each theme (e.g., 'calendario') to ensure separation of concerns.