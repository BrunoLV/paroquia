# Specification: Event Project Period Validation

## Overview
Esta especificação descreve a regra de validação para associar eventos a projetos. Um evento só pode ser vinculado a um projeto se todo o seu período estiver contido dentro do período (data de início e fim) do projeto. A validação será executada tanto no backend (regras de negócio da API) quanto no frontend (validação de formulário em tempo real).

## Functional Requirements
1. **Backend Validation Rules**:
   - Ao criar ou atualizar um Evento (`Event`), se o campo `projetoId` estiver preenchido, a API deve carregar os detalhes do Projeto.
   - Se o projeto não possuir data de início ou data de fim configuradas (valores nulos), a validação deve ser ignorada.
   - Caso o projeto possua datas de início/fim configuradas:
     - Para eventos simples: a data/hora de início do evento deve ser igual ou posterior à data de início do projeto, e a data/hora de fim do evento deve ser igual ou anterior à data de fim do projeto.
     - Para eventos recorrentes: a data de início da primeira ocorrência e a data final limite da recorrência (ou todas as ocorrências individuais) devem cair inteiramente dentro do período do projeto.
   - Se a validação falhar, a API deve retornar `HTTP 400 Bad Request` com um código de erro padronizado e uma mensagem descritiva (ex: "O período do evento está fora do limite definido para o projeto [Nome do Projeto]").

2. **Frontend UI Validation**:
   - Ao preencher o formulário de cadastro ou edição de eventos (`EventRegistrationModal.tsx`):
     - Ao selecionar um projeto, o formulário deve exibir um aviso claro em tempo real caso a data do evento selecionado caia fora do intervalo permitido para o projeto.
     - O botão de salvar deve ser desabilitado ou exibir uma mensagem de validação ao tentar submeter um formulário inválido.
     - Exibir as datas limite do projeto selecionado para ajudar o usuário a escolher uma data válida.

3. **Strategic Exclusions (Retroactivity)**:
   - A alteração do período de um Projeto não deve ser impedida retroativamente por eventos associados no passado que fiquem fora das novas datas. A validação aplica-se estritamente às operações de criação e edição do Evento.

## Non-Functional Requirements
- **Consistency**: The validation logic must be identical between frontend and backend.
- **A11y**: Validation error messages must be accessible (screen reader friendly) and follow standard UI styling.

## Acceptance Criteria
- [ ] Backend blocks event creation/updating with `400 Bad Request` if event dates fall outside the project dates.
- [ ] Backend allows event creation/updating if the project dates are null (undefined).
- [ ] Backend correctly validates recurring events ensuring all occurrences fall within the project duration.
- [ ] UI displays inline warning if the selected event dates do not align with the project dates.
- [ ] UI prevents form submission with visual validation feedback when dates are invalid.
- [ ] Standard test suites in both backend and frontend are added/updated and pass cleanly.

## Out of Scope
- Restricting project date updates based on already linked events.
- Creating a separate page/view for event-project conflict management.
