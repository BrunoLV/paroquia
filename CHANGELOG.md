# Changelog - Projeto Paróquia

Todas as alterações notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado no [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/), e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

---

## [Unreleased]

### Adicionado
- **Conselhos e Atas como espinha dorsal das reuniões da paróquia (módulo `conselho`):**
  - **Agregado `AtaReuniao`** com itens de pauta, deliberações (parecer, votação consultiva e declaração de conflito de interesses), presença nominal ou por total de participantes, leitura e aprovação da ata anterior, quórum por maioria absoluta e numeração sequencial (`003/2026`) por órgão e ano atribuída ao selar.
  - **12 tipos de reunião** com regras próprias (convocante, homologação, quórum, presença, votação e redator): CPP, CPC, CAEP, reunião conjunta CPP e CAEP, reunião de pastoral, de coordenadores, assembleias paroquial e comunitária, organismo, planejamento pastoral, comissão especial e animadores. Base: Cân. 536 e 537 e Documento 100 da CNBB (nn. 291-294).
  - **Fluxo e papéis:** o redator conduz a ata até a submissão; a homologação e o veto (com motivo) são privativos do Pároco; os tipos sem homologação terminam em `REGISTRADA`. Ata selada é imutável (também por trigger no banco) e é corrigida por **ata retificadora**.
  - **Exportação em Word (.docx) e PDF**, nos layouts estruturado e narrativo (texto de Livro de Atas), da ata completa ou só do quadro de decisões, e **compilação do livro de atas** por órgão e período. Ata não selada sai como minuta com marca d'água.
  - **Auditoria:** trilha append-only por ata (autor, perfil, IP, estado antes e depois) que registra também cada exportação, replicada no log global de auditoria.
  - **Escopo de leitura** aplicado na consulta: clero e secretaria paroquial veem todas as atas; os demais, só as dos órgãos a que estão vinculados. `ADMIN` não acessa o conteúdo.
  - **Clero e presidência nata:** o clero (cadastrado como clérigo, não como fiel) pode constar na presença e declarar conflito de interesses; nos conselhos o Pároco já vem incluído como Presidente e conta como membro convocado e, presente, no quórum.
  - **Coordenador e vice-coordenador do conselho são membros:** o formulário da ata sugere a função (Coordenador(a) ou Vice-coordenador(a) do conselho) a partir do papel do vínculo.
  - **Papéis de vínculo por tipo de organização numa tabela única:** cadastro de membros, promoção, vínculo por usuário e mandato de coordenação recusam o papel que o tipo não admite; o calendário consulta a mesma tabela; ao encerrar o mandato o vínculo volta ao papel de membro do tipo (no conselho, conforme o subtipo); e o seletor de papéis da interface mostra só os válidos.
  - **Consistência entre tipo de reunião e órgão:** a reunião conjunta exige o CAEP da mesma unidade do CPP; a compatibilidade é reconferida ao editar, submeter e homologar; e a **migração `V034`** traz um gatilho que impede gravar ata de tipo incompatível com o órgão.
  - **Migração Flyway `V033`** (`ata_reuniao`, `ata_historico`, triggers de imutabilidade) e API em `/api/v1/conselhos/atas` com `ConselhoExceptionHandler` (404, 409 e 422 em vez de 500).
  - **Frontend:** nova tela "Atas e decisões dos conselhos" com filtros, paginação no servidor, redação do rascunho por tipo, detalhe com trilha, veto, retificação e menu de exportação; ações exibidas conforme `ata.acoes`, calculadas pelo servidor.
  - Documentação em [`docs/business-rules/conselho.md`](docs/business-rules/conselho.md).

### Alterado
- `Content-Disposition` passa a ser exposto no CORS para o download usar o nome sugerido pelo servidor.
- O download de arquivos no frontend passa a usar o utilitário `shared/utils/downloadBlob` (libera a URL temporária).

### Removido
- Registro simples de decisões de conselho (`CpcDecisao`, `/api/v1/conselhos/decisoes` e a tabela `calendario.cpc_decisoes`), substituído pelas atas. A tabela continha apenas dados de teste.

## [0.11.0] - 2026-10-24

### Adicionado
- **Readequação Integral da LGPD & Governança de Privacidade (Lei 13.709/2018):**
  - **Skill e Living Documentation:** Readequação da skill [`.agents/skills/lgpd-privacy-by-design/SKILL.md`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.agents/skills/lgpd-privacy-by-design/SKILL.md) incorporando os 10 princípios fundamentais (Art. 6º), 5 fases do ciclo de vida dos dados pessoais, 20 ações de tratamento e salvaguarda canônica (Art. 16, I). Criação de [`docs/business-rules/lgpd.md`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/docs/business-rules/lgpd.md) e catalogação em [`docs/business-rules/README.md`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/docs/business-rules/README.md).
  - **Migração Flyway `V031`:** Criação da tabela `calendario.incidentes_seguranca` e adição dos canais de contato do Encarregado de Dados (DPO) em `calendario.paroquia_configuracao` com comentários DDL completos e constraints.
  - **Backend Governança & DPO:** Endpoint `PUT /api/v1/configuracao-paroquia/dpo` e use case `AtualizarDpoUseCase` para manutenção dos dados institucionais do Encarregado pelo tratamento de dados pessoais (Art. 41).
  - **Gestão de Incidentes de Segurança (Art. 48):** Endpoints `POST`, `GET` e `PATCH` em `IncidenteSegurancaController` com `GerenciarIncidenteSegurancaUseCase`, persistência `IncidenteSegurancaJpaRepository` e rastreamento de notificação à ANPD em até 3 dias úteis.
  - **Direitos do Titular (Portabilidade e Consentimento):**
    - Mapeamento explícito de consentimento LGPD nos endpoints de fiéis (`CriarFielUseCase`, `AtualizarFielUseCase`, `FielResponse`, `FielMapper`).
    - Endpoints de autoatendimento `GET /api/v1/meu-perfil/portabilidade` (download JSON legível por máquina, Art. 18, V) e `PATCH /api/v1/meu-perfil/preferencias-privacidade` (gestão e revogação de consentimento de imagem e aniversários, Art. 8º, § 5º).
  - **Frontend UI / Design System:**
    - Aba "Privacidade e DPO" em Configurações com formulário do DPO, tabela paginada com `SearchableTablePagination` (colSpan=6) e modais com `SearchableSelect`.
    - Diálogo público do DPO (`DpoPublicoDialog`) e rodapé institucional persistente no layout responsivo.
    - Modal de perfil e privacidade (`MeuPerfilPrivacidadeModal`) com ação de download de dados em arquivo JSON e switches de consentimento pastoral.
  - **Canal Portal de Autoatendimento sem Usuário & Auditoria Forense de Consentimento (Art. 8º, 9º, 14, 16):**
    - **Migração Flyway `V032`:** Paridade total com inclusão de colunas de consentimento em `calendario.clerigos`, salvaguarda de menor em `calendario.fieis`, e criação das tabelas `calendario.historico_consentimentos`, `calendario.desafios_consentimento` e `calendario.solicitacoes_dpo` com 100% de `COMMENT ON` DDL para auditoria e conformidade.
    - **Portal de Consentimento Passwordless (`/portal/consentimento`):** Concessão e revogação em 4 etapas seguras sem necessidade de criação de usuário/senha, validado por OTP de 6 dígitos numéricos via e-mail previamente cadastrado na secretaria, com rate limiting, hash SHA-256 e emissão de Comprovante Digital de Manifestação.
    - **Diálogo Canônico de Revogação:** Transparência ativa na revogação esclarecendo que a eliminação atinge comunicações, imagem e murais, preservando os assentos nos Livros Sacramentais paroquiais (Art. 16, I da LGPD e Cân. 535 do Código de Direito Canônico).
    - **Salvaguarda de Menores (Art. 14, § 1º):** Detecção automática de menores de 18 anos exigindo obrigatoriamente nome e CPF do responsável legal no cadastro (`FielCreateModal`, `FielEditModal`) e no portal.
    - **Trilha de Auditoria Forense Imutável:** Visualização tabular do histórico completo de manifestações com IP, canal, data/hora e hash do termo em `FielViewModal` e `ClerigoViewModal`.
    - **Registro de Operações de Tratamento - ROPA (Art. 37):** Inventário formal de 5 operações de tratamento canônico no painel administrativo com exportação em formato JSON interoperável e impressão.
    - **Canal de Atendimento ao DPO / DSAR (Arts. 18 e 19):** Protocolo público de requisições de titulares com prazo legal de resposta de até 15 dias, acompanhamento por protocolo e painel de moderação de solicitações pelo DPO.
    - **Página Pública de Política de Privacidade (`/politica-privacidade`):** Publicidade irrestrita das regras de privacidade, canais do DPO, finalidades, uso compartilhado diocesano e vedação comercial expressa (Art. 11, § 4º).


## [0.10.0] - 2026-10-07

### Adicionado
- **Reformulação Eclesial de Pastorais e Movimentos (módulo `organizacao`):**
  - **Tipologia:** pastoral orgânica, movimento apostólico, serviço paroquial (substitui `MINISTERIO`) e conselho, com policies de invariantes por tipo; múnus (ensinar, santificar, governar) obrigatório para pastorais e serviços.
  - **Articulação ao conselho pastoral:** pastorais, movimentos e serviços exigem como organização-mãe um conselho pastoral (CPP/CPC); novo `SubtipoConselho` (CPP, CPC, CAEP), e o CAEP não acolhe organismos pastorais.
  - **Estatuto de movimentos:** catálogo oficial com âmbito e reconhecimento de estatuto (Cân. 299 e 304); movimento sem estatuto reconhecido não é instanciado.
  - **Mandato de coordenação:** encargo temporário (máx. 3 anos) proposto pelo grupo/CPP e homologado, vetado ou encerrado pelo Pároco; job diário encerra mandatos vencidos; modal "Coordenação" na lista de organizações.
  - **Idoneidade e envio dos agentes:** formação, vivência sacramental atestada, termo de proteção de menores e envio; bloqueia proposta/homologação de mandato e promoção a coordenador com pendências; modal de idoneidade na lista de membros.
  - **Unidade de caixa:** regra de negócio documentada (organizações não têm personalidade jurídica nem caixa próprios).
  - **E-mail e telefone separados** no cadastro de organização (`contato` permanece como espelho de leitura).
  - **Dados de teste** do módulo no seed (movimentos, serviços, mandatos em todos os estados e idoneidade mista).
  - Migrações `V026` a `V030`.

### Modificado
- **Renomeação `GrupoParoquial` para `Organizacao`** em código, banco (`grupo_paroquial`, `grupo_paroquial_id`, `tipo_grupo`) e contrato REST (`grupoParoquialId` vira `organizacaoId`; `grupoParoquialResponsavelId` vira `organizacaoResponsavelId`).
- `coordenador` e `viceCoordenador` deixam de ser editáveis no cadastro de organização (espelho do mandato vigente); a sincronização de vínculos por texto livre foi removida.
- A tela de Pastorais e Movimentos lista somente organismos eclesiais; Matriz e Comunidade ficam em Governo & Território.
- Mensagens de validação e descrições OpenAPI do módulo `organizacao` passam para português.
- Documentação `docs/business-rules/organizacao.md` reescrita; `membro.md` e `conselho.md` atualizados.

### Observação de migração
- O ambiente Docker local usa o `app.jar` pré-compilado do host: recompile (`./gradlew :app:bootJar -x test`) antes de recriar `builder` e `api`, senão a API sobe no código antigo.
- Subtipo dos conselhos existentes inferido pelo nome (econômico = CAEP, comunitário = CPC, demais = CPP): revisar no cadastro. Lideranças existentes viram mandatos homologados de 1 ano a partir da migração: revisar os períodos. Pastorais e serviços existentes sem múnus precisam recebê-lo ao serem editados.

## [0.9.0] - 2026-10-06

### Modificado
- **Cores Semânticas em Ações de Tabela (`TableActionMenu`) e Botões:**
  - Padronização explícita de cores semânticas em itens do menu de ação de todas as tabelas:
    - Primária (`color="primary"`): ações de visualização/detalhes e edição (`VisibilityIcon`, `EditIcon`, `GroupsIcon`, `LinkIcon`).
    - Sucesso (`color="success"`): ações de aprovação, homologação, ativação e conclusão de exercício (`CheckCircleIcon`, `VerifiedIcon`, `ToggleOnIcon`).
    - Erro / Destrutivo (`color="error"`): ações de exclusão, inativação, veto e cancelamento (`DeleteIcon`, `BlockIcon`, `HighlightOffIcon`, `ToggleOffIcon`).
    - Informativo (`color="info"`): ações de emissão documental e fluxos de retirada/saída (`DescriptionIcon`, `OutboxIcon`).
    - Alerta / Atenção (`color="warning"`): envio para manutenção (`BuildIcon`).
- **Padronização do Clique na Linha para Visualização de Detalhes em Todas as Tabelas:**
  - Garantia de paridade em 100% das telas de tabela (`AtivosTable`, `LocaisTable`, `OrganizacoesTable`, `ReservasTable`, `Aprovacoes`, `VinculosPendentesPanel`, `AjustesCadastrais`, `Notificacoes`, `Clerigos`, `Fieis`, `Sacramentos`, `Projetos`, `UnidadesTerritoriais`) para que clicar em qualquer linha da tabela abra imediatamente o modal ou drawer de detalhes.
  - Implementação padronizada com acessibilidade completa: `role="button"`, `tabIndex={0}`, `sx={{ cursor: 'pointer' }}`, navegação por teclado (`Enter` / `Espaço`).
  - Proteção contra acionamento acidental: a célula do menu de ações contém `onClick={(e) => e.stopPropagation()}` em todas as tabelas.
  - Atualização dos testes unitários para validar a abertura de detalhes tanto pelo clique na linha quanto pelo menu de ações (`Ativos.test.tsx`).
- **Padronização da Seção de Pesquisa Externa ao Quadro da Tabela:**
  - Padronização em todas as telas de listagem (`Clerigos`, `Fieis`, `UnidadesTerritoriais`, `Reservas`, `Sacramentos`, `Projetos`, `AjustesCadastrais`, `Notificacoes`) para que a seção de pesquisa e filtros resida **sempre por fora** do quadro da tabela, em container `<Paper sx={{ p: 2, mb: 3 }}>` dedicado, utilizando o componente canônico `SearchField` (`size="small"`, busca textual integrada e ícone de lupa).
  - Tabela isolada em seu próprio `<TableContainer component={Paper}>`, eliminando anti-pattern de encapsular a pesquisa e a tabela dentro de um mesmo `<Card>` / `<CardContent>`.
  - Invariante formalizada no Design System (`gestao-paroquia-ui/DESIGN_SYSTEM.md`), regras de agentes (`.agents/rules/frontend-design-system.md`) e governança geral (`AGENTS.md`).
- **Revisão e Harmonização Semântica de Ícones (Menu, Telas, Formulários e Status):**
  - Eliminação de símbolos anacrônicos, profanos ou dúbios na interface, alinhando a representação visual com a dignidade canônica, litúrgica e pastoral da Igreja Católica.
  - Alinhamento 1:1 entre a iconografia da barra de navegação (`navigation.ts`) e os cabeçalhos de página (`PageHeader`):
    - Clero: `BadgeIcon` (ministros sagrados ordenados / estola-insígnia).
    - Pastorais e Movimentos: `VolunteerActivismIcon` (caridade e apostolado católico ativo).
    - Projetos: `AssignmentTurnedInIcon` (metas pastorais e entregas planejadas).
    - Sacramentos: `MenuBookIcon` (Sagrados Livros de Assentos Paroquiais).
    - Conselhos & Atas: `HistoryEduIcon` (lavratura colegiada de atas oficiais com pena histórica).
    - Patrimônio: `AccountBalanceIcon` (bens patrimoniais e fábrica da igreja).
    - Ajustes Cadastrais: `HowToRegIcon` (validação de registro cadastral do fiel).
  - Metadados de Status (`statusMeta` e `assentoMeta`):
    - Assentos: `DRAFT` utiliza `EditNoteIcon` (escrituração em rascunho) e `ORDEM` utiliza `WorkspacePremiumIcon` (ordenação sacerdotal/ordem sagrada).
    - Clero e Vocações: `PADRE_RELIGIOSO` e `RELIGIOSO` utilizam `ShieldIcon` (ordem religiosa / hábito / carisma consagrado); `EMERITO` e `SACERDOTE` utilizam `WorkspacePremiumIcon` (mérito e dignidade canônica jubilada).
    - Calendário: `SOLENIDADE` utiliza `FlareIcon` (resplendor litúrgico solene); `MOVIMENTO` utiliza `Diversity1Icon` (movimento eclesial comunitário).
    - Fiel: Data de aniversário de nascimento no modal utiliza `CakeIcon`.
  - Harmonização de Status e Informações do Mesmo Tipo:
    - Ciclo de vida e fluxos: `HourglassEmptyIcon` padronizado para pendências gerais e cadastrais (`situacaoStatus`, `assentoMeta`); `VerifiedIcon` para homologações canônicas oficiais (`homologacaoStatus`, `AssentoViewModal`); `TaskAltIcon` para finalização/devolução de reservas; `EventBusyIcon` para cancelamento de eventos na agenda.
    - Empréstimo e custódia de bens: substituição de `LocalShippingIcon` por `OutboxIcon` em `reservaStatus`, `ReservasTable` e `aprovacaoStatus` (`ALOCACAO_ATIVO_EMPRESTIMO`), alinhando com as métricas de reservas.
    - Sacramentos: Crisma/Confirmação padronizado com `WhatshotIcon` (fogo/chama de Pentecostes); Matrimônio com `Diversity1Icon` (aliança de amor mútuo e comunhão familiar indissolúvel) em `sacramentos` e `assentoMeta`.
    - Patrimônio: `InventoryIcon` substituído por `AccountBalanceIcon` em `AtivoViewModal` e `AtivoFormModal`.
    - Alçadas e Aprovações: `ALTERACAO_HORARIO` com `ScheduleIcon`, `RECLASSIFICACAO` com `CategoryIcon`, solicitações de projetos pastorais com `AssignmentTurnedInIcon`.
    - Eclesiásticos: `PADRE_DIOCESANO` com `BadgeIcon`; ordenações e graus da ordem unificados com `WorkspacePremiumIcon` em `ClerigoViewModal`.
- **Padronização Visual e Semântica de Botões e Cores de Ação:**
  - Botões de criação/cadastro (`primary`, `contained`) alinhados aos arquétipos de entidade: `PersonAddIcon` (Fiel, Clérigo, Usuário), `GroupAddIcon` (Pastoral, Tipo Oficial), `AddHomeWorkIcon` (Unidade Territorial), `AddLocationAltIcon` (Local/Espaço), `EventNoteIcon` (Evento de Calendário), `PostAddIcon` (Assento, Ata, Projeto).
  - Botões e ações de confirmação/gravação: `SaveIcon` padronizado em modais de criação/edição e formulários; `CheckCircleIcon` e `LoginIcon` para submissão e autenticação.
  - Botões de cancelamento/fechamento: `CloseIcon` com `color="secondary"` (`variant="outlined"` ou `"text"`).
  - Padronização de cores semânticas em botões normais e menus de ação de tabelas (`TableActionMenu`):
    - Ações destrutivas/restritivas com `color="error"`: `DeleteIcon` (exclusão), `ToggleOffIcon`/`PersonOffIcon` (inativação/desativação), `BlockIcon` (veto).
    - Ações de sucesso/homologação com `color="success"`: `CheckCircleIcon` (aprovação/conclusão), `VerifiedIcon` (homologação), `ToggleOnIcon` (ativação/reativação).
    - Ações neutras/informativas com cor primária/neutra padrão: `VisibilityIcon` (detalhes/visualizar), `EditIcon`/`EditNoteIcon` (edição/ressalva), `LinkIcon` (vínculos), `DownloadIcon` (exportação).
  - Atualização do Design System (`gestao-paroquia-ui/DESIGN_SYSTEM.md`) e regras de agentes (`.agents/rules/frontend-design-system.md`) para governança obrigatória nos próximos desenvolvimentos.
- **Revisão e Harmonização Canônica de Ícones para Tipo, Encargo e Grau:**
  - **Grau (Sacramento da Ordem & Vocações):**
    - Diaconato (`DIACONO_TRANSITORIO`, `DIACONO_PERMANENTE`, `DIACONO` vocacional): substituição de `VolunteerActivismIcon` (caridade leiga/pastoral) por `CoPresentIcon` (ministro sagrado ordenado para assistência no Altar e proclamação do Evangelho).
    - Presbiterado: `BadgeIcon` para Padre Diocesano (incardinado) e `ShieldIcon` para Padre Religioso (consagrado regular).
    - Episcopado: `WorkspacePremiumIcon` (3º grau e plenitude do sacramento da ordem).
    - Campo geral "Grau da ordem" padronizado com `WorkspacePremiumIcon`.
  - **Encargo (Ofícios Eclesiásticos & Funções Paroquiais):**
    - Campo geral de ofício/encargo canônico padronizado com `AssignmentIndIcon`.
    - Eliminação de ícones profanos/técnicos de TI em funções do clero:
      - Pároco: substituição de `AdminPanelSettingsIcon` (escudo de TI/firewall) por `ChurchIcon` (pastor titular e autoridade própria da Paróquia).
      - Administrador Paroquial: substituição de `ManageAccountsIcon` (engrenagem de TI) por `AssignmentIndIcon` (provisão e regência administrativa temporária).
      - Diácono Auxiliar: substituição de `VolunteerActivismIcon` por `CoPresentIcon` (assistência eclesial e litúrgica).
  - **Tipo (Categorização, Organismos, Locais e Ativos):**
    - Eliminação de `HelpOutlineIcon` ("?") para tipos ou categorias genéricas/não classificadas (`OUTRO`), substituído por `CategoryIcon` em `localTipoStatus`, `categoriaAtivoStatus` e `organismoTipoStatus`.
    - Sacramento em `VinculosPendentesPanel`: harmonização de `CategoryIcon` para `MenuBookIcon` (livros de assentos sacramentais).
    - Tipo de Participação em `VinculosPendentesPanel`: padronizado com `AssignmentIndIcon`.
    - Tipo de Aquisição do Solo em `UnidadeTerritorialViewModal`: substituição de `DescriptionIcon` por `ReceiptLongIcon` (título/escritura).
    - Tipo de Local em `LocalViewModal`: exibição canônica do chip `StatusIcon` com `getLocalTipoMeta(local.tipo)` no cabeçalho do diálogo.
  - Atualização canônica de governança no `gestao-paroquia-ui/DESIGN_SYSTEM.md` (Seção 6.2) e `.agents/rules/frontend-design-system.md` (Seção 1.3).

## [0.8.0] - 2026-10-04

### Adicionado
- **Configuração Institucional e Identidade Visual no Banco de Dados (`calendario.paroquia_configuracao`):**
  - Desacoplamento total dos dados institucionais e logos do `application.yml` (`app.paroquia` removido por completo).
  - Persistência binarizada (`bytea`) no PostgreSQL de logotipos para duas variantes de contraste: Fundo Claro (`CLARO`) e Fundo Escuro (`ESCURO`).
  - Semente inicial (*seed*) de logotipos e cores institucionais a partir dos novos arquivos de alta resolução enviados pelo usuário no classpath.
  - Endpoints REST públicos com cache HTTP (`max-age=86400`) para consulta institucional (`GET /api/v1/configuracao-paroquia`) e streaming dos logotipos (`GET /api/v1/configuracao-paroquia/logo/{variante}`).
  - Endpoints administrativos (`PUT /api/v1/configuracao-paroquia` e `PUT /api/v1/configuracao-paroquia/logo/{variante}`) com controle de acesso exclusivo para `PAROCO` e `ADMIN` (além de `BISPO_DIOCESANO`).
  - Single Source of Truth (SSOT): eliminação de redundâncias, derivando dados civis e de contato da Igreja Matriz (`UnidadeTerritorial`) e dados eclesiásticos da Diocese (`GovernoEclesiastico`) via porta hexagonal e ACL (`ConsultaInstitucionalAdapter`).
  - Frontend: nova aba "Identidade Visual" na tela de Configurações (`Configuracoes.tsx`) com visualização dos dados cadastrais (SSOT), componente visual `ColorPickerField` para seleção de cores primária e secundária, e cards `LogoUploadCard` com preview em contraste adequado e upload de arquivos.
  - Exibição dinâmica da identidade institucional no `Layout.tsx` e na tela de login (`Acesso.tsx`), respeitando o modo claro/escuro com alternador de tema e imagem de contraste adequado.
  - Sincronização do favicon do navegador obtido diretamente via serviço REST (`/api/v1/configuracao-paroquia/logo/{variante}.png`) através do hook `useDynamicFavicon` e `DynamicFavicon`, acompanhando automaticamente a preferência de esquema de cores do navegador (`prefers-color-scheme`) e forçando a recriação de nós no DOM para suporte pleno ao Chromium/Blink.
  - Documentação atualizada com `RN-GOV-05` (SSOT) e `RN-GOV-06` (Logotipos binarizados e dupla variante) em `docs/business-rules/governanca.md`.

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
