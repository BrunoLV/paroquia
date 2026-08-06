# Tracks Registry

---

- [x] **Track: necessário criar na ui a possibilidade de edição e cancelamento do evento conforme regras já estabelicidas na api**
*Link: [./archive/event_editing_cancellation_20260520/](./archive/event_editing_cancellation_20260520/)*

---

- [x] **Track: Implementação da tela de cadastro de evento a partir da tela de agendamento**
*Link: [./archive/event_registration_20260520/](./archive/event_registration_20260520/)*

---

- [x] **Track: Criar tela de dashboard do sistema. Será a tela inicial do sistema após login. Atualmente é tela de calendario, mas agora será a tela de dashboard. A Tela de Calendario será acessada normalmente via menu.**
*Link: [./archive/dashboard_screen_20260521/](./archive/dashboard_screen_20260521/)*

---

- [x] **Track: Customização da identidade visual e sistema de cores da Paróquia Nossa Senhora de Fátima**
*Link: [./archive/visual_theme_ns_fatima_20260521/](./archive/visual_theme_ns_fatima_20260521/)*

---

- [x] **Track: No login o sistema deve ter carregado as organizações as quais o usuario logado faz parte para que as informações sejam carregadas corretamente. Ponto importante que ADMIN pode ver tudo e fazer tudo no sistema assim como as demais ROLES que possuem esse permissionamento. Para as demais é necessário que o organização seja carregada na sessão também para que as regras de permissão e apresentação nas telas sejam devidamente respeitadas.**
*Link: [./archive/user_org_context_20260521/](./archive/user_org_context_20260521/)*

---

- [x] **Track: Implementar o model de cadastro de Evento com o restante dos campos existentes no modelo de dados conforme regras já estabelecidas no backend.**
*Link: [./archive/expand_event_model_20260523/](./archive/expand_event_model_20260523/)*

---

- [x] **Track: Corrigir bugs no modal de edição e exibição de detalhes do evento relacionados aos campos recém-adicionados (local, projeto, participantes).**
*Link: [./archive/fix_event_modals_20260524/](./archive/fix_event_modals_20260524/)*

---

- [x] **Track: Implementar a recorrencia dos eventos na UI conforme regra definida no backend**
*Link: [./archive/event_recurrence_20260524/](./archive/event_recurrence_20260524/)*

---

- [x] **Track: Implementação do fluxo de aprovação na UI conforme implementação definida pelo backend**
*Link: [./archive/approval_workflow_ui_20260525/](./archive/approval_workflow_ui_20260525/)*

---

- [x] **Track: expor a funcionalidade de gerencia de projetos no sistema/ui. A api já possui as regras implementadas. Analise o que já esta implementado para que eu possa validar e sugerir algum ajuste e implementar a tela para a funcionalidade.**
*Link: [./archive/project_management_ui_20260530/](./archive/project_management_ui_20260530/)*

---

- [x] **Track: Melhorar a apresentação dos erros tanto via api como na tela. Os erros são apresentados de uma maneiro muito genérica em termos de texto sobre o ocorrido. Melhorar esse detalhamento para facilitar o entendimento dos erros.**
*Link: [./archive/error_handling_refinement_20260531/](./archive/error_handling_refinement_20260531/)*

---

- [x] **Track: padronizar a apresentacao dos erros e validacao em todas as telas do sistema. Padronzar tambem os formularios, fontes e icones, assim como botões e links.**
*Link: [./archive/ui_standardization_20260530/](./archive/ui_standardization_20260530/)*

---

- [x] **Track: Padronização do layout da UI para estilo quadrado/angulado (0px border-radius) em todos os componentes**
*Link: [./archive/square_ui_layout_20260531/](./archive/square_ui_layout_20260531/)*

---

- [x] **Track: Ajuste fino da paleta de cores Marianas (Azul, Ouro, Vermelho Carmim) de Nossa Senhora de Fátima**
*Link: [./archive/visual_theme_fatima_colors_20260531/](./archive/visual_theme_fatima_colors_20260531/)*

---

- [x] **Track: implementar regra de cadastro vinculado a projeto, onde o evento só pode ser vinculado se o mesmo estiver definido dentro do periodo definido no projeto.**
*Link: [./archive/event_project_date_validation_20260531/](./archive/event_project_date_validation_20260531/)*

---

- [x] **Track: os erros de validação de regras, campos e informações são mostrados de uma maneira na tela de cadastro de eventos e de outra forma nas demais telas do sistmea. Padronização a apresentação de mensagens informativas e de erro no sistema para ser seguido o mesmo padrão em TODAS as telas e fluxos do sistema.**
*Link: [./archive/ui_error_presentation_standardization_20260531/](./archive/ui_error_presentation_standardization_20260531/)*

---

- [x] **Track: Implementar a funcionalidade relacionada ao ano paroquial.**
*Link: [./archive/ano_paroquial_gestao_20260531/](./archive/ano_paroquial_gestao_20260531/)*

---

- [x] **Track: Modernização da stack do backend para Java 25, Spring Boot 3 e atualização de dependências**
*Link: [./archive/java_modernization_20260601/](./archive/java_modernization_20260601/)*

---

- [x] **Track: Controle de status, fluxos de aprovação e governança de Projetos vinculados a anos paroquiais.**
*Link: [./archive/projeto_governanca_status_20260601/](./archive/projeto_governanca_status_20260601/)*

---

- [x] **Track: em gestao-paroquia nota-se no codigo o uso de inline fqn ao inves de import normal, isso nao e desejavel, ajuste o codigo.**
*Link: [./archive/clean_fqn_imports_20260602/](./archive/clean_fqn_imports_20260602/)*

---

- [x] **Track: Refactor ExportacaoCalendarioUseCase to separate export formats (PDF, Excel, CSV) via Strategy Pattern, and dynamically configure headers/footers/colors/logo from database configurations.**
*Link: [./archive/refactor_export_usecase_20260602/](./archive/refactor_export_usecase_20260602/)*

---

- [x] **Track: Refatoração e realocação de classes Exporter para os pacotes de infraestrutura adequados**
*Link: [./archive/relocate_exporters_20260603/](./archive/relocate_exporters_20260603/)*

---

- [x] **Track: revisar o codigo e estrutra de pacotes para ficar condizente com os padrões de DDD e Hexagonal Architecture, SOLID e Clean Archtecture**
*Link: [./archive/align_architecture_packages_20260603/](./archive/align_architecture_packages_20260603/)*

---

- [x] **Track: a funcionalidade de dashboard, hoje esta muito pobre, precisamos inserir informações mais gerenciais no dashboard, como graficos, eventos.**
*Link: [./archive/rich_dashboard_metrics_20260613/](./archive/rich_dashboard_metrics_20260613/)*

---

- [x] **Track: Implementar a funcionalidade de adicionar, editar e remover observações dos eventos conforme já implementado na api.**
*Link: [./archive/event_observations_management_20260613/](./archive/event_observations_management_20260613/)*

---

- [x] **Track: CRUD completo de cadastro, edição, exclusão e listagem de Locais/Espaços Paroquiais**
*Link: [./archive/locais_crud_20260616/](./archive/locais_crud_20260616/)*

---

- [x] **Track: Implementar funcionalidade de cadastro de pastorais e movimentos, incluindo coordenador e vice-coordenador se existir**
*Link: [./archive/pastorais_movimentos_20260627/](./archive/pastorais_movimentos_20260627/)*

---

- [x] **Track: Implementar o cadastro de usuários no sistema com criptografia de dados sensíveis, tela de registro público no login, fluxo de aprovação e regras de governança e vínculos de coordenação.**
*Link: [./archive/cadastro_usuarios_20260627/](./archive/cadastro_usuarios_20260627/)*

---

- [x] **Track: Implementar a funcionalidade de visualização da estrutura paroquial (organograma).**
*Link: [./archive/organograma_estrutura_20260627/](./archive/organograma_estrutura_20260627/)*

---

- [x] **Track: Hierarquia do Organograma Paroquial com Clero no topo, Conselhos Pastoral e Economico abaixo, e Pastorais subordinadas ao Conselho Pastoral**
*Link: [./archive/organograma_hierarquia_clero_20260628/](./archive/organograma_hierarquia_clero_20260628/)*

---

- [x] **Track: implementar o plano matriz_comunidades_plan.md**
*Link: [./archive/matriz_comunidades_20260629/](./archive/matriz_comunidades_20260629/)*

---

- [x] **Track: fix o problema de layout da tela de cadastro de usuário onde onde não é possível fazer a rolagem da tela**
*Link: [./archive/cadastro_rolagem_layout_20260629/](./archive/cadastro_rolagem_layout_20260629/)*

---

- [x] **Track: necessario ajustar o conceito de Pastora, membros e coordenadores (scoping por paroquia e comunidades)**
*Link: [./archive/pastoral_comunidades_concept_20260701/](./archive/pastoral_comunidades_concept_20260701/)*

---

- [x] **Track: Implementar mudança de hierarquia (Clero, Paróquia/Comunidades, Conselhos e Pastorais)**
*Link: [./archive/organizational_hierarchy_alignment_20260704/](./archive/organizational_hierarchy_alignment_20260704/)*

---

- [x] **Track: Registro de Tipos Pastorais e Movimentos Oficiais**
*Link: [./archive/pastorais_movimentos_oficiais_20260720/](./archive/pastorais_movimentos_oficiais_20260720/)*

---

- [x] **Track: Sistema de Autorização, Papéis e Alcance de Permissões**
*Link: [./archive/sistema_autorizacao_papeis_20260721/](./archive/sistema_autorizacao_papeis_20260721/)*

---

- [x] **Track: Vínculos de Usuários/Membros, Igrejas, Organizações e Papéis**
*Link: [./archive/vinculo_usuarios_igreja_papeis_20260721/](./archive/vinculo_usuarios_igreja_papeis_20260721/)*

---

- [x] **Track: Refatoração do Modelo de Usuário, Vínculos com Organizações e Seed Docker**
*Link: [./archive/refatoracao_usuario_vinculos_seed_20260721/](./archive/refatoracao_usuario_vinculos_seed_20260721/)*

---

- [x] **Track: Atualização dos Arquivos Docker, Validação de Vínculo por Igreja e Seed Data**
*Link: [./archive/docker_seed_vinculo_igreja_20260721/](./archive/docker_seed_vinculo_igreja_20260721/)*

---

- [x] **Track: Padronização do Código e Documentação segundo os Padrões AGENTS.md**
*Link: [./archive/padronizacao_agents_20260721/](./archive/padronizacao_agents_20260721/)*

---

- [x] **Track: Vínculos de Membros com Organizações (Consistência Bidirecional e Sincronismo de Liderança)**
*Link: [./archive/vinculo_membros_organizacoes_20260721/](./archive/vinculo_membros_organizacoes_20260721/)*

---

- [x] **Track: Melhorias Visuais e Responsivas nos Dashboards e Calendário do Frontend**
*Link: [./archive/dashboard_calendar_ui_20260721/](./archive/dashboard_calendar_ui_20260721/)*

---

- [x] **Track: Cadastros do Sistema via Importação de Arquivos CSV e Excel (XLS/XLSX)**
*Link: [./archive/importacao_arquivos_20260721/](./archive/importacao_arquivos_20260721/)*

---

- [x] **Track: padronização e modernização de layout da ui, botoes, mensagens, formularios e alertas.**
*Link: [./archive/padronizacao_ui_modernizacao_20260722/](./archive/padronizacao_ui_modernizacao_20260722/)*

---

- [x] **Track: Unificação do conceito de Membro e Usuário (Membro como vínculo de Usuário com Organização e Papel)**
*Link: [./archive/unificacao_membro_usuario_20260722/](./archive/unificacao_membro_usuario_20260722/)*

---

- [x] **Track: Implementar a funcionalidade de importação do organograma para imagem para download**
*Link: [./archive/organograma_export_imagem_20260725/](./archive/organograma_export_imagem_20260725/)*

---

- [x] **Track: Implementar Tela de Relatórios Gerenciais**
*Link: [./archive/relatorios_gerenciais_20260726/](./archive/relatorios_gerenciais_20260726/)*

---

- [x] **Track: Migração do Frontend para Vite & Vitest**
*Link: [./archive/vite_migration_20260726/](./archive/vite_migration_20260726/)*

---

- [x] **Track: Integração do @dabeng/react-orgchart no Sistema de Organograma**

*Link: [./archive/react_orgchart_integration_20260727/](./archive/react_orgchart_integration_20260727/)*

---

- [x] **Track: RBAC Eclesial e Governança Paroquial (CPP & CAE)**
*Link: [./archive/rbac_eclesial_governanca_20260727/](./archive/rbac_eclesial_governanca_20260727/)*

---

- [x] **Track: Reformulação do RBAC e Eliminação de Papéis Genéricos**
*Link: [./archive/rbac_especifico_eclesial_20260728/](./archive/rbac_especifico_eclesial_20260728/)*

---

- [x] **Track: Escopo Territorial e Vínculo Obrigatório (community_id)**
*Link: [./archive/escopo_territorial_community_id_20260728/](./archive/escopo_territorial_community_id_20260728/)*

---

- [x] **Track: Governança dos Conselhos (CPP e CPC) e Homologação de Atas**
*Link: [./archive/governanca_conselhos_cpp_cpc_20260728/](./archive/governanca_conselhos_cpp_cpc_20260728/)*

---

- [x] **Track: Motor de Validação da Agenda Comunitária (Níveis de Prioridade 1-4 & Bypass)**
*Link: [./archive/motor_agenda_comunitaria_prioridades_20260728/](./archive/motor_agenda_comunitaria_prioridades_20260728/)*

---

- [x] **Track: Auditoria, Consistência e Alinhamento do Cadastro de Eventos, Projetos e Recorrência (API x UI)**
*Link: [./archive/alinhamento_eventos_projetos_recorrencia_20260730/](./archive/alinhamento_eventos_projetos_recorrencia_20260730/)*

---

- [x] **Track: Vinculação de Locais (Espaços Paroquiais) a Matriz/Comunidades na API, UI e Banco de Dados (com ajuste de seed)**
*Link: [./archive/vinculo_locais_matriz_comunidades_20260730/](./archive/vinculo_locais_matriz_comunidades_20260730/)*

---

- [x] **Track: Otimização de Arquitetura Identificada pelo Grafo (Segregação de God Nodes)**
*Link: [./archive/segregacao_repositorios_god_nodes_20260730/](./archive/segregacao_repositorios_god_nodes_20260730/)*

---

- [x] **Track: Métricas Estratégicas e Preditivas de Gestão Paroquial (Sazonalidade Litúrgica, SLA de Despacho, Taxa de Projetos, Impacto Social, Concorrência e Ociosidade Patrimonial)**
*Link: [./archive/metricas_estrategicas_paroquiais_20260730/](./archive/metricas_estrategicas_paroquiais_20260730/)*

---

- [x] **Track: Motor de Governança Temporal e Sazonalidade Litúrgica (Ano Paroquial, Ano Litúrgico, Ciclos Litúrgicos 10 anos, Lock Policy e Importação Silenciosa)**
*Link: [./archive/motor_governanca_temporal_liturgica_20260730/](./archive/motor_governanca_temporal_liturgica_20260730/)*

---

- [x] **Track: Identificação Visual do Tempo Litúrgico no Calendário**
*Link: [./archive/liturgical_calendar_visual_20260731/](./archive/liturgical_calendar_visual_20260731/)*

---

- [x] **Track: Reestruturação do Motor Litúrgico Universal, Precedência, Governança Eclesial e UI**
*Link: [./archive/motor_liturgico_universal_eclesial_20260731/](./archive/motor_liturgico_universal_eclesial_20260731/)*

---

- [x] **Track: Modernização de Gráficos e Visualizações (ApexCharts, Recharts)**
*Link: [./archive/modernizacao_graficos_visualizacoes_20260731/index.md](./archive/modernizacao_graficos_visualizacoes_20260731/index.md)*

---

- [x] **Track: Enforcement de Qualidade do Harness de Testes (CI/CD, JaCoCo Threshold, Performance Tests)**
*Link: [./archive/harness_quality_enforcement_20260803/](./archive/harness_quality_enforcement_20260803/)*

---

- [x] **Track: Correção de Inconsistências do Conductor (conductor.yml, tech-stack.md, product.md, residual de track)**
*Link: [./archive/conductor_config_fixes_20260804/](./archive/conductor_config_fixes_20260804/)*

---

- [x] **Track: Redução de Densidade Visual da UI (Tipografia, Espaçamento e Componentes, até 20%)**
*Link: [./archive/ui_densidade_visual_20260804/index.md](./archive/ui_densidade_visual_20260804/index.md)*

---

- [x] **Track: Padronização de Cor e Ícone por Ação nos Botões do Sistema**
*Link: [./archive/estilizacao_botoes_acao_20260804/index.md](./archive/estilizacao_botoes_acao_20260804/index.md)*




---



- [x] **Track: Alinhar Linguagem Ubíqua — separar UnidadeTerritorial de GrupoParoquial, renomear Organizacao/TipoOrganizacao — Track 5/7 da iniciativa SDD**
*Link: [./tracks/linguagem_ubiqua_dominio_20260804/index.md](./tracks/linguagem_ubiqua_dominio_20260804/index.md)*

---

- [x] **Track: Adaptar o frontend (gestao-paroquia-ui) aos novos contratos de Fiel/GrupoParoquial/IAM — Track 6/7 da iniciativa SDD**
*Link: [./tracks/fiel_ui_iam_frontend_20260804/index.md](./tracks/fiel_ui_iam_frontend_20260804/index.md)*

---

- [~] **Track: Regressão final, cenários BDD da spec original e fechamento da iniciativa SDD — Track 7/7 (última) da iniciativa SDD**
*Link: [./tracks/sdd_regressao_final_20260804/index.md](./tracks/sdd_regressao_final_20260804/index.md)*
