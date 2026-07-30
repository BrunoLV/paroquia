# Implementation Plan: Auditoria, Consistência e Alinhamento do Cadastro de Eventos, Projetos e Recorrência (API x UI)

## Phase 1: Auditoria e Alinhamento de DTOs, Eventos e Regras de Validação no Backend e Frontend
- [x] Task: Auditar DTOs de solicitação (`CreateEventoRequest`, `UpdateEventoRequest`, `RecorrenciaRequest`) e garantir correspondência 1:1 com os tipos TypeScript (`types.ts`). (38824c7)
- [x] Task: Verificar mapeamento de exceções e respostas de erro (`ConflitoAgendaEclesialException`, `ConflitoAgendaException`, `ValidationErrorResponse`) no handler global. (818b600)
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Auditoria e Alinhamento de DTOs e Regras de Validação' (Protocol in workflow.md)

## Phase 2: Auditoria e Validação do Formulário de Eventos, Projetos e Recorrência na UI
- [ ] Task: Auditar `EventRegistrationModal.tsx` e `RecurrenceForm.tsx` garantindo preenchimento, limites de caracteres e transmissão de campos obrigatórios (`igrejaVinculoId`, `bypassAgendaConflict`).
- [ ] Task: Auditar validações de vínculo com Projetos em `EventRegistrationModal.tsx` e `Projects.tsx`, garantindo bloqueio quando a data do evento/recorrência extrapola as datas do projeto.
- [ ] Task: Auditar `EventCancelDialog.tsx` e `EventObservations.tsx` garantindo tratamento e confirmação visual de status de cancelamento/justificativas.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Auditoria e Validação do Formulário de Eventos, Projetos e Recorrência na UI' (Protocol in workflow.md)

## Phase 3: Testes de Integração E2E e Validação da Suíte Completa
- [ ] Task: Escrever testes unitários e de integração (Vitest e JUnit) simulando divergências de payloads, erros de concorrência e bypass de agenda.
- [ ] Task: Executar suite completa de testes (`./gradlew test` e `npm test`).
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Testes de Integração E2E e Validação da Suíte Completa' (Protocol in workflow.md)
