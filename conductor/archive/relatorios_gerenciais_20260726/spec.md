# Specification: Implementar Tela de Relatórios Gerenciais

## Overview
Reformular a página de Relatórios e Métricas (`/reports`) no frontend React (`@gestao-paroquia-ui`), fornecendo uma visão gerencial completa com métricas chave (KPIs), gráficos interativos (via `@mui/x-charts`), suporte a filtros, e opções de exportação de relatórios nos formatos CSV e PDF.

## Context & Data Source
- **API Endpoint:** `GET /api/v1/dashboard/metrics` que expõe `DashboardMetrics` (`resumoGeral`, `eventosPorOrganizacao`, `projetosAtivosDetalhes`, `eventosSemana`).
- **Navegação:** Garantir que o item de menu "Relatórios & Métricas" esteja integrado de forma consistente no Sidebar/Layout.

## Key Features & User Stories
1. **KPI Cards & Métricas Resumidas:**
   - Cards com contadores principais: Total de Eventos (Mês), Projetos Ativos, Solas/Aprovações Pendentes.
   - Estilização no padrão Mariano (border-radius 0px, gradientes suaves e bordas limpas).
2. **Gráficos Interativos:**
   - Gráfico de Pizza (`PieChart` da `@mui/x-charts`): Distribuição de Eventos por Organização.
   - Gráfico/Progresso de Projetos Ativos (`LinearProgress` / `@mui/x-charts`): Acompanhamento percentual do tempo decorrido de cada projeto.
3. **Filtros e Controles:**
   - Barra de ferramentas superior com seletor de período/datas e busca rápida.
4. **Exportação Multi-formato (CSV & PDF):**
   - **Exportar CSV:** Geração client-side de relatório em formato `.csv` estruturado com o resumo das métricas e projetos.
   - **Exportar PDF:** Captura e download do relatório em PDF bem formatado.

## Technical Architecture & Components
- `src/pages/Reports.tsx`: Componente principal refatorado para usar `useQuery` (`getDashboardMetrics`).
- `src/components/reports/ReportFilters.tsx`: Barra de filtros por data/período.
- `src/components/reports/ReportExportButtons.tsx`: Ações de download CSV e PDF.
- `src/utils/exportUtils.ts`: Funções utilitárias puras para conversão de dados em CSV e geração de PDF.
