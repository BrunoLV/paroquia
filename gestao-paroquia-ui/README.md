# 🎨 Paróquia - Frontend (gestao-paroquia-ui)

Interface administrativa desenvolvida em React para o sistema de gestão paroquial.

## 🚀 Tecnologias

- **Framework:** React 19
- **Linguagem:** TypeScript
- **UI Kit:** Material UI (MUI) 9
- **Gerenciamento de Estado/API:** TanStack React Query
- **Requisições:** Axios
- **Roteamento:** React Router DOM 6

## 🛠️ Desenvolvimento

### Pré-requisitos
- Node.js (v18+)
- npm ou yarn

### Instalação e Execução
```bash
# Instalar dependências
npm install

# Iniciar servidor de desenvolvimento
npm start

# Rodar testes
npm test

# Build para produção
npm run build
```

## 🌍 Variáveis de Ambiente
O projeto utiliza um arquivo `.env` para configurações:
- `REACT_APP_API_URL`: URL base da API (padrão: `http://localhost:8080/api/v1`)

## 📂 Estrutura de Pastas
- `src/api/`: Serviços e hooks de integração com o backend.
- `src/auth/`: Lógica de autenticação.
- `src/pages/`: Componentes de página e roteamento.
- `src/theme.ts`: Configuração do tema Material UI.
