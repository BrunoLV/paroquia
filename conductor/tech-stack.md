# Tech Stack

## ⚙️ Backend Stack (`gestao-paroquia`)

### Primary Language & Runtime
- **Java 25:** Strongly typed, modern object-oriented language managed natively via Gradle Toolchains (Foojay Resolver).

### Core Framework & Architecture
- **Spring Boot 3.x:** Foundational framework adhering to **Domain-Driven Design (DDD)** and **Clean Architecture (Ports & Adapters / Hexagonal)**.
- **Spring Data JPA:** Data access layer abstraction with Object-Relational Mapping.
- **Spring Security:** Stateless JWT authentication, role-based authorization, and granular scope access control.
- **Spring Web (MVC):** Declarative REST API endpoint controllers.
- **Spring Boot Actuator:** Telemetry, health checks, and operational metrics.
- **Spring Cache + Caffeine:** High-performance in-memory caching.

### Database & Migrations
- **PostgreSQL 16:** Primary relational database engine for production and Docker Compose runtime.
- **H2 Database:** In-memory relational database reserved strictly for automated unit and integration tests.
- **Flyway:** Database schema migration management.

### API Documentation & Contracts
- **Springdoc OpenAPI 2.x / Swagger UI:** Interactive OpenAPI 3 spec generation and Swagger testing interface.

### Quality Gates & Linters
- **Checkstyle 13.8.0:** Automated code style and layout enforcement.
- **PMD 7.16.0:** Static analysis, code smell detection, and rule checking.
- **SpotBugs:** Security flaw and null-pointer risk detection.
- **JaCoCo:** Code coverage analysis and reporting.

### Report Generation & Media
- **Apache POI:** Microsoft Excel (`.xlsx`) batch generation and parsing.
- **OpenPDF:** PDF report generation.

---

## 🎨 Frontend Stack (`gestao-paroquia-ui`)

### Primary Language
- **TypeScript 5:** Strongly typed language built on JavaScript, providing type safety, improved maintainability, and autocomplete across React components.

### Core Framework & Bundler
- **React 19 (via Vite 5):** Foundational library for building the user interface, bundled and served using Vite 5 for instant HMR startup and optimized production builds.

### UI Component Library & Styling
- **Material UI (MUI 9):** Comprehensive suite of pre-built React components following Marian Fátima design tokens (`@emotion/react`, `@emotion/styled`).

### State Management & Data Fetching
- **TanStack React Query 5:** Manages server state, data fetching, caching, synchronization, and optimistic UI updates.
- **Axios:** Base HTTP client with `withCredentials` enabled for cookie session handling and response interceptors for 401 redirection.
- **Context API:** Handles application-wide global UI state (`AuthContext`, `NotificationContext`).

### Routing
- **React Router (v6):** Client-side routing, protected routes, and permission-based page guards.

### Testing Framework
- **Vitest 2:** Blazing fast unit and component test runner natively integrated with Vite, jsdom, and React Testing Library.

### Media Utilities
- **`html-to-image`:** Exporting interactive parish organogram views to PNG images.
- **`@dabeng/react-orgchart`:** Interactive hierarchical tree rendering and zoom/pan organogram view.

### Data Visualization (Charts)
- **ApexCharts (`react-apexcharts`, `apexcharts`):** Bar/volume comparison chart in `Reports.tsx`. Chosen over `@mui/x-charts` for native zoom/horizontal-scroll and a built-in export menu (PNG/SVG/CSV).
- **Recharts (`recharts`):** Pie/donut event-distribution chart (`EventDistributionPieChart.tsx`, shared by `Dashboard.tsx` and `Reports.tsx`). Chosen over `@mui/x-charts` for external label lines and easier custom grouping ("Outras" slice for <3% shares).
- **`@mui/x-charts`:** Removed — no longer referenced anywhere in the codebase after the migration below.

> **2026-07-31:** Partially replaced `@mui/x-charts` with ApexCharts (bar chart) and Recharts (pie/donut chart) per track `modernizacao_graficos_visualizacoes_20260731`. Widgets under `WeeklyTimeline.tsx`, `ActiveProjectsProgressList.tsx`, and `MetricasEstrategicasWidget.tsx` remain chart-library-free (MUI `LinearProgress`/`Timeline` only), and `Organograma.tsx` keeps `@dabeng/react-orgchart`.


