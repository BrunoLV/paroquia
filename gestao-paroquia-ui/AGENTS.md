# AGENTS.md - Frontend Engineering Standards & Guidelines

Este documento unifica as diretrizes de arquitetura, padrões de UI/UX e comandos de ferramentas para atuar no frontend do "Projeto Paróquia".

## 1. Arquitetura e Contexto do Projeto
- **Escopo e Stack:** Este diretório (`gestao-paroquia-ui/`) contém o frontend em React 19, TypeScript e Vite 5[cite: 6]. A aplicação consome a API REST do backend via Axios e utiliza TanStack React Query (v5) para cache de estado do servidor, com Material UI (MUI 9) como sistema de design principal[cite: 5, 6].
- **Domain-Driven UI:** A aplicação é orientada a páginas (page-oriented)[cite: 6]. A lógica de negócio, transformações pesadas e validações devem ser isoladas em custom hooks (`src/hooks/`), mantendo os componentes de UI focados estritamente na apresentação[cite: 5].
- **Estrutura e Topologia:** A co-localização é obrigatória. Testes e estilos devem ficar próximos ao componente pai[cite: 5]. A estrutura inclui: `src/api/` (wrappers de requisições pareados com os contextos do backend), `src/auth/` (regras centralizadas de permissões), `src/pages/`, `src/components/` (divididos por feature, não um balde plano), `src/context/` e `src/utils/`[cite: 5, 6].

## 2. Regras de Negócio e Integração com a API
- **Pareamento de Backend:** A superfície de API (`src/api/`) espelha os bounded contexts do backend. Se um contrato no backend mudar, o respectivo módulo e os tipos em `types.ts` devem ser atualizados simultaneamente[cite: 6].
- **Permissões Centralizadas:** Verificações de permissão (ex: `canManageProjects()`) pertencem ao `src/auth/permissions.ts` e nunca devem ser espalhadas inline pelos componentes[cite: 6].
- **Fluxo de Aprovação:** Muitas operações de mutação (criar/atualizar evento) não são aplicadas imediatamente, passando por uma máquina de estados de aprovação refletida em `src/api/approvals.ts` e na página de `Approvals`[cite: 6].

## 3. Governança de IA e LGPD
- **Graphify Obrigatório:** Consulte sempre os artefatos em `../graphify-out/` (na raiz do monorepo) antes de buscar cegamente por componentes ou páginas[cite: 5, 6].
- **Privacidade de Dados:** A aplicação manipula PII (Personal Identifiable Information). Leia e aplique rigorosamente `docs/ia/regras_duras_lgpd.md` antes de autorar componentes, logs (limpe `console.log` antes de commitar) ou persistência local[cite: 5].
- **Habilidades (Skills):** Consulte o guia `react-frontend.md` na pasta `.agents/skills/` antes de gerar refatorações[cite: 5].

## 4. Padrões de Código e Tipagem
- **Tipagem Estrita e Imutável:** É estritamente proibido o uso de `any`[cite: 5]. Prefira uniões de literais estritos (ex: `'success' | 'error'`) ou `enum` ao invés de `string` livre[cite: 5].
- **Limites Físicos:** Componentes React devem conter entre 10 e 60 linhas[cite: 5]. Funções puras e custom hooks entre 4 e 20 linhas[cite: 5]. Divida a lógica se ultrapassar esses limites.
- **Nomenclatura (Domínio em PT / Técnico em EN):** Tipos, interfaces e DTOs que representam o negócio devem ser em Português (ex: `VinculoResponse`). Termos técnicos (`Props`, `Context`) ficam em Inglês[cite: 5]. O uso de nomes genéricos (`Manager`, `Helper`) é proibido[cite: 5].
- **Importações:** Use sempre caminhos absolutos (`@/...`) e agrupe imports logicamente (Bibliotecas -> Imports absolutos -> Estilos/Tipos)[cite: 5, 6]. 

## 5. Qualidade, Documentação e Testes (TDD)
- **Zero Avisos:** O código não deve gerar avisos de linting (ESLint), formatação (Prettier) ou falhas na compilação do TypeScript (`tsc`)[cite: 5, 6].
- **Documentação de Código (JSDoc):** Hooks envolvendo requisições (TanStack Query) devem ter JSDoc explicando o contrato de rede e invalidação. Documente propriedades de DTO não óbvias (ex: motivos de conflito) e explique o PORQUÊ das lógicas nos comentários, garantindo legibilidade autônoma para IA[cite: 5].
- **Mandato de TDD:** O ciclo Red-Green-Refactor é exigido. Desenvolva testes Unitários, de Integração/Componente (usando RTL e `user-event`) e de Ponta a Ponta (E2E)[cite: 5].
- **Isolamento de Testes:** É proibido realizar chamadas de rede reais nos testes do frontend; todos os acessos via Axios devem ser mockados[cite: 5, 6].

## 6. Instruções de Ferramentas (Tools) e Comandos
### Exploração via Graphify (A partir da raiz do monorepo)
- `graphify query "<question>"`: Para dúvidas na base de código[cite: 6].
- `graphify path "<A>" "<B>"`: Rastrear conexões[cite: 6].
- `graphify explain "<concept>"`: Explorar componentes/conceitos[cite: 6].
- `graphify update .`: Atualizar a AST após modificar código[cite: 6].

### Comandos NPM (A partir de gestao-paroquia-ui/)
- `npm install`: Instalar dependências[cite: 6].
- `npm run dev` ou `npm start`: Iniciar o Vite (porta 3000, com proxy configurado para o backend via `VITE_API_PROXY_TARGET`)[cite: 6].
- `npm run build`: Checagem rigorosa de tipos (`tsc`) e build de produção[cite: 6].
- `npm run preview`: Servir o build de produção localmente[cite: 6].
- `npm test`: Executar testes no Vitest (com jsdom configurado)[cite: 6].
- `npm run test:watch`: Vitest em modo watch[cite: 6].
- `npx vitest run path/to/File.test.tsx`: Executar um arquivo de teste isolado[cite: 6].