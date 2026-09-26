# Changelog - Projeto Paróquia

Todas as alterações notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado no [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/), e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

---

## [Unreleased]

### Adicionado
- Skill universal `fluxo-desenvolvimento` em `.agents/skills/fluxo-desenvolvimento/SKILL.md` para orquestração de planejamento e execução de software.
- Regra permanente de runtime em `.agents/rules/fluxo-desenvolvimento.md` aplicável a qualquer agente (Claude, Gemini, Antigravity, Cursor, Copilot).
- Diretório `docs/business-rules/` com documentação canônica de regras e fluxos de negócio para os 15 Bounded Contexts da paróquia.
- Guia institucional de submissão de Pull Requests em `.github/pull-request.instructions.md`.
- Formalização explícita da versão do backend no `gestao-paroquia/app/build.gradle.kts`.

---

## [0.1.0] - 2026-09-26

### Adicionado
- **Arquitetura Base Monorepo:**
  - Backend modular em Java 25, Spring Boot 4, Jetty 12 e arquitetura Hexagonal / DDD puro.
  - Frontend SPA em React 19, TypeScript estrito, Vite e Material UI (MUI 9).
  - Migrações de banco de dados relacionais via Flyway em PostgreSQL 16 com suporte a Testcontainers.
- **Calendário & Liturgia (`calendario`):**
  - Motor de cálculo de datas móveis litúrgicas via algoritmo canônico de Meeus/Jones/Butcher.
  - Agendamento de eventos litúrgicos, pastorais, sociais e administrativos com detecção de choque de horários (`ChoqueHorarioPolicy`).
  - Suporte a eventos recorrentes com escopo de edição (`APENAS_ESTE` e `TODOS_SEGUINTES`).
  - Exportação oficial de programações litúrgicas em PowerPoint (PPTX), Word (DOCX) e PDF.
  - Catálogo Litúrgico e Santoral padronizado no formato `Catálogo Litúrgico & Solenidades - {Ciclo} - {Ano}`.
- **Aprovações Paroquiais (`aprovacao`):**
  - Esteira centralizada de aprovação de eventos e projetos com classificação por criticidade e urgência.
  - Card executivo sintético no painel consolidado por tipo de solicitação com acesso direto ao módulo.
- **Governança Eclesiástica & Clero (`clero`):**
  - Cadastro canônico de clérigos (diáconos, padres, bispo) e vínculo com a diocese.
  - Registro de provisões canônicas com controle de exclusividade de Pároco em exercício.
  - Paridade total de proteção de privacidade (LGPD) entre clérigos e leigos.
- **Gestão de Fiéis & Pastoral (`fiel`, `membro`, `organizacao`):**
  - Registro civil e sacramental de fiéis (Batismo, Primeira Comunhão, Crisma, Matrimônio, Ordem).
  - Organograma paroquial de pastorais, movimentos, setores e comunidades com validação acíclica.
  - Vínculo de membros, mandatos de coordenação e histórico ministerial.
- **Projetos Pastorais (`projeto`):**
  - Acompanhamento de metas, progresso e diagnóstico preditivo de risco de projetos (`emRisco`).
  - Ordenação prioritária de projetos sob risco no topo do painel executivo com alerta visual e diagnóstico.
- **Painel Executivo / Dashboard (`painel`):**
  - Reestruturação completa do dashboard alinhada ao `DESIGN_SYSTEM.md` em grid simétrico de 12 colunas.
  - Substituição do antigo indicador de engajamento pastoral pelo KPI auditado de **Fiéis ativos** (`resumo?.totalFieisAtivos`) e novos no mês.
  - Linha do tempo semanal (`WeeklyTimeline`) e celebrações iminentes (`UpcomingEvents`).
- **Segurança, Acesso e Auditoria (`iam`, `governanca`):**
  - Autenticação stateless via JWT com RBAC declarativo (`@PreAuthorize`).
  - Auditoria 100% automática via Spring Data JPA Auditing através de `BaseAuditableEntity` com 4 colunas canônicas em UTC.
- **Design System Arrojado do Frontend (`DESIGN_SYSTEM.md`):**
  - Padronização de botões corporativos (`ActionButton`), campos com `size="small"`, modais com `<DialogTitle>` limpo, formulários controlados via Zod + React Hook Form e estados assíncronos.
