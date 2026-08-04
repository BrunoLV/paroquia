# Plan: Enforcement de Qualidade do Harness de Testes

## Fase 1 — Quality Gates do Backend

- [x] **Tarefa 1.1:** Adicionar `jacocoTestCoverageVerification` com thresholds 80%/55% no `app/build.gradle.kts` — `da722b97`
- [x] **Tarefa 1.2:** Substituir stub trivial (`ConsultaCalendarioPerformanceTest`) por teste real com P95 ≤ 2000ms sobre 30 eventos semeados — `da722b97`

## Fase 2 — Pipeline CI/CD

- [x] **Tarefa 2.1:** Criar `.github/workflows/ci.yml` com jobs paralelos `backend-ci` e `frontend-ci` — `1072e053`
