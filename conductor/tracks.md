# Tracks Registry

---

- [x] **Track: necessário criar na ui a possibilidade de edição e cancelamento do evento conforme regras já estabelicidas na api**
*Link: [./archive/event_editing_cancellation_20260520/](./archive/event_editing_cancellation_20260520/)*

---

- [x] **Track: Criar tela de dashboard do sistema. Será a tela inicial do sistema após login. Atualmente é tela de calendario, mas agora será a tela de dashboard. A Tela de Calendario será acessada normalmente via menu.**
*Link: [./archive/dashboard_screen_20260521/](./archive/dashboard_screen_20260521/)*

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

- [x] **Track: implementar regra de cadastro vinculado a projeto, onde o evento só pode ser vinculado se o mesmo estiver definido dentro do periodo definido no projeto.**
*Link: [./archive/event_project_date_validation_20260531/](./archive/event_project_date_validation_20260531/)*

---

- [x] **Track: os erros de validação de regras, campos e informações são mostrados de uma maneira na tela de cadastro de eventos e de outra forma nas demais telas do sistmea. Padronização a apresentação de mensagens informativas e de erro no sistema para ser seguido o mesmo padrão em TODAS as telas e fluxos do sistema.**
*Link: [./archive/ui_error_presentation_standardization_20260531/](./archive/ui_error_presentation_standardization_20260531/)*

---

- [x] **Track: Implementar a funcionalidade relacionada ao ano paroquial.**
*Link: [./archive/ano_paroquial_gestao_20260531/](./archive/ano_paroquial_gestao_20260531/)*

---

- [x] **Track: Controle de status, fluxos de aprovação e governança de Projetos vinculados a anos paroquiais.**
*Link: [./archive/projeto_governanca_status_20260601/](./archive/projeto_governanca_status_20260601/)*

---

- [x] **Track: em gestao-paroquia nota-se no codigo o uso de inline fqn ao inves de import normal, isso nao e desejavel, ajuste o codigo.**
*Link: [./tracks/clean_fqn_imports_20260602/](./tracks/clean_fqn_imports_20260602/)*

---

- [x] **Track: Refactor ExportacaoCalendarioUseCase to separate export formats (PDF, Excel, CSV) via Strategy Pattern, and dynamically configure headers/footers/colors/logo from database configurations.**
*Link: [./tracks/refactor_export_usecase_20260602/](./tracks/refactor_export_usecase_20260602/)*

---

- [x] **Track: classes Exporter em usecase/evento não casos de uso, sao utilitarios que servem apenas para gerar arquivos, dentro da arquitetura da app isso soa como bad smell. revise isso e ajuste os pacotes. necessário fazer essa analise no projeto como um todo**
*Link: [./tracks/relocate_exporters_20260603/](./tracks/relocate_exporters_20260603/)*

