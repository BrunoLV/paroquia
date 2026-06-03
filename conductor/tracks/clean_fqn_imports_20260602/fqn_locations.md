# Identified Inline FQN Locations

The following 34 inline fully qualified names (FQNs) were identified in the Java codebase of `gestao-paroquia`:

## Main Codebase (`app/src/main/java`)

1. **`AnoParoquialController.java:144`**:
   - `java.time.LocalDate.now` in `int currentCivilYear = java.time.LocalDate.now().getYear();`
2. **`ExportacaoCalendarioUseCase.java:100`**:
   - `org.springframework.data.domain.Pageable` and `org.springframework.data.domain.PageRequest.of` in `org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(`
3. **`ExportacaoCalendarioUseCase.java:103`**:
   - `org.springframework.data.domain.Sort.by` and `org.springframework.data.domain.Sort.Direction.ASC` in `org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "inicioUtc")`
4. **`EventoService.java:35`**:
   - `java.util.Optional.ofNullable` in `java.util.List<String> categories = java.util.Optional.ofNullable(filters.categoria())`
5. **`EventoService.java:39`**:
   - `java.util.Optional.ofNullable` in `java.util.List<String> statuses = java.util.Optional.ofNullable(filters.status())`
6. **`DomainExceptionHandler.java:164`**:
   - `org.springframework.dao.DataIntegrityViolationException.class` in `@ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)`
7. **`DomainExceptionHandler.java:166`**:
   - `org.springframework.dao.DataIntegrityViolationException` in `org.springframework.dao.DataIntegrityViolationException ex) {`
8. **`DomainExceptionHandler.java:187`**:
   - `org.springframework.dao.DataAccessException.class` in `@ExceptionHandler(org.springframework.dao.DataAccessException.class)`
9. **`DomainExceptionHandler.java:188`**:
   - `org.springframework.dao.DataAccessException` in `public ResponseEntity<ValidationErrorResponse> handleDataAccess(org.springframework.dao.DataAccessException ex) {`
10. **`ValidationExceptionHandler.java:115`**:
    - `org.springframework.validation.ObjectError` in `private ValidationErrorItem toValidationItem(org.springframework.validation.ObjectError error) {`
11. **`ValidationExceptionHandler.java:222`**:
    - `org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class` in `@ExceptionHandler(org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class)`
12. **`ValidationExceptionHandler.java:224`**:
    - `org.springframework.web.method.annotation.MethodArgumentTypeMismatchException` in `org.springframework.web.method.annotation.MethodArgumentTypeMismatchException ex) {`
13. **`EventoAuditPublisher.java:236`**:
    - `java.util.UUID.fromString` in `java.util.UUID.fromString(value);`
14. **`EventoEntity.java:63`**:
    - `jakarta.persistence.FetchType.EAGER` in `@OneToMany(fetch = jakarta.persistence.FetchType.EAGER)`
15. **`SecurityConfig.java:25`**:
    - `org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity` in `@org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity`
16. **`CreateProjetoUseCase.java:68`**:
    - `java.time.ZoneOffset.UTC` in `int startYear = request.inicio().atZone(java.time.ZoneOffset.UTC).getYear();`
17. **`CreateProjetoUseCase.java:94`**:
    - `java.time.Instant.now` in `approval.setCriadoEmUtc(java.time.Instant.now());`
18. **`ProjetoVincularPolicy.java:57`**:
    - `br.com.nsfatima.gestao.calendario.domain.type.RegraRecorrencia` in `public void validateRecurrence(UUID projetoId, Instant firstStart, Instant firstEnd, br.com.nsfatima.gestao.calendario.domain.type.RegraRecorrencia regra) {`
19. **`ProjetoVincularPolicy.java:77`**:
    - `java.time.ZoneOffset.UTC` in `LocalDate endOfProject = projeto.getFimUtc().atZone(java.time.ZoneOffset.UTC).toLocalDate();`
20. **`ProjetoVincularPolicy.java:88`**:
    - `java.time.ZoneOffset.UTC` in `LocalDate startOfSeries = firstStart.atZone(java.time.ZoneOffset.UTC).toLocalDate();`

## Test Codebase (`app/src/test/java`)

21. **`FakeEventoRepository.java:190`**:
    - `java.util.Comparator.comparing` in `.sorted(java.util.Comparator.comparing(EventoEntity::getInicioUtc))`
22. **`WeeklyMetricsSnapshotIntegrationTest.java:25`**:
    - `java.util.List.of` in `.thenReturn(java.util.List.of());`
23. **`LegacyEventoReadCompatibilityIntegrationTest.java:21`**:
    - `org.springframework.test.context.jdbc.Sql` in `@org.springframework.test.context.jdbc.Sql(scripts = "classpath:sql/security-fixtures.sql")`
24. **`ProjetoMutacaoContractTest.java:37`**:
    - `java.time.Instant.parse` in `entity.setInicioUtc(java.time.Instant.parse("2026-01-01T00:00:00Z"));`
25. **`ProjetoMutacaoContractTest.java:38`**:
    - `java.time.Instant.parse` in `entity.setFimUtc(java.time.Instant.parse("2026-12-31T23:59:59Z"));`
26. **`ProjetosContractTest.java:38`**:
    - `java.time.Instant.parse` in `entity.setInicioUtc(java.time.Instant.parse("2026-01-01T00:00:00Z"));`
27. **`ProjetosContractTest.java:39`**:
    - `java.time.Instant.parse` in `entity.setFimUtc(java.time.Instant.parse("2026-12-31T23:59:59Z"));`
28. **`ExceptionHandlerIntegrationTest.java:86`**:
    - `org.springframework.dao.DataIntegrityViolationException` in `throw new org.springframework.dao.DataIntegrityViolationException(`
29. **`EventoControllerGetTest.java:28`**:
    - `org.springframework.jdbc.core.JdbcTemplate` in `private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;`
30. **`JobLockJpaRepositoryTest.java:22`**:
    - `java.time.temporal.ChronoUnit.SECONDS` in `Instant now = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);`
31. **`MembroControllerIntegrationTest.java:111`**:
    - `br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroEntity` in `var entity = new br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroEntity();`
32. **`MembroControllerIntegrationTest.java:138`**:
    - `br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroEntity` in `var entity = new br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroEntity();`
33. **`ProjetoAgregacaoServiceTest.java:48`**:
    - `java.time.temporal.ChronoUnit.DAYS` in `projeto.setInicioUtc(agora.minus(10, java.time.temporal.ChronoUnit.DAYS));`
34. **`ProjetoAgregacaoServiceTest.java:49`**:
    - `java.time.temporal.ChronoUnit.DAYS` in `projeto.setFimUtc(agora.plus(10, java.time.temporal.ChronoUnit.DAYS));`
