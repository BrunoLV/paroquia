# Specification: Migração do Frontend para Vite & Vitest

## Overview
Substituir o ecossistema legado e descontinuado do `react-scripts` (Create React App) pelo **Vite** (bundler e dev-server de alta performance) e **Vitest** (test runner nativo e ultrarrápido) no subprojeto frontend `gestao-paroquia-ui`.

## Scope & Architectural Changes
1. **Remoção de Dependências Legadas:**
   - Remover `react-scripts` das dependências do `package.json`.
   - Adicionar `vite`, `@vitejs/plugin-react`, `vitest`, `@vitest/coverage-v8`, `@testing-library/jest-dom` e `jsdom`.

2. **Reorganização de Estrutura & Configurações:**
   - Mover `public/index.html` para a raiz do frontend (`gestao-paroquia-ui/index.html`).
   - Atualizar `index.html` para remover `%PUBLIC_URL%` e injetar a tag `<script type="module" src="/src/index.tsx"></script>`.
   - Criar `vite.config.ts` com o plugin React, aliasing `@` -> `./src`, servidor dev na porta 5173 e proxy da API `/api` direcionado ao backend Spring Boot em `http://localhost:8080`.
   - Configurar `vitest` em `vite.config.ts` para ambiente `jsdom` e setup file `src/setupTests.ts`.

3. **Padronização de Variáveis de Ambiente:**
   - Migrar todas as referências no código frontend de `process.env.REACT_APP_*` para `import.meta.env.VITE_*`.
   - Atualizar arquivos de ambiente `.env` para usar o prefixo `VITE_`.

4. **Scripts do Package.json:**
   - `npm run dev`: Inicia o Vite dev server (`vite`).
   - `npm run build`: Executa checagem de tipos e compilação de produção (`tsc && vite build`).
   - `npm run preview`: Visualiza o build de produção (`vite preview`).
   - `npm test`: Executa testes unitários com Vitest (`vitest run`).

## Acceptance Criteria
- [ ] O comando `npm run build` compila o bundle de produção sem erros de TypeScript ou bundling.
- [ ] O comando `npm run dev` inicia o servidor de desenvolvimento instantaneamente em `http://localhost:5173`.
- [ ] O comando `npm test` executa todas as 37 suítes de teste (243 testes) via Vitest com 100% de aprovação.
- [ ] A aplicação conecta normalmente ao backend Spring Boot através do proxy configurado.
