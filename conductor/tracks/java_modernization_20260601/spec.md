# Specification: Java 25 & Frameworks Modernization in gestao-paroquia

## Overview
This chore focuses on modernizing the Java runtime, build tool, and dependency versions for the backend monolithic service (`gestao-paroquia`). It involves upgrading Java from version 21 to Java 25, updating Spring Boot to version 4.0.6 (leveraging its native Java 25 support), updating Gradle to 9.1.0+ to support Java 25 compilation, and bringing key dependencies (Flyway, SpringDoc OpenAPI, Apache POI, OpenPDF, Caffeine, PostgreSQL driver, H2, JUnit Jupiter) to their latest stable releases. 

## Scope & Core Objectives
1. **Java Upgrade**: Move compilation and runtime target to Java 25.
2. **Build System Upgrade**: Upgrade Gradle Wrapper to **9.1.0** (or higher) to support compilation on Java 25.
3. **Framework Upgrade**: Upgrade Spring Boot from 3.3.13 to **4.0.6**.
4. **Dependency Modernization**: Upgrade dependencies in `libs.versions.toml` to their latest stable releases:
   - Springdoc OpenAPI (`springdoc-openapi-starter-webmvc-ui`): **3.0.3**
   - Flyway (`flyway-core` & `flyway-database-postgresql`): **12.7.0**
   - Apache POI (`poi-ooxml`): **5.5.1**
   - OpenPDF (`openpdf`): **3.0.5**
   - Caffeine Cache (`caffeine`): **3.2.4**
   - Guava (`guava`): **33.6.0-jre**
   - JUnit Jupiter (`junit-jupiter`): **6.1.0**
   - SonarQube Plugin (`sonarqube-plugin`): **7.3.0.8198**
5. **Docker Environment Modernization**:
   - Update `Dockerfile.api` base image to `eclipse-temurin:25-jre-alpine`
   - Update `Dockerfile.builder` base image to `eclipse-temurin:25-jdk-alpine`
6. **Code & Tests Validation**:
   - Resolve compilation issues due to deprecations/removals in newer JDK or Spring Boot 4.0.6 versions.
   - Run tests to ensure complete compatibility and resolve test failures.
   - Validate continuous execution in the local Dockerized environment.

## Acceptance Criteria
- [ ] The `gestao-paroquia` project compiles and runs successfully using Java 25.
- [ ] Gradle wrapper is upgraded to 9.1.0+ and runs build commands successfully.
- [ ] `build.gradle.kts` uses toolchain version 25.
- [ ] Spring Boot dependency version is updated to Spring Boot 4.0.6.
- [ ] Version catalog (`libs.versions.toml`) is modernized with the specified versions.
- [ ] Docker images use `eclipse-temurin:25` (alpine) as their base image.
- [ ] All automated tests pass successfully (`./gradlew test`).
- [ ] Code coverage (>80%) is maintained.
- [ ] The entire system runs inside Docker Compose without startup issues.

## Out of Scope
- Upgrading frontend dependencies (`gestao-paroquia-ui`).
- Implementing new features or business logic changes.
