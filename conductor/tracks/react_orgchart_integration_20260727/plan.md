# Implementation Plan: Integração do `@dabeng/react-orgchart` no Sistema de Organograma

## Phase 1: Dependências & Preparação do Ambiente
- [ ] Task: Instalar dependência `@dabeng/react-orgchart` no pacote `gestao-paroquia-ui`
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Setup' (Protocol in workflow.md)

## Phase 2: Refatoração do Hook de Exportação & Testes
- [ ] Task: Atualizar `useOrganogramaExport.ts` para redefinir matrizes de transformação de zoom durante a captura de tela (PNG/SVG)
- [ ] Task: Atualizar testes em `useOrganogramaExport.test.ts` garantindo zero regressões
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Export Logic' (Protocol in workflow.md)

## Phase 3: Integração do Componente & Limpeza de Código Legado
- [ ] Task: Integrar `@dabeng/react-orgchart` em `Organograma.tsx` com `NodeTemplate` customizado e suporte a pan/zoom
- [ ] Task: Remover código legado (`treeStyles`, `renderTreeNode`) e atualizar a suíte de testes `Organograma.test.tsx`
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Component Integration' (Protocol in workflow.md)
