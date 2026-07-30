# Implementation Plan: Motor de Governança Temporal e Sazonalidade Litúrgica

**Track ID:** `motor_governanca_temporal_liturgica_20260730`

---

## Phase 1: Backend Domain, Schema, Liturgical Engine & Database Seed
- [ ] Task: Criar migração Flyway `V050__create_tabela_anos_liturgicos.sql` e atualizar Docker seed
    - [ ] Criar tabela `calendario.anos_liturgicos` com FKs em `calendario.eventos` / `tb_projeto_evento`
    - [ ] Adicionar seed inicial Flyway e Docker seed com no mínimo 10 Anos Litúrgicos (2026 a 2035, ciclos A, B, C) e a malha de ciclos litúrgicos correspondentes
- [ ] Task: Implementar Entidade JPA `AnoLiturgicoEntity` e Repositório `AnoLiturgicoRepository`
    - [ ] Criar record `AnoLiturgico`, JPA Entity `AnoLiturgicoEntity` e repositório Spring Data JPA
    - [ ] Escrever testes de integração em `@SpringBootTest` com H2 para `AnoLiturgicoRepository`
- [ ] Task: Implementar Motor Litúrgico `LiturgicalEngine` (TDD)
    - [ ] Escrever testes unitários em `LiturgicalEngineTest` cobrindo cálculo da Páscoa (Meeus/Jones/Butcher) e início do Advento
    - [ ] Implementar `LiturgicalEngine` para calcular/resolver o Ano Litúrgico e Ciclo Litúrgico sazonal a partir de datas UTC
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Backend Domain, Schema, Liturgical Engine & Database Seed' (Protocol in workflow.md)

## Phase 2: Use Cases Integration, Calendar Lock & Silent Import Resolution
- [ ] Task: Integrar Resolução Litúrgica e Lock Policy nos Use Cases de Evento (TDD)
    - [ ] Atualizar `CreateProjetoEventoUseCase` / `ProjetoEventoService` para validar `CalendarLockPolicy` e associar `anoLiturgicoRef` e `cicloLiturgicoId`
    - [ ] Escrever testes unitários/integração TDD cobrindo criação em anos abertos/fechados
- [ ] Task: Implementar Ingestão em Lote e Resolução Silenciosa de Conflitos em `LiturgicalImportService` (TDD)
    - [ ] Implementar tratamento para importação CSV/XLSX em ano paroquial fechado (conversão para status `PENDENTE` e fila de aprovação sem quebrar o lote)
    - [ ] Escrever testes unitários em `LiturgicalImportServiceTest`
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Use Cases Integration, Calendar Lock & Silent Import Resolution' (Protocol in workflow.md)

## Phase 3: Frontend Calendar Lock, Liturgical Indicators & Executive Dashboard UI
- [ ] Task: Implementar Trava de Ano Paroquial Fechado e Form de Inclusão Extra em `gestao-paroquia-ui`
    - [ ] Atualizar `Calendar.tsx` e `EventRegistrationModal.tsx` para exigir justificativa textual em anos fechados
    - [ ] Escrever testes unitários em Vitest para `EventRegistrationModal.test.tsx`
- [ ] Task: Implementar Indicadores Litúrgicos Visuais no Calendário e Dashboard
    - [ ] Exibir cores canônicas e badges de fator de demanda nos eventos da grade do calendário
    - [ ] Atualizar `Dashboard.tsx` para exibir a curva de sazonalidade e governança temporal
    - [ ] Escrever testes de integração no Vitest (`npm test`) garantindo zero regressões
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Frontend Calendar Lock, Liturgical Indicators & Executive Dashboard UI' (Protocol in workflow.md)
