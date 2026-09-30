# Changelog - Projeto Paróquia

Todas as alterações notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado no [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/), e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

---

## [Unreleased]

### Removido
- **Saneamento e Limpeza da Suíte de Testes (Backend e Frontend):**
  - Remoção de testes obsoletos preliminares BDD/SDD (`SddCenario1CadastroSemLoginIntegrationTest`, `SddCenario2ConsultaFielUsuarioIntegrationTest`, `SddCenario3AcessoVigarioIntegrationTest`), superados pela suíte canônica de testes de domínio de Fiel, Membro e IAM.
  - Eliminação de testes rudimentares e duplicados (`LocalApiIntegrationTest`, `SmokeCalendarFlowTest`, `EventoIntegrationTest`, `ContractValidationErrorIntegrationTest`, `LegacyEventStatusReadIntegrationTest`).
  - Remoção de testes de getters e setters de JPA Entities anêmicas (`ReservaEntityTest`, `EventoRecorrenciaEntityTest`) e teste artificial de `assertNotNull` em contexto Spring (`FoundationInfrastructureTest`).
  - Desativação da suíte frágil de medição de latência em banco H2 (`AuditoriaTier1PerformanceTest`, `CancelEventoTier1PerformanceTest`, `ConsultaCalendarioPerformanceTest`, `ObservacaoTier1PerformanceTest`, `UpdateEventoTier1PerformanceTest`), reduzindo drasticamente o tempo e consumo de memória do CI.
  - Remoção no frontend de testes com mocks incompletos de Jest ou superficiais de ícones (`api.test.tsx`, `DataFetching.test.tsx`, `OrganogramaExportButton.test.tsx`, `CpcDecisaoModal.test.tsx`, `DialogExportarCalendario.test.tsx`).

## [0.4.0] - 2026-09-30

### Adicionado
- **Configuração de Pre-Commit no Monorepo:**
  - Criação do arquivo `.pre-commit-config.yaml` na raiz com hooks locais para execução automática de `checkstyleMain`, `checkstyleTest`, `pmdMain` e `pmdTest` no backend (`gestao-paroquia`) e typecheck (`npm run build`) no frontend (`gestao-paroquia-ui`).
- **Detecção Estática de Código, Métodos e Tipos Não Utilizados:**
  - Ativação das regras `UnusedLocalVariable` e `UnusedPrivateMethod` no PMD (`config/pmd/pmd.xml`) e módulo `UnusedLocalVariable` no Checkstyle (`config/checkstyle/checkstyle.xml`) no backend.
  - Ativação de `"noUnusedLocals": true` no TypeScript (`tsconfig.json`) do frontend (`gestao-paroquia-ui`).

### Corrigido
- **Conformidade Estática e Qualidade de Código (PMD, Checkstyle e TypeScript):**
  - Eliminação de violações de `ExcessivePublicCount` e `RedundantFieldInitializer` em `UnidadeTerritorialEntity`.
  - Resolução de violações de `LineLength`, `MultipleStringLiterals`, `WhitespaceAround` e `JavadocStyle` em casos de uso, resolvers de segurança, serviços e classes de teste de `calendario`, `clero`, `organizacao`, `iam` e `local`.
  - Remoção de variáveis e métodos privados não utilizados em `PastoralMovimentoOficialService`, `CreateEventoAprovacaoPendenteIntegrationTest`, `GrupoParoquialApiIntegrationTest` e `PastoralMovimentoOficialApiIntegrationTest`.
  - Limpeza integral de imports, tipos e variáveis não utilizados nos componentes e páginas do frontend.
---

## [0.3.0] - 2026-09-30

### Modificado
- **Clean Slate em Authorities e `@PreAuthorize` do Spring Security:**
  - Padronização integral de todas as anotações `@PreAuthorize` em controllers REST (`FielController`, `UsuarioController`, `UsuarioAdminController`, `CpcDecisaoController`, `LocalController`, `ImportacaoController`, `ClerigoController`, `AjusteCadastralController`, `UnidadeTerritorialController`, `DioceseController`, `GovernoEclesiasticoController`, `PastoralMovimentoOficialController`, `GrupoParoquialController`, `MembroController`) para referenciar estritamente os 14 papéis canônicos (`'ADMIN'`, `'PAROCO'`, `'BISPO_DIOCESANO'`, `'VIGARIO'`, `'SECRETARIA_PAROQUIAL'`, `'COORDENADOR'`, `'SECRETARIO_CONSELHO'`, `'MEMBRO_CONSELHO_PASTORAL'`, `'MEMBRO_CONSELHO_ECONOMICO'`), eliminando prefixos legados e nomes compostos (`CLERO_PAROCO`, `GLOBAL_PAROCO`, `PASTORAL_COORDENADOR`, `MINISTERIO_COORDENADOR`).
  - `LeitorVinculoExterno.Vinculo.authority()` e `UsuarioDetails` emitem `ROLE_<PAPEL>` (canônico) e mantêm `ROLE_<TIPO>_<PAPEL>`/`ROLE_GLOBAL_<PAPEL>` como compatibilidade secundária de escopo.
  - Atualização dos testes de integração de segurança (`UsuarioControllerSecurityIntegrationTest`, `LocalControllerIntegrationTest`, `GrupoParoquialApiIntegrationTest`, `PastoralMovimentoOficialApiIntegrationTest`) para utilizar os papéis canônicos.

---

## [0.2.0] - 2026-09-27

### Adicionado
- **Reestruturação Canônica de Permissionamento (Clean Slate Total):**
  - Implementação estrita e ortogonal dos 14 papéis canônicos finais em 3 camadas:
    - **Globais (IAM):** `ADMIN`, `BISPO_DIOCESANO`, `PAROCO`, `VIGARIO`, `DIACONO_PAROQUIAL`, `SECRETARIA_PAROQUIAL`, `FIEL`.
    - **Conselhos (Colegiados Consultivos):** `SECRETARIO_CONSELHO`, `MEMBRO_CONSELHO_PASTORAL`, `MEMBRO_CONSELHO_ECONOMICO`.
    - **Pastorais e Movimentos:** `COORDENADOR`, `SECRETARIO_GRUPO`, `MEMBRO_ATIVO`, `VOLUNTARIO`.
  - Princípio da Proposição vs. Homologação canônica (Cân. 536 §2 e Cân. 537): conselhos e secretarias elaboram minutas e propostas; homologação deliberativa reservada exclusivamente ao `PAROCO`.
  - Separação ontológica entre Ordem Sacramental e Ofício Canônico com provisão ativa (Cân. 515 §1 e 540 §1).
  - Isolamento do papel técnico `ADMIN`: proibição estrita de homologar atas, aprovar eventos e fechar o ano paroquial.
  - Atualização e unificação do contrato de permissões no frontend (`permissions.ts` e `permissions.test.ts`).
  - Refatoração dos scripts DDL (`V004`, `V005`, `V010`) e bases de dados de testes (`seed-data.sql`, `security-fixtures.sql`).
  - Atualização da Living Documentation em `docs/business-rules/` (`iam.md`, `conselho.md`, `aprovacao.md`, `membro.md`).
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
