# Spec: Enforcement de Qualidade do Harness de Testes

## Contexto
Avaliação do harness de testes identificou três lacunas críticas (P0/P1) que comprometem a confiabilidade da suíte de testes:

1. **Ausência de CI/CD**: Todo o pipeline de qualidade depende de disciplina manual do desenvolvedor — sem GitHub Actions, nenhum PR é validado automaticamente.
2. **Ausência de threshold de cobertura JaCoCo**: O backend gera reports de cobertura mas não possui quality gate; a cobertura pode regredir silenciosamente.
3. **Testes de performance são stubs vazios**: 5 arquivos de performance com asserts triviais (`assertTrue(2000 <= 2000)`) geram falsa confiança sem medir nada real.

## Objetivo
Implementar os quality gates de cobertura, remover/substituir os testes de performance falsos, e criar a pipeline de CI mínima.

## Escopo

### Tarefa 1 — JaCoCo Coverage Threshold (backend)
- Adicionar task `jacocoTestCoverageVerification` no `app/build.gradle.kts` com threshold mínimo de 70% (instrução) e 65% (branch).
- Vincular a task ao ciclo de build (`check` depende dela).
- Executar `./gradlew test jacocoTestCoverageVerification` para validar que passa.

### Tarefa 2 — Testes de Performance Reais ou Removidos (backend)
- Avaliar os 5 testes de performance stub (`ConsultaCalendarioPerformanceTest`, `AuditoriaTier1PerformanceTest`, `CancelEventoTier1PerformanceTest`, `ObservacaoTier1PerformanceTest`, `UpdateEventoTier1PerformanceTest`).
- Substituí-los por testes que meçam latência real de operações locais (sem rede externa), usando `StopWatch` ou `Instant.now()`.
- Testes devem usar dados reais via H2/Flyway e medir a execução em memória.
- SLA: P95 ≤ 500ms para operações de leitura simples; ≤ 1000ms para escrita.

### Tarefa 3 — Pipeline CI/CD via GitHub Actions
- Criar `.github/workflows/ci.yml` com:
  - Trigger: `push` e `pull_request` para qualquer branch
  - Job `backend-ci`: Java 25, cache Gradle, `./gradlew check` (inclui Checkstyle, PMD, testes, JaCoCo)
  - Job `frontend-ci`: Node 20, cache npm, `npm ci && npm test && npm run build`
  - Ambos os jobs executam em paralelo

## Critérios de Aceite
- [ ] `./gradlew check` falha se cobertura cair abaixo do threshold
- [ ] Os 5 testes de performance medem operações reais e têm asserts não-triviais
- [ ] O arquivo `.github/workflows/ci.yml` executa ambos os projetos
