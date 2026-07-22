# Specification: Padronização e Modernização de Layout da UI, Botões, Mensagens, Formulários e Alertas

## Overview
Esta especificação estabelece a padronização, modernização e harmonização estética do sistema de design e componentes da interface do usuário em toda a aplicação `@gestao-paroquia-ui`. O objetivo é assegurar uma experiência de usuário (UX) coesa, consistente e moderna, aplicando rigorosamente as diretrizes de design da aplicação (Paleta Mariana: Azul Divino e Dourado Celestial, border-radius angulado 0px, tipografia hierarquizada e feedback visual padronizado).

## Functional & Technical Requirements

1. **Padronização de Formulários e Modais de Cadastro**:
   - Unificar layout, espaçamentos e comportamentos em todos os modais de formulário (`EventRegistrationModal`, `OrganizacaoFormModal`, `LocalFormModal`, etc.).
   - Alinhar botões de ação nos modais à direita (cancelar à esquerda/secundário e salvar/confirmar à direita/primário).
   - Implementar feedback de validação de campos em tempo real com mensagens de erro claras sob os inputs.

2. **Botões, Tipografia, Ícones e Links**:
   - Aplicar border-radius angulado (`borderRadius: 0`) em todos os botões, badges e cartões de controle.
   - Utilizar a paleta de cores institucional Mariana (Primary Blue `#0288d1`, Secondary Gold `#00796b`, Alert Red `#d32f2f`).
   - Padronizar hierarquia tipográfica com pesos 600/700 para títulos e tamanho adaptável.

3. **Feedback Visual e Sistema de Notificações**:
   - Integrar todas as telas e componentes ao `NotificationContext` unificado (`showSuccess` / `showError`).
   - Padronizar toasts/snackbars e alertas no topo da tela com ícones contextuais e fechamento automático.
   - Implementar Skeletons de carregamento unificados durante requisições de API para eliminar saltos de layout.

4. **Tabelas, Listas e Cards de Conteúdo**:
   - Harmonizar estilos de cards estatísticos, tabelas de listagem e listas de progresso com elevação 0, bordas suaves e hover micro-interações.

## Non-Functional Requirements
- **Acessibilidade (WCAG 2.1 AA)**: Garantir contraste auditado em botões, links e badges.
- **Responsividade**: Layout adaptável para telas mobile (<600px), tablet (600-900px) e desktop (>900px).
- **Desempenho & Testes**: Manter 100% dos testes frontend (`npm test` e `npm run build`) integrados e passando sem regressão.

## Acceptance Criteria
- Todos os modais de cadastro e formulários do sistema seguem o layout e padrão de botões unificado.
- As mensagens de erro e sucesso são exibidas exclusivamente através do `NotificationContext`.
- O build de produção (`npm run build`) e os testes unitários (`npm test`) finalizam com 100% de sucesso.
