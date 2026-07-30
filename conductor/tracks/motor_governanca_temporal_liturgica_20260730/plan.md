# Implementation Plan: Motor de Governança Temporal e Sazonalidade Litúrgica

**Track ID:** `motor_governanca_temporal_liturgica_20260730`

---

## Phase 1: Backend Domain, Schema, Liturgical Engine & Database Seed
- [x] Task: Criar migração Flyway `V050__create_tabela_anos_liturgicos.sql` e atualizar Docker seed cee6f9c
    - [x] Criar tabela `calendario.anos_liturgicos` com FKs em `calendario.eventos` / `tb_projeto_evento`
    - [x] Adicionar seed inicial Flyway e Docker seed com no mínimo 10 Anos Litúrgicos (2026 a 2035, ciclos A, B, C) e a malha de ciclos litúrgicos correspondentes
- [x] Task: Implementar Entidade JPA `AnoLiturgicoEntity` e Repositório `AnoLiturgicoRepository` e7528fd
    - [x] Criar record `AnoLiturgico`, JPA Entity `AnoLiturgicoEntity` e repositório Spring Data JPA
    - [x] Escrever testes de integração em `@SpringBootTest` com H2 para `AnoLiturgicoRepository`
- [x] Task: Implementar Motor Litúrgico `LiturgicalEngine` (TDD) 97d8e93
    - [x] Escrever testes unitários em `LiturgicalEngineTest` cobrindo cálculo da Páscoa (Meeus/Jones/Butcher) e início do Advento
    - [x] Implementar `LiturgicalEngine` para calcular/resolver o Ano Litúrgico e Ciclo Litúrgico sazonal a partir de datas UTC
- [x] Task: Conductor - User Manual Verification 'Phase 1: Backend Domain, Schema, Liturgical Engine & Database Seed' (Protocol in workflow.md) c794437

## Phase 2: Use Cases Integration, Calendar Lock & Silent Import Resolution
- [x] Task: Integrar Resolução Litúrgica e Lock Policy nos Use Cases de Evento (TDD) 6b21838
    - [x] Atualizar `CreateProjetoEventoUseCase` / `ProjetoEventoService` para validar `CalendarLockPolicy` e associar `anoLiturgicoRef` e `cicloLiturgicoId`
    - [x] Escrever testes unitários/integração TDD cobrindo criação em anos abertos/fechados
- [x] Task: Implementar Ingestão em Lote e Resolução Silenciosa de Conflitos em `LiturgicalImportService` (TDD) 6b21838
    - [x] Implementar tratamento para importação CSV/XLSX em ano paroquial fechado (conversão para status `PENDENTE` e fila de aprovação sem quebrar o lote)
    - [x] Escrever testes unitários em `LiturgicalImportServiceTest`
- [x] Task: Conductor - User Manual Verification 'Phase 2: Use Cases Integration, Calendar Lock & Silent Import Resolution' (Protocol in workflow.md) a58ec3e

## Phase 3: Frontend Calendar Lock, Liturgical Indicators & Executive Dashboard UI
- [ ] Task: Implementar Trava de Ano Paroquial Fechado e Form de Inclusão Extra em `gestao-paroquia-ui`
    - [ ] Atualizar `Calendar.tsx` e `EventRegistrationModal.tsx` para exigir justificativa textual em anos fechados
    - [ ] Escrever testes unitários em Vitest para `EventRegistrationModal.test.tsx`
- [ ] Task: Implementar Indicadores Litúrgicos Visuais no Calendário e Dashboard
    - [ ] Exibir cores canônicas e badges de fator de demanda nos eventos da grade do calendário
    - [ ] Atualizar `Dashboard.tsx` para exibir a curva de sazonalidade e governança temporal
    - [ ] Escrever testes de integração no Vitest (`npm test`) garantindo zero regressões
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Frontend Calendar Lock, Liturgical Indicators & Executive Dashboard UI' (Protocol in workflow.md)
