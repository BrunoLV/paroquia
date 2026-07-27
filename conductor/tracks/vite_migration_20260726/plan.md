# Plan: Migração do Frontend para Vite & Vitest

## Phase 1: Instalação de Dependências e Configuração Base do Vite & Vitest

- [x] Task 1.1: Atualizar `package.json` removendo `react-scripts` e adicionando `vite`, `@vitejs/plugin-react`, `vitest`, `jsdom` e `@testing-library/jest-dom` (2d9aca2)
- [x] Task 1.2: Criar arquivo de configuração `vite.config.ts` com suporte a React, Vitest (`jsdom`), aliases de caminhos e proxy de API para `http://localhost:8080` (ed7b169)
- [x] Task 1.3: Mover e refatorar `index.html` para a raiz de `gestao-paroquia-ui/`, adicionando a tag de módulo JS `/src/index.tsx` e removendo `%PUBLIC_URL%` (c253cb8)
- [x] Task 1.4: Conductor - User Manual Verification 'Phase 1: Instalação de Dependências e Configuração Base' (Protocol in workflow.md)

## Phase 2: Migração de Variáveis de Ambiente e Configuração de Tipos

- [x] Task 2.1: Atualizar `src/react-app-env.d.ts` / `src/vite-env.d.ts` para tipagem do `import.meta.env` (bfc2103)
- [x] Task 2.2: Refatorar referências no código de `process.env.REACT_APP_*` para `import.meta.env.VITE_*` e atualizar arquivos de ambiente `.env` (0be7988)
- [ ] Task 2.3: Conductor - User Manual Verification 'Phase 2: Migração de Variáveis de Ambiente' (Protocol in workflow.md)

## Phase 3: Adaptação dos Testes para Vitest e Execução da Suíte

- [ ] Task 3.1: Ajustar `src/setupTests.ts` e Mocks Globais para compatibilidade total com o Vitest
- [ ] Task 3.2: Executar e validar a suíte de testes completa (`CI=true npm test`) para garantir 100% de passagem nos 243 testes
- [ ] Task 3.3: Conductor - User Manual Verification 'Phase 3: Adaptação dos Testes e Validação da Suíte' (Protocol in workflow.md)

## Phase 4: Validação do Build de Produção e Checkpoint Final

- [ ] Task 4.1: Executar build de produção (`npm run build`) e testar execução do preview (`npm run preview`)
- [ ] Task 4.2: Executar verificação final e criar o commit de checkpoint da track
