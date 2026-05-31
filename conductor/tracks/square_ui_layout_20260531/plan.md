# Plan: Square UI Layout & Component Standardization

## Phase 1: Configuração do Tema Global (MUI Theme)
- [ ] Task: Atualizar os testes unitários do tema em `theme.test.tsx` para assegurar border-radius de `0` e sem arredondamentos
- [ ] Task: Modificar o tema global em `theme.ts` definindo `shape.borderRadius` como `0` e atualizando os componentes overrides para usar `0px`
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Configuração do Tema Global' (Protocol in workflow.md)

## Phase 2: Ajuste de Componentes com Bordas Arredondadas Hardcoded
- [ ] Task: Remover custom styles/border-radius hardcoded no modal de eventos (`EventRegistrationModal.tsx`)
- [ ] Task: Identificar e remover qualquer outro `borderRadius` customizado ou hardcoded nas demais páginas/componentes (ex: `Projects.tsx`, `Calendar.tsx`)
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Ajuste de Componentes com Bordas Arredondadas Hardcoded' (Protocol in workflow.md)

## Phase 3: Verificação de Qualidade e Testes de Integração
- [ ] Task: Executar e validar todo o conjunto de testes unitários/comportamento da UI
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Verificação de Qualidade e Testes de Integração' (Protocol in workflow.md)
