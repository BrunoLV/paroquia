# Plan: Exportar Organograma para Imagem

## Phase 1: Setup e Infraestrutura

- [x] Task: Instalar e configurar a biblioteca de captura de DOM (`html-to-image`) 3737a64
    - [x] Adicionar `html-to-image` ao `package.json` do projeto `gestao-paroquia-ui`
    - [x] Verificar compatibilidade com a versão atual do React e TypeScript
    - [x] Validar que a biblioteca exporta corretamente tipos TypeScript
- [x] Task: Criar hook `useOrganogramaExport` com estrutura inicial 507218e
    - [x] Criar arquivo `src/hooks/useOrganogramaExport.ts` com assinatura do hook
    - [x] Definir tipos TypeScript: `ExportFormat = 'png' | 'svg'` e `ExportState`
    - [x] Definir a interface do hook: `exportOrganograma(ref, format, filename)` e estado `isExporting`
- [x] Task: Conductor - User Manual Verification 'Phase 1: Setup e Infraestrutura' (Protocol in workflow.md) ed7b786

## Phase 2: Implementação do Hook de Exportação (TDD)

- [x] Task: Escrever testes unitários para o hook `useOrganogramaExport` (Red Phase) 1a7b171
    - [x] Criar `src/hooks/__tests__/useOrganogramaExport.test.ts`
    - [x] Escrever teste: deve iniciar com `isExporting = false`
    - [x] Escrever teste: deve definir `isExporting = true` durante a exportação
    - [x] Escrever teste: deve chamar `html-to-image` com o formato PNG correto
    - [x] Escrever teste: deve chamar `html-to-image` com o formato SVG correto
    - [x] Escrever teste: deve gerar nome de arquivo no formato `organograma_<nome>_<data>.<ext>`
    - [x] Escrever teste: deve restaurar `isExporting = false` após completar
    - [x] Escrever teste: deve tratar erro de captura e restaurar `isExporting = false`
    - [x] Confirmar que todos os testes falham (Red Phase)
- [x] Task: Implementar lógica do hook `useOrganogramaExport` (Green Phase) 2035ecc
    - [x] Implementar função `exportToPng(ref, filename)` usando `html-to-image`
    - [x] Implementar função `exportToSvg(ref, filename)` usando `html-to-image`
    - [x] Implementar lógica de download automático via `<a>` element
    - [x] Implementar controle de estado `isExporting`
    - [x] Implementar geração automática do nome do arquivo com data e nome da organização
    - [x] Confirmar que todos os testes passam (Green Phase)
- [x] Task: Conductor - User Manual Verification 'Phase 2: Hook de Exportação' (Protocol in workflow.md) 95b04d9

## Phase 3: Componente de UI do Botão de Exportação (TDD)

- [x] Task: Escrever testes para o componente `OrganogramaExportButton` (Red Phase) 5935b22
    - [x] Criar `src/components/organograma/__tests__/OrganogramaExportButton.test.tsx`
    - [x] Escrever teste: deve renderizar o botão de exportação
    - [x] Escrever teste: deve abrir menu de opções ao clicar no botão
    - [x] Escrever teste: deve exibir opções PNG e SVG no menu
    - [x] Escrever teste: deve chamar `onExport('png')` ao selecionar PNG
    - [x] Escrever teste: deve chamar `onExport('svg')` ao selecionar SVG
    - [x] Escrever teste: deve exibir indicador de loading quando `isExporting = true`
    - [x] Escrever teste: deve desabilitar o botão enquanto `isExporting = true`
    - [x] Confirmar que todos os testes falham (Red Phase)
- [x] Task: Implementar componente `OrganogramaExportButton` (Green Phase) eb3b049
    - [x] Criar `src/components/organograma/OrganogramaExportButton.tsx`
    - [x] Implementar botão primário MUI com menu dropdown (PNG / SVG)
    - [x] Integrar prop `isExporting` para exibir `CircularProgress` e desabilitar botão
    - [x] Aplicar design system do projeto (Marian Blue/Gold palette)
    - [x] Confirmar que todos os testes passam (Green Phase)
- [x] Task: Conductor - User Manual Verification 'Phase 3: Componente de UI' (Protocol in workflow.md) 82d03eb

## Phase 4: Integração na Página do Organograma

- [x] Task: Integrar o botão e hook na página/componente do Organograma 085b12f
    - [x] Localizar o componente de página do Organograma em `gestao-paroquia-ui`
    - [x] Adicionar `ref` ao container do gráfico do organograma para captura
    - [x] Instanciar o hook `useOrganogramaExport` no componente
    - [x] Renderizar `OrganogramaExportButton` acima do gráfico, conectado ao hook
    - [x] Passar o nome da organização para a geração do nome do arquivo
- [x] Task: Testes de integração e validação manual
    - [x] Executar suíte completa de testes: `CI=true npm test`
    - [x] Verificar cobertura de testes > 80% para os novos arquivos
    - [x] Validar manualmente o download PNG em Chrome e Firefox
    - [x] Validar manualmente o download SVG em Chrome e Firefox
    - [x] Verificar que o nome do arquivo segue o padrão correto
    - [x] Verificar que o loading é exibido e desaparece após o download
- [x] Task: Conductor - User Manual Verification 'Phase 4: Integração na Página' (Protocol in workflow.md) d396f50
