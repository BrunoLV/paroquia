# Specification: Padronização do Código e Documentação segundo os Padrões AGENTS.md

## Overview
Esta especificação define o alinhamento de todos os subprojetos do monorepo (Backend Java/Spring Boot, Frontend React/TypeScript e Documentação Conductor) com as diretrizes de engenharia estabelecidas em `AGENTS.md`. A track contempla a uniformização do idioma de todos os comentários, docstrings (Javadoc/JSDoc) e documentações para Português (pt-BR), a eliminação de imports inline (FQN), a substituição de tipos genéricos ou unions livres de String por `enum`, a adição de justificativas explicativas (WHY) em métodos públicos e a remoção de código morto ou logs locais.

## Functional & Technical Requirements

1. **Uniformização do Idioma (pt-BR)**:
   - Todos os comentários de código, Javadoc no backend Java, JSDoc no frontend React/TypeScript e documentações do ecossistema Conductor devem ser padronizados em Português (pt-BR).

2. **Conformidade do Backend (Java / Spring Boot)**:
   - Auditar e remover quaisquer imports inline (FQN no meio do código), movendo-os obrigatoriamente para a seção de imports no topo do arquivo.
   - Assegurar que exceções lancem mensagens descritivas explicitando o valor ofensivo recebido e o formato/faixa esperada.
   - Adicionar Javadoc em pt-BR em classes e métodos públicos explicando a intenção/motivo (*WHY*) e exemplo de uso.
   - Garantir que funções tenham de 4 a 20 linhas e arquivos permaneçam abaixo de 500 linhas.

3. **Conformidade do Frontend (React + TypeScript)**:
   - Garantir o uso de path mappings absolutos (`@/...`) em substituição a caminhos relativos profundos.
   - Eliminar qualquer uso de `any` ou tipos implícitos.
   - Adicionar JSDoc em pt-BR nas funções utilitárias e hooks customizados.
   - Garantir higienização da console (remover `console.log` residuais de depuração local).

4. **Documentação Conductor**:
   - Revisar os documentos centrais do Conductor (`product.md`, `tech-stack.md`, `workflow.md`, `product-guidelines.md`) assegurando clareza e conformidade no idioma pt-BR.

## Non-Functional Requirements
- **Manutenibilidade & Limpeza**: Código legível, fortemente tipado e sem comentários redundantes ("WHAT").
- **Zero Regressão**: Manter 100% dos testes unitários e de integração do backend e frontend passando.

## Acceptance Criteria
- Todos os testes do backend (`./gradlew test`) e do frontend (`npm test` e `npm run build`) continuam passando com 100% de sucesso.
- Ausência de imports FQN inline no backend e ausência de tipos `any` ou caminhos relativos profundos desnecessários no frontend.
- Todos os comentários e docstrings revisados estritamente em português (pt-BR).
