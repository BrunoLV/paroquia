# Plan: Exportar Organograma para Imagem

## Phase 1: Setup e Infraestrutura

- [ ] Task: Instalar e configurar a biblioteca de captura de DOM (`html-to-image`)
    - [ ] Adicionar `html-to-image` ao `package.json` do projeto `gestao-paroquia-ui`
    - [ ] Verificar compatibilidade com a versão atual do React e TypeScript
    - [ ] Validar que a biblioteca exporta corretamente tipos TypeScript
- [ ] Task: Criar hook `useOrganogramaExport` com estrutura inicial
    - [ ] Criar arquivo `src/hooks/useOrganogramaExport.ts` com assinatura do hook
    - [ ] Definir tipos TypeScript: `ExportFormat = 'png' | 'svg'` e `ExportState`
    - [ ] Definir a interface do hook: `exportOrganograma(ref, format, filename)` e estado `isExporting`
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Setup e Infraestrutura' (Protocol in workflow.md)

## Phase 2: Implementação do Hook de Exportação (TDD)

- [ ] Task: Escrever testes unitários para o hook `useOrganogramaExport` (Red Phase)
    - [ ] Criar `src/hooks/__tests__/useOrganogramaExport.test.ts`
    - [ ] Escrever teste: deve iniciar com `isExporting = false`
    - [ ] Escrever teste: deve definir `isExporting = true` durante a exportação
    - [ ] Escrever teste: deve chamar `html-to-image` com o formato PNG correto
    - [ ] Escrever teste: deve chamar `html-to-image` com o formato SVG correto
    - [ ] Escrever teste: deve gerar nome de arquivo no formato `organograma_<nome>_<data>.<ext>`
    - [ ] Escrever teste: deve restaurar `isExporting = false` após completar
    - [ ] Escrever teste: deve tratar erro de captura e restaurar `isExporting = false`
    - [ ] Confirmar que todos os testes falham (Red Phase)
- [ ] Task: Implementar lógica do hook `useOrganogramaExport` (Green Phase)
    - [ ] Implementar função `exportToPng(ref, filename)` usando `html-to-image`
    - [ ] Implementar função `exportToSvg(ref, filename)` usando `html-to-image`
    - [ ] Implementar lógica de download automático via `<a>` element
    - [ ] Implementar controle de estado `isExporting`
    - [ ] Implementar geração automática do nome do arquivo com data e nome da organização
    - [ ] Confirmar que todos os testes passam (Green Phase)
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Hook de Exportação' (Protocol in workflow.md)

## Phase 3: Componente de UI do Botão de Exportação (TDD)

- [ ] Task: Escrever testes para o componente `OrganogramaExportButton` (Red Phase)
    - [ ] Criar `src/components/organograma/__tests__/OrganogramaExportButton.test.tsx`
    - [ ] Escrever teste: deve renderizar o botão de exportação
    - [ ] Escrever teste: deve abrir menu de opções ao clicar no botão
    - [ ] Escrever teste: deve exibir opções PNG e SVG no menu
    - [ ] Escrever teste: deve chamar `onExport('png')` ao selecionar PNG
    - [ ] Escrever teste: deve chamar `onExport('svg')` ao selecionar SVG
    - [ ] Escrever teste: deve exibir indicador de loading quando `isExporting = true`
    - [ ] Escrever teste: deve desabilitar o botão enquanto `isExporting = true`
    - [ ] Confirmar que todos os testes falham (Red Phase)
- [ ] Task: Implementar componente `OrganogramaExportButton` (Green Phase)
    - [ ] Criar `src/components/organograma/OrganogramaExportButton.tsx`
    - [ ] Implementar botão primário MUI com menu dropdown (PNG / SVG)
    - [ ] Integrar prop `isExporting` para exibir `CircularProgress` e desabilitar botão
    - [ ] Aplicar design system do projeto (Marian Blue/Gold palette)
    - [ ] Confirmar que todos os testes passam (Green Phase)
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Componente de UI' (Protocol in workflow.md)

## Phase 4: Integração na Página do Organograma

- [ ] Task: Integrar o botão e hook na página/componente do Organograma
    - [ ] Localizar o componente de página do Organograma em `gestao-paroquia-ui`
    - [ ] Adicionar `ref` ao container do gráfico do organograma para captura
    - [ ] Instanciar o hook `useOrganogramaExport` no componente
    - [ ] Renderizar `OrganogramaExportButton` acima do gráfico, conectado ao hook
    - [ ] Passar o nome da organização para a geração do nome do arquivo
- [ ] Task: Testes de integração e validação manual
    - [ ] Executar suíte completa de testes: `CI=true npm test`
    - [ ] Verificar cobertura de testes > 80% para os novos arquivos
    - [ ] Validar manualmente o download PNG em Chrome e Firefox
    - [ ] Validar manualmente o download SVG em Chrome e Firefox
    - [ ] Verificar que o nome do arquivo segue o padrão correto
    - [ ] Verificar que o loading é exibido e desaparece após o download
- [ ] Task: Conductor - User Manual Verification 'Phase 4: Integração na Página' (Protocol in workflow.md)
