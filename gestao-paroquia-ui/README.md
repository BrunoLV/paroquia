# 🎨 Paróquia — Frontend (`gestao-paroquia-ui`)

Interface administrativa desenvolvida em **React 19** para o sistema de gestão administrativa e pastoral da Paróquia Nossa Senhora de Fátima. Os módulos seguem DDD e Arquitetura Hexagonal por contexto, consumindo a API REST do [backend](../gestao-paroquia).

---

## 🚀 Tecnologias e Ferramentas

- **Framework UI:** **React 19** (`react` / `react-dom`)
- **Linguagem:** **TypeScript 5**
- **Bundler & Dev Server:** **Vite 5** (Hot Module Replacement ultra-rápido e suporte nativo a ES Modules)
- **UI Kit & Estilização:** **Material UI (MUI 9)**, Emotion (`@emotion/react`, `@emotion/styled`)
- **Gráficos:** **ApexCharts** (`apexcharts` / `react-apexcharts`) e **Recharts**
- **Organograma:** `@dabeng/react-orgchart`
- **Gerenciamento de Estado de Servidor:** **TanStack React Query 5** (cache, re-fetching e sincronização)
- **Formulários & Validação:** **React Hook Form** + **Zod** (`@hookform/resolvers`)
- **Cliente HTTP:** **Axios** (configurado com `withCredentials: true`, interceptors de 401 e tratamento centralizado)
- **Roteamento:** **React Router DOM 6** (rotas protegidas e controle de acesso por permissões)
- **Testes Automatizados:** **Vitest 2** + **React Testing Library** + **jsdom**
- **Exportação & Mídia:** `jspdf` + `jspdf-autotable` (PDF), `docx` (Word), CSV nativo e `html-to-image` (geração de imagens do organograma paroquial)

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

## 📂 Arquitetura de Pastas (`src/`)

```
src/
├── app/                  # Bootstrap, providers e roteamento: composição técnica da SPA
├── shared/               # Código transversal técnico, visual e sem domínio próprio
│   ├── domain/types/     # Contratos TypeScript compartilhados
│   ├── infrastructure/   # Cliente HTTP, interceptors e integrações externas
│   ├── presentation/     # Tema, providers e componentes do Design System
│   └── utils/            # Formatadores e funções técnicas reutilizáveis
└── contextos/            # Bounded contexts alinhados ao backend
    └── <contexto>/
        ├── domain/       # Modelos, regras e tipos puros: sem React, Axios ou MUI
        ├── application/  # Use cases e portas de entrada/saída
        ├── infrastructure/http/ # Adapters HTTP, DTOs e mapeadores
        └── presentation/ # Páginas, componentes, hooks Query, schemas Zod e testes
```

Os 16 contextos são `calendario`, `fiel`, `clero`, `sacramento`, `conselho`, `painel`, `governanca`, `iam`, `importacao`, `local`, `organizacao`, `membro`, `patrimonio`, `projeto`, `reserva` e `aprovacao`. Nomes de domínio ficam em português; nomes técnicos, como `Adapter`, `Port`, `Provider`, `Hook` e `Query`, permanecem em inglês.

## 🧭 Mapa de Rotas Canônicas

### Rotas Públicas (Sem Exigência de Login)

| Área | Rota canônica | Contexto |
| :--- | :--- | :--- |
| Acesso e Autenticação | `/acesso` | `iam` |
| Portal de Consentimento e Revogação LGPD (OTP E-mail) | `/portal/consentimento` | `governanca` |
| Política de Privacidade e Proteção de Dados | `/politica-privacidade` | `governanca` |

### Rotas Autenticadas (Protegidas por Sessão e Alçada)

| Área | Rota canônica | Contexto |
| :--- | :--- | :--- |
| Painel Executivo | `/painel` | `painel` |
| Calendário Paroquial e Litúrgico | `/calendario` | `calendario` |
| Cadastro de Fiéis | `/fieis` | `fiel` |
| Clero e Governo Eclesiástico | `/clerigos` | `clero` |
| Livros e Assentos Sacramentais | `/sacramentos` | `sacramento` |
| Organizações Pastorais e Movimentos | `/organizacoes` | `organizacao` |
| Organograma Paroquial | `/organograma` | `organizacao` |
| Governo e Unidades Territoriais | `/governo-territorio` | `organizacao` |
| Conselhos e Atas de Reunião | `/conselhos` | `conselho` |
| Projetos Paroquiais | `/projetos` | `projeto` |
| Central de Aprovações | `/aprovacoes` | `aprovacao` |
| Locais e Espaços Físicos | `/locais` | `local` |
| Patrimônio e Ativos | `/ativos` | `patrimonio` |
| Reservas de Locais e Ativos | `/reservas` | `reserva` |
| Meu Perfil, Ajustes Cadastrais e Privacidade | `/ajustes-cadastrais` | `governanca` / `fiel` |
| Notificações | `/notificacoes` | `iam` |
| Relatórios e Exportações | `/relatorios` | `painel` |
| Configurações, Ano Paroquial e DPO/LGPD | `/configuracoes` | `governanca` / `iam` |

As antigas URLs inglesas não são suportadas.

---

## 🧪 Estratégia de Testes

Os testes da interface são executados com **Vitest** e **React Testing Library**, seguindo o padrão `*.test.tsx` / `*.test.ts` colocalizado junto ao arquivo testado. Cobrem:
- Renderização e comportamento de modais e formulários (ex.: `CadastroEventoModal.test.tsx`, `RecurrenceForm.test.tsx`).
- Integração de páginas e adapters HTTP com mocks de respostas, co-localizados em `contextos/<contexto>/`.
- Guardas de autenticação, contexto de sessão e rotas protegidas (`AuthContext.test.tsx`, `ProtectedRoute.test.tsx`, `permissions.test.ts`).
- Utilitários de exportação (CSV/PDF), calendário litúrgico e formatação de moeda/data.
- Interceptors HTTP e tratamento centralizado de erros (`interceptors.test.ts`).

Execute `npm test` para rodar a suíte completa uma única vez, ou `npm run test:watch` durante o desenvolvimento.

---

## 📚 Documentação Central

Para arquitetura geral, regras de negócio, diagramas Mermaid e especificações do sistema, consulte o [Portal de Documentação Central (Docsify)](../docs/guide/).
