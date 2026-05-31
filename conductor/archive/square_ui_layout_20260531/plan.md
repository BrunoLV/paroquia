# Plan: Square UI Layout & Component Standardization

## Phase 1: Configuração do Tema Global (MUI Theme)
- [x] Task: Atualizar os testes unitários do tema em `theme.test.tsx` para assegurar border-radius de `0` e sem arredondamentos [1a5774a]
- [x] Task: Modificar o tema global em `theme.ts` definindo `shape.borderRadius` como `0` e atualizando os componentes overrides para usar `0px` [1a5774a]
- [x] Task: Conductor - User Manual Verification 'Phase 1: Configuração do Tema Global' (Protocol in workflow.md)

## Phase 2: Ajuste de Componentes com Bordas Arredondadas Hardcoded
- [x] Task: Remover custom styles/border-radius hardcoded no modal de eventos (`EventRegistrationModal.tsx`) [a604896]
- [x] Task: Identificar e remover qualquer outro `borderRadius` customizado ou hardcoded nas demais páginas/componentes (ex: `Projects.tsx`, `Calendar.tsx`) [3867ba4]
- [x] Task: Conductor - User Manual Verification 'Phase 2: Ajuste de Componentes com Bordas Arredondadas Hardcoded' (Protocol in workflow.md)

## Phase 3: Verificação de Qualidade e Testes de Integração
- [x] Task: Executar e validar todo o conjunto de testes unitários/comportamento da UI
- [x] Task: Conductor - User Manual Verification 'Phase 3: Verificação de Qualidade e Testes de Integração' (Protocol in workflow.md)
