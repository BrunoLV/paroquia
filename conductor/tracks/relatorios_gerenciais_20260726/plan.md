# Plan: Implementar Tela de Relatórios Gerenciais

## Phase 1: Utilitários de Exportação (CSV e PDF)

- [x] Task 1.1: Criar testes unitários em `exportUtils.test.ts` para funções de exportação CSV e PDF (09aff59)
- [x] Task 1.2: Implementar funções utilitárias em `exportUtils.ts` (geração de CSV e disparo de download de PDF) (09aff59)

## Phase 2: Componentes Modulares de Relatório

- [x] Task 2.1: Criar testes unitários para os componentes `ReportFilters.test.tsx` e `ReportExportButtons.test.tsx` (2c92c5c)
- [x] Task 2.2: Implementar `ReportFilters.tsx` e `ReportExportButtons.tsx` com design system Mariano (0px border-radius) (2c92c5c)

## Phase 3: Refatoração e Enriquecimento da Página de Relatórios (`Reports.tsx`)

- [x] Task 3.1: Atualizar testes em `Reports.test.tsx` para validar integração com `getDashboardMetrics`, gráficos e exportação (68391af)
- [x] Task 3.2: Refatorar `Reports.tsx` com React Query, KPI Cards, gráficos interativos (`@mui/x-charts`) e integração de filtros/exportação (68391af)

## Phase 4: Navegação, Verificação de Cobertura e Checkpoint

- [~] Task 4.1: Verificar e ajustar integração de rota/menu de navegação se necessário e rodar testes de integração
- [ ] Task 4.2: Executar validação final da suíte de testes (`CI=true npm test`) e registrar checkpoint
