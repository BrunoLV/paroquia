# OpenAPI Configuration Plan

## Objective
Configure OpenAPI (Swagger) documentation for the Gestão Paróquia API using `springdoc-openapi`.

## Key Files & Context
- `app/build.gradle.kts`: The `springdoc-openapi-starter-webmvc-ui` dependency is already present.
- `app/src/main/resources/application.yml`: Needs configuration for OpenAPI paths.
- `app/src/main/java/br/com/nsfatima/gestao/support/infrastructure/config/OpenApiConfig.java`: New class to define the API metadata and security scheme.
- `app/src/main/java/br/com/nsfatima/gestao/calendario/infrastructure/security/SecurityConfig.java`: Needs to be updated to permit access to Swagger UI endpoints.

## Implementation Steps

### 1. Update `application.yml`
Add the following configuration to expose Swagger UI and OpenAPI documentation:
```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    operationsSorter: method
```

### 2. Create `OpenApiConfig` Class
Create `app/src/main/java/br/com/nsfatima/gestao/support/infrastructure/config/OpenApiConfig.java` to define:
- **Title**: Gestão Paróquia API
- **Description**: API for the Gestão Paróquia application.
- **Security**: Configure Bearer Authentication (JWT or Session) or simply add global security requirements so Swagger UI can send the correct headers. Note: The application uses JSESSIONID stateful session (as seen in SecurityConfig). We might not need a Bearer token if it relies on cookie, but we should define the API metadata anyway. Wait, the API uses a custom JSON login which sets `JSESSIONID` cookie. Swagger UI doesn't natively handle cookie-based authentication out of the box unless configured, but since it's same-domain, the browser sends the cookie automatically if the user logs in via another tab, or we can configure Swagger UI to know about the cookie. For now, we will add the `@OpenAPIDefinition`.

### 3. Update `SecurityConfig.java`
Permit access to the following paths:
- `/v3/api-docs/**`
- `/swagger-ui/**`
- `/swagger-ui.html`

## Verification
- Run `./gradlew bootRun` and access `http://localhost:8080/swagger-ui.html` to verify the documentation is accessible and displays all the controllers.
