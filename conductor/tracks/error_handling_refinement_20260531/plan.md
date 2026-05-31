# Plano de Implementação: Melhoria de Apresentação e Detalhamento de Erros

## Fase 1: Padronização e Tratamento de Erros no Backend [checkpoint: a28d034]
- [x] Task: Implementar e padronizar mapeamento de exceções e erros de negócio no backend (f28248f)
    - [x] Criar testes de integração para validar o JSON de erro para `ForbiddenOperationException`, `AccessDeniedException` e violações de integridade (banco de dados)
    - [x] Atualizar os handlers `DomainExceptionHandler`, `SecurityExceptionHandler` e `GlobalExceptionHandler` para retornar a estrutura padronizada com códigos de erro consistentes
- [x] Task: Conductor - User Manual Verification 'Fase 1: Padronização e Tratamento de Erros no Backend' (Protocol in workflow.md)

## Fase 2: Componente Global de Toast (Snackbar) no Frontend
- [x] Task: Implementar o provedor global de Notificações (Alert Snackbar) (1f7349a)
    - [x] Criar testes unitários para o componente `NotificationProvider` validando renderização de severidade (sucesso, erro) e fechamento automático
    - [x] Criar o provedor de contexto de notificações (`src/context/NotificationContext.tsx`) e integrá-lo na raiz da aplicação
- [x] Task: Conductor - User Manual Verification 'Fase 2: Componente Global de Toast (Snackbar) no Frontend' (Protocol in workflow.md)

## Fase 3: Integração dos Handlers de Erro nos Formulários da UI
- [ ] Task: Integrar tratamento de exceções da API com os Toasts nas telas de cadastro
    - [ ] Criar testes unitários nas telas de Projetos e Calendário simulando falhas da API e checando chamada do Toast
    - [ ] Atualizar formulários e ações em `Projects.tsx`, `Calendar.tsx` e `Approvals.tsx` para direcionar mensagens de falha detalhadas para o Toast global
- [ ] Task: Conductor - User Manual Verification 'Fase 3: Integração dos Handlers de Erro nos Formulários da UI' (Protocol in workflow.md)
