# Getting Started

Follow these instructions to get the Gestão Paróquia application running on your local machine for development and testing.

## Prerequisites

- **Docker & Docker Compose**: Recommended for running the full environment (Database, SonarQube, API).
- **Java 21 JDK**: If you wish to run/build outside Docker.
- **Gradle**: For local builds (wrapper included in the project).

## Running with Docker Compose

The easiest way to start the application and its dependencies is using Docker Compose:

```bash
docker-compose up --build
```

This will start:
- **PostgreSQL**: Accessible on port `5432`.
- **SonarQube**: Accessible on port `9000`.
- **API**: Accessible on port `8080`.

## Local Development

If you prefer to run the API locally (e.g., via your IDE) while keeping the database in Docker:

1. Start only the database:
   ```bash
   docker-compose up -d db
   ```
2. Run the application using the Gradle wrapper:
   ```bash
   ./gradlew bootRun
   ```

The API will be available at `http://localhost:8080`.

## Demo & Testing

The project includes a comprehensive demonstration script that interacts with the API to showcase the MVP features:

```bash
./demo_mvp.sh
```

This script generates a `demo_report.html` file with the results of the execution.

### Running Tests

To run the automated test suite:

```bash
./gradlew test
```

Test reports can be found in `app/build/reports/tests/test/index.html`.
