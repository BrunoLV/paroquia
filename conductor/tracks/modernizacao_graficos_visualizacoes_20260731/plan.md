# Plan: Modernização de Gráficos e Visualizações (Reports & Dashboard)

## Fase 1 — Preparação e Documentação da Stack

- [x] Task: Documentar mudança de stack em `tech-stack.md` (0b4a305)
  - [x] Adicionar seção descrevendo a introdução de `react-apexcharts`/`apexcharts` (gráfico de barras) e `recharts` (gráfico de rosca), com nota datada explicando a substituição parcial de `@mui/x-charts`
- [x] Task: Instalar dependências (64e9190)
  - [x] Rodar `npm install react-apexcharts apexcharts recharts` em `gestao-paroquia-ui/`
  - [x] Verificar `package.json`/`package-lock.json` atualizados
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Fase 2 — Componente Unificado de Rosca (Recharts)

- [x] Task: Escrever testes (Red) para o novo `EventDistributionPieChart.tsx` (862ead8)
  - [x] Teste: renderiza fatias a partir de `EventDistribution[]`
  - [x] Teste: agrupa organizações com <3% de representatividade em fatia "Outras"
  - [x] Teste: não exibe "Outras" quando nenhuma organização está abaixo do limiar
  - [x] Teste: exibe mensagem de estado vazio quando não há dados
  - [x] Rodar `npx vitest run EventDistributionPieChart.test.tsx` e confirmar falha (Red)
- [x] Task: Atualizar mock global em `setupTests.ts` (dbfc367)
  - [x] Substituir/estender mock de `@mui/x-charts` por mock de `recharts` (PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer)
- [x] Task: Implementar `EventDistributionPieChart.tsx` com Recharts (Green) (9f5ab48)
  - [x] Extrair função utilitária de agrupamento "Outras" (limiar 3%)
  - [x] Labels externos com linha-guia (customized label/labelLine do Recharts)
  - [x] Animação de entrada ease-out (`isAnimationActive`, `animationEasing="ease-out"`)
  - [x] Cores dinâmicas via `useTheme()` (primary/secondary/info/warning/success)
  - [x] Suporte a dark mode no tooltip/legenda (fundo semitransparente de `background.paper`)
  - [x] Rodar testes e confirmar passagem (Green)
- [x] Task: Substituir uso em `Dashboard.tsx`
  - [x] Confirmar que `Dashboard.tsx` continua passando `data={metrics?.eventosPorOrganizacao || []}` sem mudança de contrato
  - [x] Rodar `Dashboard.test.tsx` e ajustar mocks/asserts se necessário
- [x] Task: Substituir `PieChart` inline em `Reports.tsx` (9ffff2f)
  - [x] Remover import de `PieChart` de `@mui/x-charts` em `Reports.tsx`
  - [x] Substituir bloco inline (linha ~429-456) pelo componente `EventDistributionPieChart`, adaptando `pieChartData`/`eventosPorOrganizacao` para a prop `data: EventDistribution[]`
  - [x] Rodar `Reports.test.tsx` e ajustar mocks/asserts se necessário
- [x] Task: Verificar cobertura de teste (>80% para código novo)
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Fase 3 — Gráfico de Barras em ApexCharts (`Reports.tsx`)

- [x] Task: Escrever testes (Red) para o novo bloco de barras (55e53b0)
  - [x] Teste: renderiza série de barras a partir de `eventosPorOrganizacao`
  - [x] Teste: exibe mensagem de estado vazio quando não há dados
  - [x] Teste: aplica cores do tema MUI e reage a `palette.mode`
  - [x] Rodar `npx vitest run EventVolumeBarChart.test.tsx` e confirmar falha (Red)
- [x] Task: Adicionar mock de `react-apexcharts` em `setupTests.ts` (a27963d)
  - [x] Mock do componente `ReactApexChart` retornando marcação simples testável (`data-testid="mock-apex-bar-chart"`)
- [x] Task: Implementar bloco de barras com ApexCharts (Green) (96e5873, d40fee5)
  - [x] Substituir import de `BarChart` de `@mui/x-charts` por `react-apexcharts` em `Reports.tsx`
  - [x] Configurar `options`/`series` a partir de `eventosPorOrganizacao` (`categories` = nomes das organizações)
  - [x] Habilitar zoom e rolagem horizontal (`chart.zoom.enabled`, `chart.type: 'bar'`, scroll via `xaxis`/`chart.toolbar`)
  - [x] Habilitar menu de exportação nativo (`chart.toolbar.show = true`, export PNG/SVG/CSV)
  - [x] Cores dinâmicas via `useTheme()` e `theme.mode` propagado para `options.theme.mode`
  - [x] Container responsivo (`width: '100%'`, `ResizeObserver`/redraw automático do ApexCharts)
  - [x] Rodar testes e confirmar passagem (Green)
- [x] Task: Verificar cobertura de teste (>80% para código novo)
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Fase 4 — Limpeza, Validação Cruzada e Responsividade

- [x] Task: Auditar uso remanescente de `@mui/x-charts` (681a65a)
  - [x] Buscar por outras referências no projeto (`grep -r "@mui/x-charts"`)
  - [x] Se não houver mais uso, remover a dependência do `package.json` e do mock em `setupTests.ts`; caso contrário, documentar por que foi mantida
- [x] Task: Confirmar que componentes preservados não foram tocados
  - [x] Revisar diffs de `WeeklyTimeline.tsx`, `ActiveProjectsProgressList.tsx`, `MetricasEstrategicasWidget.tsx`, `Organograma.tsx` — nenhuma nova lib de gráfico introduzida
- [x] Task: Rodar suíte completa e typecheck
  - [~] `npm run build` (tsc + vite build) sem erros — falha por 12 erros de TS **pré-existentes** e não relacionados a este track (`ConselhosAtas.tsx`, `Layout.tsx`, `MetricasEstrategicasTab.tsx`, `ConcorrenciaAlertaBanner.tsx`, testes usando `it` do vitest); confirmado via `git stash`/`npx tsc --noEmit` que os mesmos 12 erros já existiam antes do início do track. Nenhum erro novo introduzido pelas mudanças deste track.
  - [x] `CI=true npm test` sem regressões — 43 arquivos, 306 testes passando
- [ ] Task: Validar responsividade manual
  - [ ] Verificar `Reports.tsx` (abas de indicadores) em breakpoints mobile/tablet do MUI Grid
  - [ ] Verificar `Dashboard.tsx` com o novo componente de rosca em telas menores
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)
