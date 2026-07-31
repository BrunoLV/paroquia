# Spec: Modernização de Gráficos e Visualizações (Reports & Dashboard)

## Overview
Refatorar a camada de visualização de dados de `gestao-paroquia-ui`, substituindo `@mui/x-charts` por bibliotecas especializadas em dois pontos específicos do sistema, mantendo o restante da arquitetura de widgets (MUI puro) intocado. É um refactor técnico de biblioteca de renderização — o comportamento de negócio e os dados exibidos não mudam, mas a interatividade (zoom/scroll, exportação nativa, agrupamento visual) é ampliada.

## Escopo

### 1. Gráfico de Barras — `Reports.tsx` (aba "Indicadores Operacionais")
- Substituir o `BarChart` de `@mui/x-charts` (linha ~481) por **ApexCharts** via `react-apexcharts`.
- Zoom e rolagem horizontal nativos habilitados para acomodar muitas pastorais/movimentos sem achatar as barras.
- Menu nativo do ApexCharts (canto superior direito do card) habilitado para exportação em PNG, SVG e CSV.
- Contêiner responsivo à largura do card pai (usar `width: '100%'` + redraw automático do ApexCharts).

### 2. Gráfico de Rosca — Componente unificado `EventDistributionPieChart.tsx`
- Substituir o `PieChart` de `@mui/x-charts` por **Recharts** (`recharts`).
- **Unificação:** extrair a lógica de renderização hoje duplicada entre `EventDistributionPieChart.tsx` (usado em `Dashboard.tsx`) e o `PieChart` inline de `Reports.tsx` (linha ~431) em um único componente `EventDistributionPieChart.tsx`, reutilizado nos dois locais.
- Labels externos com linha-guia (*label lines*) para evitar sobreposição em fatias pequenas.
- Agrupamento dinâmico: organizações com menos de 3% de representatividade no total são agregadas em uma fatia "Outras". Se nenhuma organização estiver abaixo do limiar, a fatia "Outras" não é exibida (sem fatia vazia forçada).
- Animação de entrada suavizada (*ease-out*) ao montar.
- Estado vazio (sem dados) mantém a mensagem textual já existente em ambos os locais.

## Componentes Preservados (fora de escopo)
- `Organograma.tsx` / `@dabeng/react-orgchart` — inalterado.
- `WeeklyTimeline.tsx`, `ActiveProjectsProgressList.tsx`, `MetricasEstrategicasWidget.tsx` — proibido introduzir bibliotecas de gráficos; continuam usando `@mui/lab/Timeline` e `<LinearProgress />`.

## Integração com Tema MUI
Ambas as novas bibliotecas devem consumir `useTheme()` do MUI dinamicamente (sem cores hardcoded):
| Elemento | Propriedade do tema |
|---|---|
| Cor principal | `theme.palette.primary.main` |
| Cor secundária | `theme.palette.secondary.main` |
| Cores de apoio (fatias extras / séries adicionais) | `theme.palette.info.main`, `warning.main`, `success.main` |
| Texto / eixos | `theme.palette.text.secondary` |
| Grid / divisórias | `theme.palette.divider` |

Dark mode: ambos os gráficos devem reagir a `theme.palette.mode`.
- ApexCharts: propriedade `theme.mode` do chart config alternando `'light'`/`'dark'`.
- Recharts: tooltip e fundo de cards flutuantes usando fundo semitransparente derivado de `theme.palette.background.paper`.

## Dependências
Adicionar ao `gestao-paroquia-ui/package.json`: `react-apexcharts`, `apexcharts`, `recharts`. Documentar em `tech-stack.md` antes da implementação (regra do workflow.md).
`@mui/x-charts` permanece como dependência apenas se outro ponto do sistema ainda o utilizar após esta migração (verificar antes de remover do `package.json`).

## Testes
- Atualizar `setupTests.ts`: substituir/estender o mock atual de `@mui/x-charts` por mocks equivalentes para `react-apexcharts` e `recharts`, seguindo o padrão TDD do `workflow.md` (testes primeiro, depois implementação).
- Atualizar os testes afetados: `EventDistributionPieChart.test.tsx` (se existir, senão criar), `Dashboard.test.tsx`, `Reports.test.tsx`.
- Cobertura de teste para: regra de agrupamento "Outras" (limiar de 3%), estado vazio, e renderização com tema dark/light.

## Critérios de Aceitação
1. `Reports.tsx` renderiza o gráfico de barras via ApexCharts com zoom/scroll horizontal funcional e menu de exportação (PNG/SVG/CSV) visível.
2. O componente `EventDistributionPieChart.tsx` (Recharts) é usado tanto em `Dashboard.tsx` quanto em `Reports.tsx`, sem duplicação de lógica.
3. Organizações abaixo de 3% de representatividade aparecem agrupadas em "Outras"; quando não aplicável, a fatia não aparece.
4. Ambos os gráficos respeitam as cores do tema MUI (claro/escuro) sem cores fixas em hex.
5. `npm run build` (typecheck) e `npm test` passam sem regressões.
6. Nenhuma biblioteca de gráficos é introduzida em `WeeklyTimeline.tsx`, `ActiveProjectsProgressList.tsx` ou `MetricasEstrategicasWidget.tsx`.
7. Responsividade validada em resoluções mobile/tablet (breakpoints existentes do MUI Grid).

## Fora de Escopo
- Migração do Organograma para React Flow (fica pré-aprovada para o futuro, não faz parte deste track).
- Qualquer alteração em `MetricasEstrategicasTab.tsx`/`MetricasEstrategicasWidget.tsx` além de garantir que nenhuma lib de gráficos seja adicionada ali.
- Mudanças de contrato de API/backend — dados consumidos permanecem os mesmos (`DashboardMetrics`, `EventDistribution`).
