# ⛪ Projeto Paróquia

[![Docker Compose](https://img.shields.io/badge/docker-compose-blue.svg?style=flat-square&logo=docker)](./docker-compose.yml)
[![Java 25](https://img.shields.io/badge/Java-25-ED8B00.svg?style=flat-square&logo=openjdk)](./gestao-paroquia)
[![Spring Boot 3](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F.svg?style=flat-square&logo=springboot)](./gestao-paroquia)
[![React 19](https://img.shields.io/badge/React-19-61DAFB.svg?style=flat-square&logo=react)](./gestao-paroquia-ui)
[![Vite 5](https://img.shields.io/badge/Vite-5-646CFF.svg?style=flat-square&logo=vite)](./gestao-paroquia-ui)
[![PostgreSQL 16](https://img.shields.io/badge/PostgreSQL-16-336791.svg?style=flat-square&logo=postgresql)](./docker-compose.yml)

Sistema unificado de gestão administrativa e pastoral para a Paróquia Nossa Senhora de Fátima, integrando backend monolítico modular em **Java 25 (Spring Boot 3)** e frontend em **React 19 (TypeScript + Material UI 9 + Vite)**.

---

## 🏗️ Estrutura do Monorepo

O repositório é organizado em módulos independentes com regras de arquitetura bem definidas:

- **[Backend (`gestao-paroquia`)](./gestao-paroquia):** API RESTful desenvolvida em **Java 25** com **Spring Boot 3**, estruturada sob os princípios de **Domain-Driven Design (DDD)** e **Clean Architecture (Ports & Adapters)**.
- **[Frontend (`gestao-paroquia-ui`)](./gestao-paroquia-ui):** Interface de usuário moderna desenvolvida em **React 19**, **TypeScript**, **Material UI (MUI 9)**, **TanStack React Query** e **Vite 5**.
- **[Infraestrutura (`docker-compose.yml`)](./docker-compose.yml):** Orquestração completa em Docker para banco de dados PostgreSQL 16, backend API, frontend UI, pgAdmin 4 e seeder automático de dados.
- **[Conductor (`conductor/`)](./conductor):** Especificações funcionais, diretrizes do produto e trilhas de tarefas.
- **[Documentação (`gestao-paroquia/docs/`)](./gestao-paroquia/docs):** Portal de documentação técnica, arquitetura, domínios e conformidade com a LGPD.

---

## 🚀 Como Iniciar

A forma recomendada de executar a aplicação completa é via **Docker Compose**:

```bash
# Subir todos os serviços (PostgreSQL, Backend API, Frontend UI, pgAdmin e Seeder)
docker compose up -d
```

### 🌐 Serviços e Ambientes Disponíveis:

| Serviço | URL / Endereço | Credenciais / Notas |
| :--- | :--- | :--- |
| **Frontend Web** | [http://localhost:3000](http://localhost:3000) | Interface administrativa em React (Vite) |
| **Backend REST API** | [http://localhost:8080](http://localhost:8080) | API Spring Boot 3 |
| **Swagger UI** | [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) | Documentação interativa OpenAPI |
| **pgAdmin 4** | [http://localhost:5050](http://localhost:5050) | Email: `admin@admin.com` \| Senha: `admin` |
| **PostgreSQL DB** | `localhost:5432` | DB: `calendario` \| User: `postgres` \| Pass: `password` |

---

## 🛠️ Tecnologias Principais

| Camada | Tecnologias e Ferramentas |
| :--- | :--- |
| **Backend** | Java 25 (Gradle Toolchain), Spring Boot 3, Spring Data JPA, Spring Security, Spring Actuator, Spring Cache + Caffeine, Flyway |
| **Frontend** | React 19, TypeScript, Vite 5, Material UI (MUI 9), TanStack React Query 5, Axios, React Router DOM 6 |
| **Banco de Dados** | PostgreSQL 16 (Ambiente de Execução / Docker), H2 Database (Ambiente de Testes) |
| **Qualidade & Linters** | **Backend:** Checkstyle 13.8.0, PMD 7.16.0, SpotBugs, JaCoCo <br> **Frontend:** Vitest, React Testing Library, TypeScript (`tsc`) |
| **Relatórios / Mídia** | Apache POI (Excel), OpenPDF (PDF), `html-to-image` (Exportação de Organogramas) |

---

## 🛡️ Governança, Qualidade e LGPD

- **Engenharia e Padrões de Codificação:** Consulte [`AGENTS.md`](./AGENTS.md) e [`gestao-paroquia/AGENTS.md`](./gestao-paroquia/AGENTS.md) para diretrizes de desenvolvimento, TDD e linters.
- **Privacidade e Proteção de Dados (LGPD):** Consulte a diretiva em [`gestao-paroquia/docs/ia/regra_dura_lgpd.md`](./gestao-paroquia/docs/ia/regra_dura_lgpd.md).

---

## 📖 Documentação Adicional

- [Visão Geral da Arquitetura do Backend](./gestao-paroquia/docs/architecture.md)
- [Guia do Produto (Conductor)](./conductor/product.md)
- [Stack Tecnológica (Conductor)](./conductor/tech-stack.md)
- [Workflow de Desenvolvimento (Conductor)](./conductor/workflow.md)

---
*Desenvolvido com foco em eficiência, alta confiabilidade e organização paroquial.*
