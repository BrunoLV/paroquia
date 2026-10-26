# 📋 Regras de Negócio & Funcionalidades Implementadas

Esta seção cataloga todas as regras operacionais, fluxos e funcionalidades implementadas no **Projeto Paróquia**, detalhando o comportamento de negócio nos 17 Bounded Contexts do backend e no frontend React.

---

## 🧭 Visão Geral dos Bounded Contexts

O backend do sistema é particionado nos seguintes domínios sob `br.com.nsfatima.gestao`:

```
br.com.nsfatima.gestao
├── calendario          <-- Agendamento, liturgia, precedência da agenda e recorrência
├── aprovacao           <-- Fila de aprovação de eventos e alterações
├── local               <-- Espaços físicos da matriz e comunidades
├── reserva             <-- Reserva de locais e de ativos, com retirada e devolução
├── patrimonio          <-- Ativos patrimoniais, tombamento, manutenção e exclusão lógica
├── conselho            <-- Conselhos (CPP, CPC, CAEP), atas, deliberações e homologações
├── organizacao         <-- Pastorais, movimentos, serviços, conselhos e mandatos de coordenação
├── fiel                <-- Dados cadastrais civis, sacramentais e vocacionais
├── clero               <-- Clérigos, encargos, governo eclesiástico e diocese
├── sacramento          <-- Assentos sacramentais com fé pública e averbações
├── membro              <-- Vínculo do fiel a organizações e idoneidade dos agentes
├── projeto             <-- Projetos paroquiais com período, vinculados a eventos
├── iam                 <-- Autenticação JWT, usuários, papéis e permissões
├── governanca          <-- Ano paroquial, consentimentos, incidentes de segurança e DPO
├── importacao          <-- Carga em lote (CSV e XLSX) de membros, organizações e eventos
├── dashboard           <-- Consolidação de métricas executivas
├── observabilidade     <-- Telemetria, healthchecks, auditoria e logs estruturados
└── support             <-- Tratamento global de exceções e abstrações comuns
```

---

## 1. Gestão de Eventos e Calendário (`calendario`)

### Funcionalidades
- **Agendamento Multifacetado:** Cadastro e edição de eventos litúrgicos (missas, celebrações, confissões), pastorais (reuniões, catequese, encontros), sociais (quermesses, festas comunitárias) e administrativos.
- **Integração Litúrgica:** Associação automática com o calendário litúrgico romano:
  - Detecção do tempo litúrgico corrente (Tempo Comum, Quaresma, Páscoa, Advento, Natal).
  - Consulta e exibição de solenidades e festas de preceito.
- **Recorrência Flexível:** Agendamento recorrente com periodicidade diária, semanal (com múltiplos dias da semana) e mensal.
- **Edição com Escopo de Recorrência:** Possibilidade de aplicar alterações apenas neste evento (`SOMENTE_ESTE`), neste e nos próximos (`ESTE_E_PROXIMOS`) ou em toda a série (`TODOS`).
- **Filtros Inteligentes:** Visualização mensal em grade (*grid*) ou lista corrida (*agenda*), com filtragem por organização responsável, local, status e tipo de celebração.

### Regras de Negócio Críticas
1. **Precedência e Choque de Agenda (`HierarquiaPrioridadeAgendaPolicy`):**
   - A agenda segue quatro níveis de prioridade por categoria (solenidade, litúrgico e sacramental, pastoral e afins, movimento): o evento de nível superior cancela os de nível inferior que se sobrepõem no mesmo espaço, e os de nível inferior não se impõem aos superiores.
   - A sobreposição no mesmo espaço e período é sinalizada no evento (estado de conflito `PENDENTE` com o motivo), e sua resolução cabe ao fluxo de aprovação.
2. **Submissão Extraordinária (`ADICIONADO_EXTRA`):**
   - Eventos criados com status `ADICIONADO_EXTRA` (adicionados fora do planejamento anual, com o ano litúrgico fechado) **exigem justificativa textual obrigatória** de negócio.
3. **Ativos do Evento:**
   - Eventos que precisam de ativos patrimoniais (som, projetor) os ocupam por **reserva**, que valida a disponibilidade do ativo no período exato.

---

## 2. Workflows de Aprovação (`aprovacao`)

### Funcionalidades
- **Esteira de Decisão:** centralização das solicitações pendentes de criação, edição, alteração de horário, cancelamento e reclassificação de eventos, entre outros tipos.
- **Pareceres e Histórico:** registro do histórico de aprovação e reprovação, com o autor da decisão, a observação e o instante UTC.
- **Execução da Decisão:** depois de aprovada, a ação é executada sobre o evento; a falha de execução fica registrada e, se for sistêmica, admite reexecução.

### Regras de Negócio Críticas
1. **Imutabilidade durante Análise:**
   - Enquanto um evento possui solicitação pendente de decisão (`aprovacaoPendenteId`), os botões de edição e cancelamento ficam desabilitados no calendário, para evitar colisões de estado.
2. **Alçada de Decisão:**
   - A decisão é privativa do Pároco (`AprovadorPapel.PAROCO`); conselhos e coordenadores propõem, mas não decidem.

O detalhamento está em `docs/business-rules/aprovacao.md`.

---

## 3. Gestão Patrimonial e Ativos (`patrimonio`)

### Funcionalidades
- **Inventário de Ativos:** cadastro de bens móveis com código de tombamento gerado pelo sistema (`PAT-AAAA-NNNN`), tipo (eletrônico, mobiliário, paramento ou infraestrutura), valor e data de aquisição, e a unidade territorial proprietária (Matriz ou Comunidade).
- **Reserva de Ativos:** o empréstimo do bem é feito por **reserva** (módulo `reserva`), com aprovação, retirada e devolução com conferência de avarias.
- **Manutenção:** envio do bem para manutenção por período determinado, com checagem prévia de reservas conflitantes, e conclusão que o devolve a `DISPONIVEL`.
- **Exclusão Lógica:** desincorporação do bem, recusada com manutenção em aberto ou reservas futuras.

### Regras de Negócio Críticas
1. **Imutabilidade Estrutural:**
   - Depois do cadastro, só a descrição muda; o código de tombamento é único, gerado pelo sistema e imutável. Não há transferência entre comunidades.
2. **Status:**
   - `DISPONIVEL`, `RESERVADO` e `EM_MANUTENCAO`; a manutenção tem precedência sobre a reserva e bloqueia novas reservas do período.

O detalhamento está em `docs/business-rules/patrimonio.md`.

---

## 4. Conselhos Paroquiais e Atas (`conselho`)

### Funcionalidades
- **Atas por tipo de reunião:** 12 tipos (CPP, CPC, CAEP, reunião conjunta, reunião de pastoral, coordenadores, assembleias, organismo, planejamento pastoral, comissão especial e animadores), cada um com suas regras de convocante, homologação, quórum, presença e votação.
- **Pautas e Deliberações:** itens de pauta com relato e deliberações, votação consultiva e declaração de conflito de interesses.
- **Homologação Paroquial:** nos tipos que a exigem, a ata é redigida pelo secretário e homologada ou vetada, com motivo, pelo Pároco.
- **Ata retificadora, exportação e trilha:** correção de ata selada por nova ata ligada à original; exportação em Word e PDF (estruturada ou narrativa) e trilha de auditoria de cada ação.

### Regras de Negócio Críticas
1. **Imutabilidade Pós-Selagem:**
   - Ata homologada ou registrada é estritamente imutável e nunca é excluída, para garantir fé pública eclesiástica.
2. **Quórum Informativo:**
   - Nos colegiados, o quórum é a maioria absoluta dos membros ativos mais o Pároco; consta da ata, mas não impede a lavratura.
3. **Segregação:**
   - O redator (por órgão) não homologa, e o Pároco não redige.

O detalhamento está em `docs/business-rules/conselho.md`.

---

## 5. Estrutura Organizacional (`organizacao`)

### Funcionalidades
- **Árvore de Organismos:** Modelagem hierárquica das organizações eclesiais:
  - Comunidade Matriz e Capelas.
  - Setores Pastorais (Liturgia, Catequese, Ação Social, Família, Juventude).
  - Pastorais e Movimentos específicos (ex: Pastoral da Criança, ECC, Vicentinos, RCC).
- **Vínculos Territoriais:** Associação de pastorais às capelas onde desempenham suas atividades.

---

## 6. Fiéis e Membros (`fiel` e `membro`)

### Funcionalidades
- **Ficha Cadastral do Fiel:** Cadastro unificado de pessoas físicas com dados civis e de contato.
- **Histórico Sacramental:** Registro canônico de sacramentos recebidos (Batismo, Primeira Comunhão, Crisma, Matrimônio, Ordem).
- **Vínculo Pastoral (Membro):** Associação do fiel a um ou mais grupos paroquiais, com papel específico (Membro Ativo, Voluntário, Coordenador e Secretário de Grupo, que é também o papel do Vice-Coordenador; nos conselhos, Secretário e Membro do Conselho).
- **Gestão de Mandatos:** a coordenação é um mandato temporário, homologado pelo Pároco e encerrado automaticamente quando vence.

### Regras de Negócio Críticas (LGPD)
1. **Paridade Clérigo/Fiel:**
   - Clérigos (Pároco, Vigários, Diáconos) e leigos possuem rigorosamente o mesmo nível de proteção de privacidade nos seus dados civis.
2. **Segregação de Contratos REST:**
   - Endpoints públicos/administrativos não expõem campos sensíveis (como CPF ou endereço completo) a usuários sem a permissão `SECRETARIA` ou `ADMIN`.
3. **Criptografia em Repouso:**
   - Documentos e identificadores civis são persistidos com proteção criptográfica.

---

## 7. Espaços Físicos e Locais (`local`)

### Funcionalidades
- **Cadastro de Ambientes:** local com nome, tipo (salão paroquial, igreja, capela, sala de reunião, auditório, quadra, secretaria ou outro), endereço, capacidade máxima e descrição.
- **Vinculação:** cada local pertence a uma organização (igreja ou comunidade) da paróquia.
- **Integridade:** o local com eventos futuros não é inativado nem excluído.

---

## 8. Identidade e Acesso (`iam`)

### Funcionalidades
- **Autenticação Segura:** Autenticação via JSON Web Tokens (JWT) com verificação de assinatura e expiração.
- **Matriz Ortogonal de 14 Papéis Canônicos em 3 Camadas:**
  - **Camada 1 — Papéis Globais (`PapelAcesso`):** `ADMIN` (infraestrutura e logs, isolado de decisões eclesiais), `BISPO_DIOCESANO`, `PAROCO`, `VIGARIO`, `DIACONO_PAROQUIAL`, `SECRETARIA_PAROQUIAL` e `FIEL`.
  - **Camada 2 — Vínculos de Conselho:** `SECRETARIO_CONSELHO`, `MEMBRO_CONSELHO_PASTORAL` (CPP/CPC) e `MEMBRO_CONSELHO_ECONOMICO` (CAEP).
  - **Camada 3 — Vínculos de Organização Pastoral:** `COORDENADOR`, `SECRETARIO_GRUPO` (acumula Vice-Coordenador), `MEMBRO_ATIVO` e `VOLUNTARIO`.
- **Proteção contra IDOR (*Insecure Direct Object References*):**
  - Usuários só podem visualizar ou manipular recursos dentro do escopo de organizações autorizadas.

O detalhamento está em `docs/business-rules/iam.md`.

---

## 9. Importação e Exportação de Relatórios (`importacao`)

### Funcionalidades
- **Importação em Lote:** carga de vínculos de membros, organizações e eventos a partir de CSV ou XLSX, em três passos (modelo, validação sem gravação e confirmação), com erros isolados por linha.
- **Exportações (nos módulos de origem):**
  - **Calendário:** grade e celebrações em PowerPoint, PDF, Excel e CSV.
  - **Atas de conselho:** Word (`.docx`, via Apache POI XWPF) e PDF (via OpenPDF), com cabeçalho institucional.
  - **Relatórios do painel:** PDF e CSV, gerados na interface (jsPDF).

O detalhamento está em `docs/business-rules/importacao.md`.

---

## 10. Painel Executivo e Dashboard (`dashboard`)

### Funcionalidades
- **Widgets de Logística:** Alertas de choque de horários, eventos órfãos de organização e taxa de ocupação de ambientes.
- **Métricas Comunitárias:** Total de fiéis cadastrados, retenção pastoral, aniversariantes do mês e distribuição por tempo litúrgico.
- **Progresso de Projetos:** acompanhamento dos projetos ativos, com o indicador de risco de atraso.

O detalhamento está em `docs/business-rules/painel.md`.

---

## 11. Clero e Governo Eclesiástico (`clero`)

### Funcionalidades
- **Cadastro de Ministros Ordenados:** Registro próprio de clérigos (`DIACONO_TRANSITORIO`, `DIACONO_PERMANENTE`, `PADRE_DIOCESANO`, `PADRE_RELIGIOSO`, `BISPO`), datas de ordenação e diocese de incardinação.
- **Histórico de Encargos:** Controle de função eclesiástica atual (`PAROCO`, `VIGARIO_PAROQUIAL`, `ADMINISTRADOR_PAROQUIAL`, `DIACONO_AUXILIAR`, `BISPO_DIOCESANO`, `EMERITO`) com encerramento automático do encargo anterior ao assumir novo ofício.
- **Governo da Paróquia e Diocese:** Registro do decreto de ereção, data de posse do Pároco, livro de tombo e vínculo com a Mitra Diocesana.
- **Paridade LGPD:** Proteção criptográfica de dados civis, segregação entre DTO institucional e DTO completo, autoatendimento no Portal de Consentimento e trilha de auditoria forense.

O detalhamento está em `docs/business-rules/clero.md`.

---

## 12. Livros e Assentos Sacramentais (`sacramento`)

### Funcionalidades
- **Fé Pública Canônica Perpétua (Cân. 535):** Lavratura de assentos nos Livros Paroquiais (Batismo, Crisma, Matrimônio e Ordem) com numeração por livro, folha e termo.
- **Imutabilidade e Averbações:** Assentos lavrados são imutáveis e jamais excluídos fisicamente (Art. 16, I da LGPD); retificações e anotações inter-sacramentais ocorrem exclusivamente por averbações formais datadas.
- **Reivindicação e Moderação de Vínculos:** Vinculação entre cadastros de fiéis e registros sacramentais sujeita à homologação na Central de Aprovações.

O detalhamento está em `docs/business-rules/sacramento.md`.

---

## 13. Reservas de Locais e Ativos (`reserva`)

### Funcionalidades
- **Ciclo de Vida de Empréstimo:** Solicitação, aprovação, retirada (`RETIRADO`) e devolução (`FINALIZADA`) de espaços físicos (`LOCAL`) e bens móveis (`ATIVO`).
- **Prevenção de Conflitos e Vistoria:** Bloqueio automático contra sobreposição de reservas confirmadas ou janelas de manutenção; registro de vistoria na devolução com abertura automática de manutenção em caso de avaria.

O detalhamento está em `docs/business-rules/reserva.md`.

---

## 14. Projetos Paroquiais (`projeto`)

### Funcionalidades
- **Planejamento Pastoral e Obras:** Cadastro de projetos com período de vigência, organização responsável e associação de eventos do calendário.
- **Indicador Preditivo de Risco:** Cálculo dinâmico de risco de atraso com base na proporção de eventos concluídos/confirmados frente ao progresso temporal do projeto.

O detalhamento está em `docs/business-rules/projeto.md`.

---

## 15. Governança, Ano Paroquial e Conformidade LGPD (`governanca` e `observabilidade`)

### Funcionalidades
- **Auditoria Universal (`BaseAuditableEntity`):** Rastreabilidade automática em UTC (`criado_em_utc`, `atualizado_em_utc`, `criado_por`, `atualizado_por`) e trilha de auditoria imutável.
- **Ciclo do Ano Paroquial e Identidade Visual:** Abertura/fechamento do Ano Paroquial pelo Pároco e configuração de cores e logotipos binarizados (fundo claro e escuro).
- **Portal de Consentimento Passwordless (`/portal/consentimento`):** Autoatendimento público para fiéis e clérigos via código OTP de 6 dígitos enviado por e-mail, diálogo de transparência canônica (Cân. 535), salvaguarda de menores (Art. 14, § 1º) e emissão de comprovante digital com hash SHA-256 do termo.
- **Painel do Encarregado (DPO), ROPA e Incidentes:** Inventário ROPA exportável (Art. 37), atendimento a requisições de titulares com protocolo e prazo legal de 15 dias (Arts. 18 e 19), gestão de incidentes de segurança (Art. 48) e página pública `/politica-privacidade`.
- **Observabilidade (`observabilidade`):** Telemetria via Spring Boot Actuator, métricas Prometheus, logs JSON assíncronos via Log4j2 Disruptor (Loki/Promtail) com garantia de PII Zero.

O detalhamento está em `docs/business-rules/governanca.md`, `docs/business-rules/lgpd.md` e `docs/guide/logging-metricas.md`.
