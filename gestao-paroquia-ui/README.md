# 🎨 Paróquia — Frontend (`gestao-paroquia-ui`)

Interface administrativa desenvolvida em **React 19** para o sistema de gestão administrativa e pastoral da Paróquia Nossa Senhora de Fátima. Cobre agendamento de eventos, aprovações, cadastro de membros, organograma, controle de patrimônio, importação/exportação de dados e um dashboard de métricas executivas — tudo consumindo a API REST do [backend](../gestao-paroquia).

---

## 🚀 Tecnologias e Ferramentas

- **Framework UI:** **React 19** (`react` / `react-dom`)
- **Linguagem:** **TypeScript 5**
- **Bundler & Dev Server:** **Vite 5** (Hot Module Replacement ultra-rápido e suporte nativo a ES Modules)
- **UI Kit & Estilização:** **Material UI (MUI 9)**, Emotion (`@emotion/react`, `@emotion/styled`)
- **Gráficos:** **ApexCharts** (`apexcharts` / `react-apexcharts`) e **Recharts**
- **Organograma:** `@dabeng/react-orgchart`
- **Gerenciamento de Estado de Servidor:** **TanStack React Query 5** (cache, re-fetching e sincronização)
- **Cliente HTTP:** **Axios** (configurado com `withCredentials: true`, interceptors de 401 e tratamento centralizado)
- **Roteamento:** **React Router DOM 6** (rotas protegidas e controle de acesso por permissões)
- **Testes Automatizados:** **Vitest 2** + **React Testing Library** + **jsdom**
- **Exportação & Mídia:** `html-to-image` (geração de imagens do organograma paroquial)

---

## 🛠️ Desenvolvimento e Execução

### Pré-requisitos

- **Node.js:** v18 ou superior
- **npm:** v9 ou superior

### Scripts disponíveis

No diretório `gestao-paroquia-ui`:

```bash
# Instalar dependências
npm install

# Iniciar o servidor de desenvolvimento Vite (porta 3000 com proxy /api)
npm run dev
# ou
npm start

# Executar a suíte de testes em modo de execução única (Vitest)
npm test

# Executar os testes em modo watch (desenvolvimento contínuo)
npm run test:watch

# Checar tipos (tsc) e gerar o build de produção otimizado
npm run build

# Executar servidor local de pré-visualização do build de produção
npm run preview
```

---

## 🌍 Variáveis de Ambiente

O projeto utiliza variáveis prefixadas com `VITE_` gerenciadas via Vite (declare-as em um arquivo `.env` local, ou use o `.env.example` da raiz do monorepo como referência):

| Variável | Descrição | Valor Padrão (Local) |
| :--- | :--- | :--- |
| `VITE_API_URL` | URL base dos endpoints da API REST | `http://localhost:8080/api/v1` |
| `VITE_API_PROXY_TARGET` | Endereço do backend para o proxy reverso do Vite em dev | `http://localhost:8080` |
| `PORT` | Porta de execução do servidor Vite | `3000` |

---

## 📂 Estrutura de Pastas (`src/`)

```
src/
├── api/                  # Cliente Axios, QueryProvider e módulos de integração por domínio
│   ├── client.ts         # Instância base do Axios com interceptors de resposta
│   ├── QueryProvider.tsx # Provider do TanStack React Query
│   ├── aprovacoes.ts     # Integração com o bounded context de aprovações
│   ├── ativos.ts         # Integração com o bounded context de patrimônio (ativos)
│   ├── conselhos.ts      # Integração com decisões/homologações do Conselho (CPC)
│   ├── eventos.ts        # Integração com o calendário de eventos paroquiais
│   ├── locais.ts         # Integração com o cadastro de espaços físicos
│   ├── membros.ts        # Integração com o bounded context de membros
│   ├── metricas.ts       # Integração com métricas estratégicas do dashboard
│   ├── organizacoes.ts   # Integração com a estrutura organizacional
│   ├── pastoraisOficiais.ts # Integração com pastorais/movimentos oficiais
│   ├── projects.ts       # Integração com projetos paroquiais
│   ├── solenidades.ts    # Integração com o calendário litúrgico
│   ├── usuarios.ts       # Integração com gestão de usuários (IAM)
│   └── importacao.ts     # Processamento de arquivos Excel/PDF
├── auth/                 # Contexto de autenticação, JWT e verificação de autorização/roles
│   ├── AuthContext.tsx   # Contexto global de sessão do usuário
│   ├── ProtectedRoute.tsx# Componente de guarda de rota protegida
│   └── permissions.ts   # Utilitários de matriz de permissões por papel
├── components/           # Componentes reutilizáveis de interface
│   ├── dashboard/         # Widgets do Dashboard (cards, gráfico de distribuição, timeline semanal, aprovações pendentes)
│   ├── organograma/       # Exportação/renderização do organograma paroquial
│   ├── reports/           # Filtros, gráficos de volume e exportação de relatórios
│   └── ...                # Modais e diálogos de cadastro/edição (CadastroEventoModal, ImportModal, AtivoAlocacoesModal, CpcDecisaoModal, RecurrenceForm etc.)
├── context/              # Contextos globais da aplicação React (ex: NotificationContext)
├── hooks/                # Hooks customizados React (useMetricasEstrategicas, useOrganogramaExport)
├── pages/                # Páginas da aplicação: Dashboard, Calendar, Aprovacoes, Ativos, ConselhosAtas, Organograma, Projects, Reports, Settings, Login
├── types/                # Definições de tipos auxiliares (ex: react-orgchart.d.ts)
├── utils/                # Utilitários de exportação (CSV, PDF), calendário litúrgico e formatadores
├── theme.ts              # Configuração centralizada do tema Material UI (paleta Fátima, tipografia)
├── setupTests.ts         # Setup do ambiente de testes Vitest com @testing-library/jest-dom
├── AppRoutes.tsx         # Mapeamento centralizado de rotas da aplicação
└── App.tsx               # Componente raiz da aplicação
```

---

## 🧪 Estratégia de Testes

Os testes da interface são executados com **Vitest** e **React Testing Library**, seguindo o padrão `*.test.tsx` / `*.test.ts` colocalizado junto ao arquivo testado. Cobrem:
- Renderização e comportamento de modais e formulários (ex.: `CadastroEventoModal.test.tsx`, `RecurrenceForm.test.tsx`).
- Integração de páginas com a API utilizando mocks de respostas (`DataFetching.test.tsx` e os `*.test.ts` de `src/api/`).
- Guardas de autenticação, contexto de sessão e rotas protegidas (`AuthContext.test.tsx`, `ProtectedRoute.test.tsx`, `permissions.test.ts`).
- Utilitários de exportação (CSV/PDF), calendário litúrgico e formatação de moeda/data.
- Interceptors HTTP e tratamento centralizado de erros (`interceptors.test.ts`).

Execute `npm test` para rodar a suíte completa uma única vez, ou `npm run test:watch` durante o desenvolvimento.

---

## 📚 Documentação Central

Para arquitetura geral, regras de negócio, diagramas Mermaid e especificações do sistema, consulte o [Portal de Documentação Central (Docsify)](../docs/guide/).
