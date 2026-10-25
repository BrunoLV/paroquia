# 📚 Regras de Negócio e Fluxos do Sistema - Projeto Paróquia

Este diretório centraliza a documentação viva (*Living Documentation*) das regras de negócio, fluxos operacionais e invariantes de domínio implementados no **Projeto Paróquia**.

---

## 🏛️ Governança e Regra Mandatória de Atualização

1. **Documentação Viva (*Living Documentation*):**
   - Cada arquivo neste diretório reflete com exatidão o **último estado implementado e testado** no código-fonte (backend Java e frontend React).
   - **PROIBIDO CÓDIGO DESALINHADO COM A DOCUMENTAÇÃO:** Qualquer alteração, inclusão ou refatoração de funcionalidade que afete regras de negócio, validações, fluxos ou políticas de domínio **DEVE OBRIGATORIAMENTE** atualizar o respectivo documento antes da conclusão da tarefa.

2. **Normatividade em Tempo Presente (Zero Histórico):**
   - Os documentos deste diretório definem estritamente o que o sistema É e COMO ELE OPERA no momento atual.
   - **É EXPRESSAMENTE PROIBIDO relatar passado:** Não use termos como "o antigo indicador", "foi substituído", "anteriormente", nem descreva o comportamento de versões prévias ou refatorações.
   - O histórico de evolução, notas de lançamento e mudanças pretéritas pertencem com exclusividade ao `CHANGELOG.md` e ao histórico de commits do Git.

3. **Estrutura Padrão de cada Módulo:**
   - **Visão Geral e Propósito:** O que o módulo resolve na vida da Paróquia.
   - **Agregados, Entidades e Value Objects:** Modelagem do domínio e conceitos da Linguagem Ubíqua.
   - **Regras de Negócio Invariantes:** Validações, restrições e políticas de cálculo/decisão.
   - **Fluxos Operacionais e Casos de Uso:** Ciclos de vida, transições de estado e esteiras de processo.
   - **Integração e Bounded Contexts Relacionados:** Comunicação com outros módulos (APIs internas, contratos e eventos).
   - **Privacidade & Segurança (LGPD e RBAC):** Níveis de permissão e restrições de dados civis/sensíveis.


---

## 🧭 Catálogo de Bounded Contexts e Documentos de Regras

| Módulo / Bounded Context | Arquivo de Regras | Responsabilidade Principal |
| :--- | :--- | :--- |
| **Aprovação** | [`aprovacao.md`](./aprovacao.md) | Esteira de aprovação de eventos, alterações e cancelamentos com alçadas e prioridades. |
| **Calendário** | [`calendario.md`](./calendario.md) | Agendamento paroquial, integração litúrgica (Meeus/Jones/Butcher), choque de horários e recorrência. |
| **Clero** | [`clero.md`](./clero.md) | Governança eclesiástica, provisões canônicas, pároco, vigários, bispo e paridade LGPD. |
| **Conselho** | [`conselho.md`](./conselho.md) | Atas de reunião por tipo (CPP, CPC, CAEP, assembleias e demais), deliberações, quórum, homologação do Pároco, ata retificadora, exportação em Word e PDF e trilha de auditoria. |
| **Fiel** | [`fiel.md`](./fiel.md) | Cadastro civil, vida sacramental, vocacional, consentimento LGPD e blindagem de dados sensíveis. |
| **Governança** | [`governanca.md`](./governanca.md) | Auditoria automática de entidades (`BaseAuditableEntity`), trilhas de auditoria e conformidade. |
| **IAM (Segurança)** | [`iam.md`](./iam.md) | Autenticação JWT, usuários, papéis (RBAC), credenciais e políticas de acesso. |
| **Importação & Exportação** | [`importacao.md`](./importacao.md) | Carga em lote de fiéis e clero via planilha, relatórios e apresentações litúrgicas (a exportação de atas é do módulo Conselho). |
| **LGPD & Privacidade** | [`lgpd.md`](./lgpd.md) | Diretrizes transversais da LGPD, direitos dos titulares, portabilidade, canal do DPO e gestão de incidentes. |
| **Local** | [`local.md`](./local.md) | Espaços físicos da matriz e comunidades, capacidade, recursos fixos e controle de ocupação. |
| **Membro** | [`membro.md`](./membro.md) | Vínculo de fiéis com pastorais, movimentos e serviços, histórico de atuação e lideranças. |
| **Organização** | [`organizacao.md`](./organizacao.md) | Estrutura pastoral, setores, pastorais, movimentos, comunidades e territórios eclesiásticos. |
| **Painel (Dashboard)** | [`painel.md`](./painel.md) | Indicadores executivos, total de fiéis ativos, quadro descritivo de projetos em risco e timeline semanal. |
| **Patrimônio** | [`patrimonio.md`](./patrimonio.md) | Tombamento de bens móveis e imóveis, conservação, alocação de ativos em eventos e responsabilidade. |
| **Projeto** | [`projeto.md`](./projeto.md) | Gestão de projetos pastorais, metas, status, métricas de progresso e diagnósticos preditivos de risco. |
| **Reserva** | [`reserva.md`](./reserva.md) | Bloqueios temporários e conciliação de espaços paroquiais para eventos externos e comunitários. |
| **Sacramento** | [`sacramento.md`](./sacramento.md) | Assentos de Batismo, Confirmação, Matrimônio e Ordem com fé pública, vínculo tardio a fiel/clérigo, averbações, retificação, certidões e livro reservado. |
