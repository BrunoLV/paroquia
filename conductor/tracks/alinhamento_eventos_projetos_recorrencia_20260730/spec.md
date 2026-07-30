# Specification: Auditoria, Consistência e Alinhamento do Cadastro de Eventos, Projetos e Recorrência (API x UI)

## Overview
Esta track tem como objetivo realizar uma auditoria completa de consistência no fluxo de cadastro, edição, cancelamento, vinculação a projetos e eventos recorrentes na aplicação (`gestao-paroquia` e `gestao-paroquia-ui`), garantindo alinhamento 100% estrito entre as regras de negócio da API backend (Spring Boot / DDD) e a interface do usuário (React / Material UI).

## Functional Requirements
1. **Auditoria de DTOs e Validações de Formulário:**
   - Garantir que todos os campos de `CreateEventoRequest` e `UpdateEventoRequest` (título, descrição, categoria, organização responsável, igreja de vínculo, local, projeto, participantes, recorrencia, status, justificativa extra, bypassAgendaConflict) sejam devidamente representados e validados no formulário (`EventRegistrationModal.tsx`).
   - Validar limites de caracteres, obrigatoriedade de campos de acordo com a categoria/status e tratamento de campos nulos/opcionais.

2. **Consistência de Eventos Recorrentes (`RecurrenceForm.tsx` & API de Recorrência):**
   - Validar a criação, atualização e encerramento de regras de recorrência (`SEMANAL`, `MENSAL`, `ANUAL`, limites por ocorrências ou por data final).
   - Garantir que a integração do `RecurrenceForm` dentro do `EventRegistrationModal.tsx` reflita corretamente os payloads aceitos pelo backend (`PUT /api/v1/eventos/{id}/recorrencia` ou payloads embutidos).

3. **Consistência do Vínculo e Governança de Projetos (`Projects.tsx` & Backend de Projetos):**
   - Assegurar que a seleção de Projetos no formulário respeite o período operacional do Projeto (`inicio` e `fim`), desabilitando o salvamento e exibindo alertas inline quando a data do evento ou a regra de recorrência exceder os limites do projeto.
   - Garantir a sincronização do status do Projeto e fluxos de aprovação de eventos vinculados.

4. **Consistência de Regras Eclesiais e Governança:**
   - Garantir que o campo de vínculo comunitário (`igrejaVinculoId` / `community_id`) seja obrigatório e coerente com o escopo territorial do usuário logado.
   - Validar a aplicação do bypass de conflito de agenda (`bypassAgendaConflict`), permitindo sua exibição/interação exclusivamente para papéis autorizados (`PAROCO` / `ADMIN`).
   - Garantir a exibição correta dos níveis de prioridade da agenda (1-Solenidade, 2-Litúrgico, 3-Pastoral, 4-Movimento).

5. **Tratamento de Erros e Feedback Visual Unificado:**
   - Padronizar o tratamento e a apresentação visual de exceções da API (como `ConflitoAgendaEclesialException`, `ConflitoAgendaException`, erros de validação `ValidationErrorResponse`) usando o `NotificationContext` (Toasts de erro).

6. **Modais e Componentes Envolvidos:**
   - `EventRegistrationModal.tsx` (Cadastro e Edição de Eventos e Recorrência)
   - `RecurrenceForm.tsx` (Configuração de Recorrência)
   - `EventCancelDialog.tsx` (Cancelamento de Eventos com Justificativa)
   - `EventObservations.tsx` (Visualização e Adição de Observações)
   - `Projects.tsx` (Gerenciamento e Seleção de Projetos)
   - `Calendar.tsx` (Integração e Exibição da Agenda)

## Non-Functional Requirements
- **Test-Driven Development (TDD):** Manter cobertura de testes no backend (JUnit/Integration tests) e no frontend (Vitest / React Testing Library) para todos os fluxos validados.
- **Aparência e Design System:** Respeitar o layout angulado (0px border-radius), paleta Marian e padrão de alinhamento de botões à direita.

## Acceptance Criteria
- [ ] Todos os DTOs de eventos, projetos e recorrência possuem representação idêntica e validada na UI.
- [ ] O vínculo entre Eventos, Projetos e Recorrência valida limites de datas e regras de recorrência sem inconsistências.
- [ ] Tentativas de burlar regras eclesiais via UI resultam em mensagens claras de erro amigáveis sem exceções não tratadas.
- [ ] A suíte completa de testes no backend e frontend executa com 0 erros.

## Out of Scope
- Alterações em outros módulos não relacionados a Eventos, Projetos e Recorrência (ex: Módulo de Finanças ou Membros).
