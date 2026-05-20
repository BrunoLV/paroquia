# gestao-paroquia Development Guidelines

Auto-generated from all feature plans. Last updated: 2026-04-18

## Active Technologies
- PostgreSQL compartilhado (schema de calendario + tabelas externas somente leitura) (001-parish-calendar-api)
- Java 21 + Spring Boot 3.x (Web, Validation, Security, Data JPA), Flyway, PostgreSQL driver, OpenAPI tooling (001-parish-calendar-api)
- PostgreSQL compartilhado; schema do calendario proprio da API e leitura somente de `usuarios`, `organizacoes`, `membros_organizacao` (001-parish-calendar-api)
- Java 21 + Spring Boot 3.x, Spring Web, Spring Validation, Spring Security (RBAC), Spring Data JPA, Flyway, OpenAPI 3.0.3 (001-parish-calendar-api)
- PostgreSQL (banco compartilhado; escrita apenas nas tabelas da API e leitura de `organizacoes`, `usuarios`, `membros_organizacao`) (001-parish-calendar-api)
- Java 21 + Spring Boot 3.x (Web, Validation, Security, Data JPA), Flyway, PostgreSQL driver, OpenAPI 3.0.3 tooling (001-parish-calendar-api)
- PostgreSQL compartilhado; escrita apenas nas tabelas da API de calendario e leitura somente de `organizacoes`, `usuarios`, `membros_organizacao` (001-parish-calendar-api)
- Java 21 + Spring Boot 3.3.5 (Web, Validation, Security, Data JPA, Actuator), Flyway, PostgreSQL driver (002-enum-fields-dto-endpoints)
- PostgreSQL (schema `calendario`) com suporte a migrations Flyway; H2 para testes (002-enum-fields-dto-endpoints)
- PostgreSQL (schema `calendario`) com migrations Flyway; H2 para testes (002-enum-fields-dto-endpoints)
- PostgreSQL (`schema calendario`) para persistencia de eventos; H2 para testes (003-complete-event-creation)
- Java 21 + Spring Boot 3.3.5 (Web, Validation, Security, Data JPA, Actuator), Flyway 10.20.1 (004-complete-event-patch)
- PostgreSQL (schema `calendario`) para runtime; H2 para testes de integracao (004-complete-event-patch)
- Java 21 + Spring Boot 3.3.5, Spring Security 6.3.x, Spring Data JPA, Flyway, Spring Validation (005-spring-security-rbac)
- PostgreSQL (banco compartilhado, esquema `calendario`); H2 em memória para testes (005-spring-security-rbac)
- PostgreSQL (banco compartilhado, esquema `calendario`) e H2 em memória para testes (005-spring-security-rbac)
- PostgreSQL (schema `calendario`) em runtime; H2 em testes de integração (006-cancelamento-evento)
- Java 21 + Spring Boot 3.3.13 (Web, Validation, Security, Data JPA, Actuator), Flyway 10.22.0, PostgreSQL driver (008-observacoes-evento-crud)
- PostgreSQL (schema `calendario`) em runtime; H2 em testes de integracao (008-observacoes-evento-crud)
- PostgreSQL em runtime com migrations Flyway; H2 para testes de integração (009-audit-observability-closure)

- Java 21 + Spring Boot 3.x (Web, Validation, Data JPA), Spring Security (resource server/JWT), Jackson, Flyway, PostgreSQL driver, springdoc-openapi (001-parish-calendar-api)

## Project Structure

```text
app/
	src/
		main/
			java/
			resources/
		test/
			java/
			resources/
specs/
	001-parish-calendar-api/
```

## Commands

- `./gradlew clean test`
- `./gradlew :app:test`
- `./gradlew :app:bootRun`
- `./gradlew :app:build`

## Code Style

Java 21: Follow standard conventions

## Recent Changes
- 009-audit-observability-closure: Added Java 21 + Spring Boot 3.3.13 (Web, Validation, Security, Data JPA, Actuator), Flyway 10.22.0, PostgreSQL driver
- 008-observacoes-evento-crud: Added Java 21 + Spring Boot 3.3.13 (Web, Validation, Security, Data JPA, Actuator), Flyway 10.22.0, PostgreSQL driver
- 007-auto-approval-event-flow: Added Java 21 + Spring Boot 3.3.5 (Web, Validation, Security, Data JPA, Actuator), Flyway 10.20.1


<!-- MANUAL ADDITIONS START -->
<!-- MANUAL ADDITIONS END -->
