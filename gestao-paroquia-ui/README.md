# 🎨 Paróquia - Frontend (`gestao-paroquia-ui`)

Interface administrativa moderna desenvolvida em **React 19** para o sistema de gestão da Paróquia Nossa Senhora de Fátima.

---

## 🚀 Tecnologias e Ferramentas

- **Framework UI:** **React 19** (`react` / `react-dom`)
- **Linguagem:** **TypeScript 5**
- **Bundler & Dev Server:** **Vite 5** (HMR ultra-rápido e suporte nativo a ES Modules)
- **UI Kit & Estilização:** **Material UI (MUI 9)**, Emotion (`@emotion/react`, `@emotion/styled`), `@mui/x-charts`
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

O projeto utiliza variáveis prefixadas com `VITE_` gerenciadas via Vite:

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
│   ├── approvals.ts      # Integração com o bounded context de aprovações
│   ├── membros.ts        # Integração com o bounded context de membros
│   ├── organizacoes.ts   # Integração com a estrutura organizacional
│   ├── usuarios.ts       # Integração com gestão de usuários (IAM)
│   └── importacao.ts     # Processamento de arquivos Excel/PDF
├── auth/                 # Contexto de autenticação, JWT e verificação de autorização/roles
│   ├── AuthContext.tsx   # Contexto global de sessão do usuário
│   ├── ProtectedRoute.tsx# Componente de guarda de rota protegida
│   └── permissions.ts   # Utilitários de matriz de permissões por papel
├── components/           # Componentes reutilizáveis de interface
│   ├── layout/           # Sidebar, Navbar e Container base
│   ├── modals/           # Modais de cadastro, edição e confirmação
│   └── OrganogramaCanvas # Renderizador interativo do organograma paroquial
├── context/              # Contextos globais da aplicação React (Auth, Tema)
├── hooks/                # Hooks customizados React (ex: exportação de organograma)
├── pages/                # Páginas e visões da aplicação (Dashboard, Agendamentos, Aprovações, etc.)
├── utils/                # Utilitários de exportação (CSV, PDF) e formatadores de data/moeda
├── theme.ts              # Configuração centralizada do tema Material UI (paleta Fátima, tipografia)
├── setupTests.ts         # Setup do ambiente de testes Vitest com @testing-library/jest-dom
├── AppRoutes.tsx         # Mapeamento centralizado de rotas da aplicação
└── App.tsx               # Componente raiz da aplicação
```

---

## 🧪 Estratégia de Testes

Os testes da interface são executados com **Vitest** e **React Testing Library**, cobrindo:
- Renderização e comportamento de modais e formulários.
- Integração de páginas com a API utilizando mocks de respostas.
- Guardas de autenticação e rotas protegidas.
- Utilitários de exportação e manipulação de estado.

