# Changelog - Projeto Paróquia

Todas as alterações notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado no [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/), e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

---

## [Unreleased]

## [0.7.2] - 2026-10-01

### Corrigido
- Menu lateral reorganizado por finalidade em seis seções, com cabeçalhos legíveis, recuo dos links e recolhimento acessível; rotas e permissões preservadas, inclusive nos modos compacto e mobile.
- Ações das tabelas padronizadas com menu compartilhado e botão delineado `⋮`, sem texto visível, preservando permissões e estados; regra permanente dos agentes e testes de teclado, foco e estrutura.
- Autocompletes alinhados aos campos de texto do tema em altura, espaçamento e superfície; seta e limpeza sem borda de botão, preservando seleção múltipla e pesquisa.
- Paginação integrada ao rodapé semântico das tabelas; Linhas por página com pesquisa e mesma altura dos botões anterior/próximo, sem alterar formulários. Padrão registrado no Design System e nas configurações dos agentes, com teste estrutural contra rodapés externos.

## [0.7.1] - 2026-10-01

### Modificado
- Todas as listas de valores da UI oferecem autocomplete com pesquisa, incluindo filtros, seleção múltipla e quantidade de linhas por página; códigos, IDs e permissões dos contextos são preservados.
- Design System, instruções dos agentes e skill frontend exigem listas pesquisáveis, com verificação estrutural automatizada contra seletores sem pesquisa.

## [0.7.0] - 2026-10-01

### Adicionado
- **Módulo de Gerenciamento de Sacramentos (`sacramento`):**
  - Livro de assentos de Batismo, Confirmação, Matrimônio e Ordem Sacra com ciclo `DRAFT → SEALED` (homologação privativa do Pároco), cancelamento motivado e nenhuma exclusão física (triggers no banco).
  - Snapshot textual dos participantes com vínculo tardio opcional a Fiel/Clérigo, que nunca altera o texto lavrado; endpoint de visão por vínculo.
  - Averbações marginais append-only no Batismo (digitação pela Secretaria, homologação pelo Pároco), ressalvas de retificação material, notificações canônicas e certidão narrativa com omissão de dados sensíveis.
  - Livro reservado (matrimônio secreto) invisível à Secretaria.
  - Trilha estrita `assento_historico` (autor, perfil, IP, valores anteriores e posteriores) e matriz de alçadas `AlcadaSacramentalPolicy`; `ADMIN` sem acesso.
  - Frontend: página `/sacramentos` com abas por sacramento, formulário de rascunho, ficha com ações por alçada, vínculo, averbação, retificação, cancelamento e certidão.
  - Seção "Vida sacramental" nas fichas de Fiel e Clérigo, somente consulta, com o tipo de participação da pessoa em cada assento.
  - Vínculo de fiel/clérigo feito exclusivamente a partir do registro do sacramento, informando o tipo de participação (padrinho, testemunha, ministro etc.). O vínculo da Secretaria fica pendente; o Pároco aprova ou rejeita na nova aba "Vínculos Sacramentais" da tela de Aprovações e recebe notificação `SACRAMENTO_VINCULO_PENDENTE`; só o Pároco desvincula (migração `V024`).
  - Seed de desenvolvimento com 9 assentos de cenários (4 sacramentos; rascunho, homologado e cancelado; livro reservado; dado sensível; vínculos aprovados, pendentes e sem cadastro; averbações, ressalva, notificações canônicas, trilha e notificação ao Pároco), validado por `SeedDataSqlIntegrityTest` contra as policies do domínio.
  - Documentação `docs/business-rules/sacramento.md` (RN-SAC-01 a RN-SAC-10).

## [0.6.0] - 2026-10-01

### Adicionado
- **Fluxos de Alteração e Redefinição de Senha (Autoatendimento e Recuperação):**
  - Endpoint autenticado `POST /api/v1/autenticacao/alterar-senha` permitindo ao usuário logado alterar sua própria senha com validação da credencial atual.
  - Endpoint público `POST /api/v1/autenticacao/solicitar-reset-senha` (`permitAll()`) para registro de solicitação anônima de redefinição de acesso na tela de login.
  - Nova aba "Minha Senha" na tela de Configurações (`Configuracoes.tsx`) com formulário padronizado.
  - Diálogo e ação "Esqueci minha senha" na tela de login (`Acesso.tsx`), seguindo o Design System.
- **Edição e Atualização Parcial de Usuários:**
  - Endpoint `PATCH /api/v1/usuarios/{id}` no backend via `AtualizarUsuarioRequest` (`username` e `enabled`).
  - Modal de edição de usuário com formulário em `Card variant="outlined"` e campos compactos `size="small"`.
- **Governança de Living Documentation:**
  - Novas regras `RN-IAM-06` (Pureza de IAM e expurgo de dados pastorais), `RN-IAM-07` (Alçada exclusiva de ADMIN e PAROCO para edição e inativação) e `RN-IAM-08` (Fluxos de senha) em `docs/business-rules/iam.md`.

### Modificado
- **Pureza do Bounded Context IAM e Segregação de Dados Pessoais/Pastorais:**
  - Expurgo dos atributos de contato e sacramentais (`telefone`, `dataNascimento`, `igrejaId`, `igrejaNome`, `batizado`, `primeiraComunhao`, `crismado`, `casadoIgrejaCatolica`) de `UsuarioResponse`.
  - Desacoplamento dos use cases `ListarUsuariosUseCase` e `ObterUsuarioUseCase` de repositórios pastorais e de organizações; apenas o `nome` do fiel ou clérigo vinculado é recuperado para rótulo de identificação.
  - Tabela de gerenciamento de usuários em `Configuracoes.tsx` reestruturada para 4 colunas limpas: Nome, E-mail / Username, Status e Ações.
- **Restrição de Alçadas Canônicas em Usuários:**
  - Remoção de privilégios de `SECRETARIA_PAROQUIAL` no `UsuarioAuthorizationService` para ações administrativas de contas.
  - Proteção declarativa restrita a `ADMIN` e `PAROCO` no endpoint `PATCH /api/v1/usuarios/{id}` e `PATCH /api/v1/usuarios-admin/{id}/status`.
  - Frontend condicionado via helper `canEditOrInactivateUser(user)`: opções de "Editar" e "Inativar/Ativar" são omitidas para usuários com papel de Secretaria.
- **Padronização Visual no Design System:**
  - Modais de cadastro e edição de usuário em `Configuracoes.tsx` convertidos para o padrão estrito com `size="small"`, `Card variant="outlined" sx={{ borderRadius: 2 }}` e `DialogContent dividers sx={DIALOG_CONTENT_SCROLL_SX}`.
  - Confirmação de inativação/ativação encapsulada em diálogo modal nativo.


### Adicionado
- **Fluxo de Criação e Homologação de Usuários por Alçadas Canônicas:**
  - Habilitação dos papéis `PAROCO` e `SECRETARIA_PAROQUIAL` no cadastro de usuários (`POST /api/v1/usuarios`).
  - Contas criadas por `ADMIN` e `PAROCO` nascem ativas (`enabled = true`) e podem ser vinculadas a fiéis ou clérigos.
  - Contas criadas por `SECRETARIA_PAROQUIAL` nascem pendentes de homologação (`enabled = false`) e vinculadas exclusivamente a fiéis leigos.
  - Disparo de notificação persistente (`tipo = "USUARIO_APROVACAO_PENDENTE"`) para todos os Párocos e Administradores ativos após submissão de cadastro por secretaria.
  - Nova aba "Solicitações de Usuários" na tela de Aprovações (`/aprovacoes`), restrita a `ADMIN` e `PAROCO`, permitindo homologar cadastros e liberar acesso com feedback em tempo real.
  - Regra canônica formalizada em `docs/business-rules/iam.md` (`RN-IAM-05`) e `docs/business-rules/aprovacao.md` (`RN-APR-04`).

### Corrigido
- **Interoperabilidade de Vínculos Organizacionais (`grupoParoquialId` e `organizacaoId`):**
  - Adicionado alias `@JsonAlias({"organizacaoId", "grupoParoquialId"})` no record component `grupoParoquialId` de `AdicionarVinculoRequest` no backend, eliminando o erro `400 Bad Request` (`Unknown field 'organizacaoId'`).
  - Alinhamento do frontend (`usuarios.ts`, `OrganizacaoMembrosModal.tsx` e `Configuracoes.tsx`) para enviar transparentemente tanto `grupoParoquialId` quanto `organizacaoId`.
- **Autenticação e Escopo de Acesso de Novos Usuários:**
  - Atribuição automática do papel `ROLE_FIEL` no use case `CriarUsuarioUseCase` para contas associadas a fiéis leigos e `ROLE_CLERO` para clérigos.
  - Atualização do método `UsuarioEntity.aprovar` para garantir a atribuição de `ROLE_FIEL` ao homologar usuários.
  - Adequação do `EventoActorContextResolver` para reconhecer fiéis e usuários recém-criados que ainda não possuem vínculos em `membro_grupo`, resolvendo o escopo paroquial padrão (`PAROQUIA`) e evitando exceções `403 Forbidden` (`ROLE_SCOPE_INVALID`) no painel.

## [0.4.1] - 2026-09-30

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
