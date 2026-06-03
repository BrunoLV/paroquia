# Plan: Java 25 & Frameworks Modernization in gestao-paroquia

## Phase 1: Environment & Build Tool Upgrades [checkpoint: 6d59040]
- [x] Task: Upgrade Gradle Wrapper to 9.1.0+ 16c8633
    - [x] Update Gradle wrapper properties in `gradle/wrapper/gradle-wrapper.properties` to reference Gradle 9.1.0 or higher.
    - [x] Run `./gradlew --version` to verify Gradle runtime works.
- [x] Task: Update Java Target & Toolchain to Java 25 819fd22
    - [x] Modify `app/build.gradle.kts` to target JavaLanguageVersion.of(25).
    - [x] Verify local gradle build compiles with JDK 25.
- [x] Task: Conductor - User Manual Verification 'Phase 1: Environment & Build Tool Upgrades' (Protocol in workflow.md) 6d59040

## Phase 2: Dependency & Framework Modernization [checkpoint: 28bfe55]
- [x] Task: Modernize Spring Boot and Dependency Management Versions 15f5729
    - [x] Update Spring Boot plugin version to `4.0.6` in `gradle/libs.versions.toml`.
    - [x] Update Spring Dependency Management plugin version to `1.1.7` in `gradle/libs.versions.toml`.
- [x] Task: Update Core Libraries and Drivers 8b91192
    - [x] Modernize Springdoc OpenAPI version to `3.0.3` in `gradle/libs.versions.toml`.
    - [x] Modernize Flyway plugin version to `12.7.0` in `gradle/libs.versions.toml`.
    - [x] Modernize Caffeine Cache to `3.2.4` and Guava to `33.6.0-jre` in `gradle/libs.versions.toml`.
    - [x] Modernize Apache POI to `5.5.1` and OpenPDF to `3.0.5` in `gradle/libs.versions.toml`.
    - [x] Modernize JUnit Jupiter to `6.1.0` in `gradle/libs.versions.toml`.
- [x] Task: Conductor - User Manual Verification 'Phase 2: Dependency & Framework Modernization' (Protocol in workflow.md) 28bfe55

## Phase 3: Code Adjustments & Test Verification
- [x] Task: Resolve Deprecations and Compilation Errors 51860c4
    - [x] Run `./gradlew compileJava` and solve compilation issues or API changes.
    - [x] Adjust configurations and imports affected by breaking changes in Spring Boot 4.0.6.
- [x] Task: Execute and Verify Automated Test Suite 51860c4
    - [x] Run tests using `./gradlew test`.
    - [x] Solve any regression or test failures caused by dependency updates.
    - [x] Verify code coverage is maintained above 80%.
- [~] Task: Conductor - User Manual Verification 'Phase 3: Code Adjustments & Test Verification' (Protocol in workflow.md)

## Phase 4: Container Environment Update & Integration
- [ ] Task: Update Docker Configuration
    - [ ] Modify `Dockerfile.api` to use `eclipse-temurin:25-jre-alpine`.
    - [ ] Modify `Dockerfile.builder` to use `eclipse-temurin:25-jdk-alpine`.
- [ ] Task: Docker Compose Execution & Verification
    - [ ] Rebuild and run all services with `docker-compose up --build -d`.
    - [ ] Check container logs to verify backend starts successfully and connects to PostgreSQL/migrations run.
- [ ] Task: Conductor - User Manual Verification 'Phase 4: Container Environment Update & Integration' (Protocol in workflow.md)
