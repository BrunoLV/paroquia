# Plan: Enforcement de Qualidade do Harness de Testes

## Fase 1 — Quality Gates do Backend

- [~] **Tarefa 1.1:** Adicionar `jacocoTestCoverageVerification` com thresholds 70%/65% no `app/build.gradle.kts`
- [ ] **Tarefa 1.2:** Substituir os 5 testes de performance stub por testes que meçam latência real

## Fase 2 — Pipeline CI/CD

- [ ] **Tarefa 2.1:** Criar `.github/workflows/ci.yml` com jobs paralelos para backend e frontend
