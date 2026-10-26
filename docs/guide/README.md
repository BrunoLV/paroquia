# ⛪ Projeto Paróquia — Guia Técnico & Arquitetural

Bem-vindo ao portal de documentação viva do **Projeto Paróquia**. Este documento estabelece os fundamentos, objetivos, arquitetura e convenções técnicas que orientam o desenvolvimento de todo o ecossistema do sistema de gestão paroquial.

---

## 🎯 Objetivo e Missão do Projeto

O **Projeto Paróquia** é um sistema unificado de gestão administrativa, litúrgica e pastoral, concebido para atender com precisão, zelo e conformidade legal as demandas rotineiras de uma paróquia católica contemporânea.

Historicamente, administrações paroquiais dependem de planilhas dispersas, livros físicos e anotações descentralizadas para gerenciar eventos, fiéis, sacramentos, atas de reuniões e patrimônio material. O objetivo central deste projeto é **digitalizar, integrar e proteger** esses fluxos em uma plataforma unificada de alta confiabilidade:

1. **Zelo Litúrgico e Pastoral:** Integrar o calendário paroquial civil ao **Calendário Litúrgico Romano**, considerando solenidades, festas, preceitos e tempos litúrgicos (Advento, Natal, Quaresma, Páscoa e Tempo Comum).
2. **Harmonia Organizacional:** Modelar com fidelidade canônica a estrutura paroquial (Comunidades, Capelas, Pastorais, Movimentos, Setores e Serviços), com papéis claros para clérigos, coordenadores e agentes leigos.
3. **Governança Participativa:** Suportar os processos decisórios dos Conselhos Pastorais (CPP e CPC) e do Conselho de Assuntos Econômicos (CAEP), viabilizando atas estruturadas, pautas, pareceres e homologações pelo pároco.
4. **Zelo pelo Patrimônio Eclesial:** Prover rastreabilidade rigorosa de ativos paroquiais (inventário, alocação para eventos, transferências entre comunidades e conservação).
5. **Privacidade por Desenho (*Privacy by Design*):** Cumprir estritamente a **Lei Geral de Proteção de Dados (LGPD - Lei 13.709/2018)**, assegurando que dados de fiéis e clérigos tenham tratamento ético, seguro, com PII Zero em logs e segregação estrita de acesso.

---

## 🏛️ Pilares Fundamentais

| Pilar | Descrição |
| :--- | :--- |
| **Living Documentation** | Toda a arquitetura e regras de negócio são documentadas junto ao código, mantidas por testes unitários e diagramas conceituais atualizados. |
| **Clean & Hexagonal Architecture** | Regras de negócio puras, livres de dependências de frameworks, banco de dados ou UI, encapsuladas na camada `domain`. |
| **Tell, Don't Ask & Anti-Anemia** | Agregados ricos que comandam suas próprias transições de estado por métodos semânticos, repudiando modelos anêmicos de dados. |
| **Segurança e Paridade LGPD** | Mesmas regras rigorosas de privacidade para leigos e clérigos, controle de acesso baseado em papéis (RBAC) e proteção contra IDOR. |
| **Alta Performance & Resiliência** | Uso de **Virtual Threads** do Java 25 para operações de E/S sem bloqueio de threads de sistema operacional, e logging assíncrono de altíssima vazão. |

---

## 🛠️ Stack Tecnológica

O ecossistema é organizado em um **Monorepo com fronteiras estritamente isoladas**:

### Backend (`gestao-paroquia/`)
- **Linguagem / Runtime:** Java 25 com OpenJDK Toolchain e Virtual Threads ativas.
- **Framework Core:** Spring Boot 4.x com injeção de dependências estrita por construtor imutável.
- **Servidor Web Embarcado:** Eclipse Jetty 12 configurado para alta concorrência de E/S.
- **Logging Assíncrono:** Log4j2 com LMAX Disruptor (`AsyncLoggerContextSelector`) e formato JSON estruturado com mascaramento PII.
- **Persistência Relacional:** PostgreSQL 16 com migrações versionadas via Flyway e testes automatizados com Testcontainers (PostgreSQL real, sem banco em memória).
- **Qualidade & Inspeção:** Checkstyle 13, PMD 7, SpotBugs, JaCoCo, JUnit 5, AssertJ e Mockito 5 (`STRICT_STUBS`).

### Frontend (`gestao-paroquia-ui/`)
- **Biblioteca Base:** React 19 em TypeScript estrito.
- **Tooling de Build:** Vite 5 com compilação ultra-rápida e Tree-Shaking otimizado.
- **Design System & UI:** Material UI 9 (MUI) com tema padronizado e sem tags HTML cruas de layout.
- **Gerenciamento de Estado de Servidor:** TanStack React Query v5 para cache, prefetch e mutações otimistas.
- **Formulários e Validação:** React Hook Form integrado ao Zod para validação declarativa desacoplada de apresentação.
- **Testes Unitários:** Vitest + React Testing Library.

---

## 🗺️ Mapa de Navegação do Guia

Utilize o menu lateral ou os links abaixo para explorar os tópicos específicos da documentação:

- [📋 Regras de Negócio e Funcionalidades](regras-funcionalidades.md): Detalhamento dos 17 domínios do backend e dos 16 contextos da interface web.
- [🏛️ Padrões de Arquitetura](arquitetura.md): Princípios de Clean Architecture, Hexagonal e diagrama C4 Container.
- [🧩 Padrões de Projeto (Design Patterns)](padroes-projeto.md): Aplicação prática de GoF e DDD tático com diagrama de classes dos agregados.
- [🔄 Diagramas de Sequência](fluxos-sequencia.md): Fluxos visuais de agendamento de eventos, reserva de locais/ativos e homologação de atas de conselho.
- [⚙️ Diagramas de Estados](estados.md): Máquinas de estado finito de eventos, aprovações, reservas, ativos patrimoniais e atas.
- [📊 Logging & Métricas](logging-metricas.md): Log4j2 assíncrono, MDC, filtros LGPD PII Zero e métricas Prometheus/Actuator.
