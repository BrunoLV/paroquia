# Specification: Integração do `@dabeng/react-orgchart` no Sistema de Organograma

## Overview
Substituir a renderização estática baseada em listas nativas (`<ul>`/`<li>`) e pseudo-elementos CSS em `Organograma.tsx` pela biblioteca `@dabeng/react-orgchart`. A nova implementação manterá a identidade visual (Mariana Fátima tokens, cores por função/tipo, expansão de membros com `Collapse`), adicionará suporte a Pan/Zoom fluído no modo de árvore, preservará o modo em grade, e atualizará a exportação em PNG/SVG no `useOrganogramaExport.ts`.

## Functional Requirements
1. **Instalação de Dependência:** Adicionar `@dabeng/react-orgchart` ao `gestao-paroquia-ui/package.json`.
2. **Template Customizado de Nó (`NodeTemplate`):** Implementar um template de nó customizado em `Organograma.tsx` preservando os estilos de cada `subtype` (Clero, Matriz, Conselho, Pastoral), crachás de função, avatares e menu retrátil de membros adicionais.
3. **Modo Árvore (`viewMode === 'tree'`):** Substituir `treeStyles` e `renderTreeNode()` pelo `<ChartContainer>` da biblioteca com suporte a pan & zoom.
4. **Modo Grade (`viewMode === 'grid'`):** Manter inalterado o modo de exibição em grade por categorias com o sistema de `Grid` do MUI 9.
5. **Ajuste na Exportação (`useOrganogramaExport.ts`):** Atualizar o hook de captura `html-to-image` para resetar temporariamente a matriz de transformação (`transform: matrix(...)`) e ajustar o contêiner para a dimensão real do gráfico (`scrollWidth`/`scrollHeight`) antes de gerar a imagem em HD (2x), restaurando o estado visual após o download.
6. **Limpeza de Código Legado:** Remover as regras obsoletas `treeStyles` e métodos recursivos manuais.

## Acceptance Criteria
- Todos os testes unitários e de renderização (`Organograma.test.tsx`, `useOrganogramaExport.test.ts`, `OrganogramaExportButton.test.tsx`) continuam passando 100%.
- A árvore renderiza corretamente todas as hierarquias paroquiais com suporte a arrasto (pan) e zoom.
- A exportação PNG/SVG gera imagens completas e sem cortes.
