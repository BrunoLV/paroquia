# ⛪ Projeto Paróquia

[![Docker Compose](https://img.shields.io/badge/docker-compose-blue.svg?style=flat-square&logo=docker)](./docker-compose.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF.svg?style=flat-square&logo=kotlin)](./gestao-paroquia)
[![React](https://img.shields.io/badge/React-19-61DAFB.svg?style=flat-square&logo=react)](./gestao-paroquia-ui)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791.svg?style=flat-square&logo=postgresql)](./docker-compose.yml)

Sistema unificado para gestão administrativa e pastoral de paróquias, integrando backend em Spring Boot e frontend em React.

---

## 🏗️ Estrutura do Projeto

Este é um monorepo que organiza o sistema em módulos independentes:

- **[Backend (gestao-paroquia)](./gestao-paroquia):** API RESTful desenvolvida em Kotlin/Java com Spring Boot.
- **[Frontend (gestao-paroquia-ui)](./gestao-paroquia-ui):** Interface administrativa moderna em React com Material UI.
- **[Infraestrutura](./docker-compose.yml):** Orquestração via Docker para banco de dados, API, UI e análise de código (SonarQube).
- **[Conductor](./conductor):** Documentação técnica, planos de desenvolvimento e diretrizes do produto.

---

## 🚀 Como Iniciar

A forma mais rápida de rodar o projeto completo é utilizando o Docker Compose na raiz:

```bash
# Sobe todos os serviços (DB, API, UI, SonarQube)
docker-compose up -d
```

### Serviços Disponíveis:
- **Frontend:** [http://localhost:3000](http://localhost:3000)
- **Backend API:** [http://localhost:8080](http://localhost:8080)
- **SonarQube:** [http://localhost:9000](http://localhost:9000)
- **pgAdmin:** [http://localhost:5050](http://localhost:5050)

---

## 🛠️ Tecnologias Principais

| Camada | Tecnologia |
| :--- | :--- |
| **Linguagens** | Kotlin, Java, TypeScript |
| **Framework Backend** | Spring Boot 3.x, Gradle |
| **Biblioteca Frontend** | React 19, Material UI (MUI), React Query |
| **Banco de Dados** | PostgreSQL 16 |
| **Qualidade** | SonarQube, Jest, JUnit |

---

## 📖 Documentação Adicional

Para detalhes sobre regras de negócio, cronograma e arquitetura, consulte a pasta `/conductor`.

- [Guia do Produto](./conductor/product.md)
- [Stack Tecnológica](./conductor/tech-stack.md)
- [Workflow de Desenvolvimento](./conductor/workflow.md)

---
*Desenvolvido com foco em eficiência e organização paroquial.*
