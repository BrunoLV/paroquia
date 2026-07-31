# Implementation Plan - Reestruturação do Motor Litúrgico, Precedência e Governança Eclesial

## Phase 1: Backend - Reestruturação do Motor Litúrgico Universal (`LiturgicalEngine`) com Records Java 25
- [x] Task: Criar Records e Enums Litúrgicos imutáveis e implementar o novo algoritmo stateless em `LiturgicalEngine.java` 88a3caf
    - [x] Write unit tests in `LiturgicalEngineTest.java` for Java 25 Records (`LiturgicalOutput`, `MarcoLiturgico`), cycle formula (`1->A, 2->B, 0->C`), movable offsets and fixed solemnities
    - [x] Implement `LiturgicalOutput.java`, `MarcoLiturgico.java`, `CorLiturgica.java` and pure calculation methods in `LiturgicalEngine.java`
    - [x] Confirm all tests pass across years 2024-2030 and satisfy monorepo Checkstyle rules
- [x] Task: Conductor - User Manual Verification 'Phase 1: Backend - Reestruturação do Motor Litúrgico Universal (LiturgicalEngine) com Records Java 25' (Protocol in workflow.md) 2f4c607

## Phase 2: Backend - Provisionamento, Tabela de Precedência e Governança Eclesial (`PAROCO`/`VIGARIO`)
- [ ] Task: Implementar exceção de regra imutável, controle de acesso estrito (`PAROCO`/`VIGARIO`) e refatorar `LiturgicalProvisioningService`
    - [ ] Write tests in `LiturgicalGovernanceTest.java` and `LiturgicalProvisioningServiceTest.java` asserting permission check for `PAROCO`/`VIGARIO`, `ImmutableEclesialRuleException`, automatic provisioning and 1-5 precedence order
    - [ ] Implement `ImmutableEclesialRuleException.java`, security checks, precedence ranking (1-5) and update dependent services (`CreateEventoUseCase`, `ImportacaoService`)
    - [ ] Confirm all backend tests pass and build completes cleanly
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Backend - Provisionamento, Tabela de Precedência e Governança Eclesial (PAROCO/VIGARIO)' (Protocol in workflow.md)

## Phase 3: Frontend - Sincronização do Motor Litúrgico e Precedência na UI (`Calendar.tsx`)
- [ ] Task: Sincronizar o utilitário `liturgicalCalendar.ts` e `Calendar.tsx` com a fórmula universal do ciclo e hierarquia de precedência
    - [ ] Update unit tests in `liturgicalCalendar.test.ts` and `Calendar.test.tsx` verifying liturgical cycle formula (`1->A, 2->B, 0->C`) and precedence styling
    - [ ] Update `liturgicalCalendar.ts` and `Calendar.tsx` applying strict cycle formula and visual precedence hierarchy
    - [ ] Confirm all UI tests pass cleanly
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Frontend - Sincronização do Motor Litúrgico e Precedência na UI (Calendar.tsx)' (Protocol in workflow.md)
