# Plan: Enforcement de Qualidade do Harness de Testes

## Fase 1 — Quality Gates do Backend

- [x] **Tarefa 1.1:** Adicionar `jacocoTestCoverageVerification` com thresholds 80%/55% no `app/build.gradle.kts` — `da722b97`
- [x] **Tarefa 1.2:** Substituir stub trivial (`ConsultaCalendarioPerformanceTest`) por teste real com P95 ≤ 2000ms sobre 30 eventos semeados — `da722b97`

## Fase 2 — Pipeline CI/CD

- [x] **Tarefa 2.1:** Criar `.github/workflows/ci.yml` com jobs paralelos `backend-ci` e `frontend-ci` — `1072e053`

## Fase 3 — Eliminação de Débito Técnico Checkstyle

- [x] **Tarefa 3.1:** Remover 160 imports não utilizados e 27 star imports em arquivos de teste — `7d5a5578`
- [x] **Tarefa 3.2:** Corrigir 27 imports duplicados gerados pela expansão de star imports — `7d5a5578`
- [x] **Tarefa 3.3:** Quebrar 36 linhas > 120 chars em main e test (Javadoc, throws, condicionais, asserções) — `7d5a5578`
- [x] **Tarefa 3.4:** Adicionar `{}` obrigatórias em blocos `if` sem chaves — `7d5a5578`
- [x] **Tarefa 3.5:** Extrair strings literais duplicadas para constantes (PAPEL_COORDENADOR, PAPEL_VICE, COR_BRANCO, CAMPO_ALOCACAO) — `7d5a5578`
- [x] **Tarefa 3.6:** Corrigir FQN inline e auto-referência de constante — `7d5a5578`

**Status Final:** `./gradlew :app:check BUILD SUCCESSFUL` — zero violações Checkstyle em main e test.
