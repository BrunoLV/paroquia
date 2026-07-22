# Specification: Melhorias Visuais e Responsivas nos Dashboards e Calendário do Frontend

## Overview
Esta especificação estabelece o aprimoramento visual, estético e de responsividade dos painéis e visualizações principais da aplicação (`Dashboard.tsx`, `Reports.tsx` e `Calendar.tsx`). O objetivo é elevar a experiência do usuário a um padrão de design premium, incorporando cards estatísticos com gradientes harmoniosos, micro-animações suaves, gráficos dinâmicos de distribuição, cabeçalhos refinados para o calendário e layout totalmente responsivo para dispositivos móveis, tablets e computadores.

## Functional & Technical Requirements

1. **Dashboard Principal (`Dashboard.tsx`)**:
   - **Cards de Métricas Chave**: Exibir estatísticas de eventos agendados, aprovações pendentes, pastorais ativas e membros cadastrados usando gradientes modernos, sombras suaves e ícones representativos.
   - **Visualização Gráfica**: Adicionar gráficos interativos (distribuição por categoria de evento, solicitações por status).
   - **Responsividade Grid**: Reorganizar o layout usando CSS Grid/Flexbox adaptável para telas móveis (<600px), tablets (600px-900px) e desktops (>900px).

2. **Dashboard de Relatórios e Estrutura (`Reports.tsx`)**:
   - **Cards Resumo**: Apresentar indicadores de membros leigos vs líderes, distribuição por comunidade/igreja e volume de atividades.
   - **Micro-interações**: Adicionar animações suaves de hover, transições de estado e estados de carregamento elegantes com Skeletons.

3. **Tela de Calendário (`Calendar.tsx`)**:
   - **Controles & Filtros**: Ajustar controles de navegação de data, seletores de modo (Mês, Semana, Dia, Lista) e filtros por igreja/pastoral com visualização moderna e responsiva.
   - **Adaptação Mobile**: Garantir renderização e toque responsivo nos blocos de eventos e modais de detalhes em telas pequenas.

4. **Diretrizes de Design e Acessibilidade (A11y)**:
   - Paleta de cores harmoniosa integrada ao MUI Theme da aplicação, com contraste auditado (WCAG AA).
   - Tipografia clara e hierarquizada (Google Fonts / Inter / Roboto).
   - Ausência de valores fixos arbitrários em pixels, utilizando espaçamentos flexíveis do sistema de design.

## Non-Functional Requirements
- **Desempenho**: Renderização fluida a 60fps nas animações e transições.
- **Zero Regressão**: Manter 100% dos testes frontend (`npm test` e `npm run build`) passando.

## Acceptance Criteria
- O build de produção (`npm run build`) e os testes unitários (`npm test`) finalizam com 100% de sucesso.
- O Dashboard Principal, Relatórios e Calendário apresentam visualização limpa e responsiva em breakpoints mobile, tablet e desktop.
- Os cards estatísticos, gráficos e visão de calendário exibem métricas reais com animações e estado sem quebra de layout.
