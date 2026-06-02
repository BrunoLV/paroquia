# Specification: Approval Workflow UI Integration

## Overview
Esta track implementa a interface do fluxo de aprovações no frontend (`gestao-paroquia-ui`), integrando-se com a API do backend (`/api/v1/aprovacoes`). Ela permitirá que usuários com papéis administrativos visualizem, aprovem ou rejeitem solicitações de criação, modificação ou cancelamento de eventos paroquiais.

## Functional Requirements

### 1. Página de Aprovações Dedicada (Nova Tela)
* Criar uma nova rota e componente em `src/pages/Approvals.tsx`.
* Adicionar um link "Aprovações" no menu lateral (`Layout.tsx`).
* Apresentar uma listagem paginada de solicitações de aprovação (usando `GET /api/v1/aprovacoes` com filtros de status).
* Por padrão, a listagem deve carregar solicitações com status `PENDENTE`. Permitir alternar os filtros para outros status (`APROVADA`, `REPROVADA`, `FALHA_EXECUCAO`).

### 2. Detalhamento e Decisão de Aprovações
* Ao selecionar uma solicitação na listagem, abrir um painel lateral (Drawer) ou modal de detalhamento.
* Exibir os detalhes da solicitação:
  * Identificação do solicitante (ID/nome/papel).
  * Data e hora da criação da solicitação.
  * Tipo de ação (`CRIACAO_EVENTO`, `ATUALIZACAO_EVENTO`, `CANCELAMENTO_EVENTO` ou enums correspondentes no backend).
  * Detalhes do evento afetado.
* Disponibilizar botões de ação: **Aprovar** e **Rejeitar**.
* Incluir um campo de texto opcional para justificativa/observação (máximo 2000 caracteres).
* Enviar a decisão chamando `PATCH /api/v1/aprovacoes/{id}` com o payload `{ status: 'APROVADA' | 'REPROVADA', observacao: '...' }`.
* Atualizar a lista local de aprovações após uma decisão bem-sucedida.

### 3. Exibição no Calendário de Eventos
* Exibir eventos que possuem aprovação pendente no `Calendar.tsx` com estilo visual atenuado (opaco/translúcido) e um badge ou indicador textual claro indicando "Pendente".
* O modal de exibição de detalhes de evento do calendário deve identificar claramente que o evento está pendente de aprovação.

### 4. Controle de Acesso (RBAC)
* A página de Aprovações e o menu lateral só devem estar visíveis e acessíveis para usuários cuja sessão ativa possua a Role `ADMIN` ou os papéis `CONSELHO_COORDENADOR` ou `PAROCO` na organização atual.
* Para os demais usuários, ocultar a opção no menu e bloquear o acesso à rota.

## Acceptance Criteria
* Apenas usuários ADMIN, CONSELHO_COORDENADOR e PAROCO conseguem acessar e interagir com a tela de aprovações.
* As solicitações de aprovação são listadas corretamente a partir do backend.
* O fluxo de decisão chama o endpoint PATCH correspondente, envia a observação e atualiza a listagem.
* Eventos pendentes são exibidos de forma translúcida no calendário principal com badge explicativo.
