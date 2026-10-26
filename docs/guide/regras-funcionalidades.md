# 📋 Regras de Negócio & Funcionalidades Implementadas

Esta seção cataloga todas as regras operacionais, fluxos e funcionalidades implementadas no **Projeto Paróquia**, detalhando o comportamento de negócio nos 15 Bounded Contexts do backend e no frontend React.

---

## 🧭 Visão Geral dos 15 Bounded Contexts

O backend do sistema é particionado nos seguintes domínios sob `br.com.nsfatima.gestao`:

```
br.com.nsfatima.gestao
├── calendario          <-- Agendamento, liturgia, choque de horários e recorrência
├── aprovacao           <-- Fila de aprovação de eventos e alterações
├── local               <-- Espaços físicos da matriz e comunidades
├── patrimonio          <-- Ativos patrimoniais, tombamento, conservação e alocações
├── conselho            <-- Conselhos (CPP, CPC, CAEP), atas, deliberações e homologações
├── organizacao         <-- Pastorais, movimentos, setores e comunidades
├── fiel                <-- Dados cadastrais civis, sacramentais e vocacionais
├── membro              <-- Vínculo do fiel a organizações e cargos de liderança
├── projeto             <-- Projetos paroquiais vinculados a eventos e metas
├── iam                 <-- Autenticação JWT, usuários, papéis e permissões
├── governanca          <-- Auditoria de alterações e políticas de segurança
├── importacao          <-- Carga e exportação em lote (Excel, PDF, Word)
├── dashboard           <-- Consolidação de métricas executivas
├── observabilidade     <-- Telemetria, healthchecks e logs estruturados
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
- **Edição com Escopo de Recorrência:** Possibilidade de aplicar alterações em `APENAS_ESTE` evento ou propagar para `TODOS_SEGUINTES` da série.
- **Filtros Inteligentes:** Visualização mensal em grade (*grid*) ou lista corrida (*agenda*), com filtragem por organização responsável, local, status e tipo de celebração.

### Regras de Negócio Críticas
1. **Política de Choque de Horários (`ChoqueHorarioPolicy`):**
   - Não é permitido agendar dois eventos simultâneos no mesmo local físico (`localId`), considerando intervalo de tolerância/preparação.
   - Detecção em tempo real na interface e validação atômica no backend antes de persistência.
2. **Submissão Extraordinária (`ADICIONADO_EXTRA`):**
   - Eventos criados com status `ADICIONADO_EXTRA` (adicionados fora do planejamento anual aprovado) **exigem justificativa textual obrigatória** de negócio.
3. **Bypass de Pároco:**
   - Usuários com o papel `PAROCO` possuem autorização expressa para forçar agendamento sobrepondo avisos de conflito não-bloqueantes.
4. **Alocação de Recursos/Ativos:**
   - Eventos podem requisitar ativos patrimoniais (ex: sistema de som, projetor). A disponibilidade dos ativos é validada para o período exato do evento.

---

## 2. Workflows de Aprovação (`aprovacao`)

### Funcionalidades
- **Esteira de Decisão:** Centralização de todas as solicitações pendentes de criação, edição e cancelamento de eventos.
- **Fila Diferenciada:** Visualização de solicitações urgentes com alertas visuais de proximidade da data do evento.
- **Pareceres e Histórico:** Registro completo do histórico de aprovação/rejeição, identificando o autor da decisão e o timestamp UTC.

### Regras de Negócio Críticas
1. **Justificativa Obrigatória para Rejeição/Cancelamento:**
   - Nenhuma solicitação pode ser reprovada ou cancelada sem o fornecimento de motivo justificado.
2. **Imutabilidade durante Análise:**
   - Enquanto um evento possui solicitação pendente de decisão (`aprovacaoPendenteId`), botões de edição e cancelamento ficam desabilitados no calendário para evitar colisões de estado.
3. **Alçadas de Decisão:**
   - Coordenadores de pastoral aprovam solicitações internas do seu próprio grupo.
   - O Pároco e a Secretaria Geral possuem alçada global de aprovação/homologação.

---

## 3. Gestão Patrimonial e Ativos (`patrimonio`)

### Funcionalidades
- **Inventário de Ativos:** Cadastro completo de bens materiais com número de tombo paroquial, categoria (áudio/vídeo, mobiliário, alfaias litúrgicas, veículos), estado de conservação e localização física base.
- **Alocação Temporária em Eventos:** Vinculação de itens do patrimônio a eventos específicos do calendário com data/hora de entrega e devolução.
- **Transferência entre Comunidades:** Registro da movimentação física de bens entre a Igreja Matriz e as Capelas filiadas.
- **Baixa e Manutenção:** Fluxo formal de envio de itens para manutenção técnica e baixa por obsolescência/descarte justificado.

### Regras de Negócio Críticas
1. **Unicidade do Tombo:**
   - O código identificador de patrimônio (número de tombo) é único no escopo da paróquia.
2. **Prevenção de Alocação Duplicada:**
   - Um mesmo ativo não pode estar alocado em dois eventos com janelas temporais coincidentes.
3. **Conexão de Estado:**
   - Ativos em estado `EM_MANUTENCAO` ou `BAIXADO` não podem ser selecionados para alocação em novos eventos.

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
- **Gestão de Mandatos:** Período de vigência de lideranças com alertas automáticos de expiração de mandato.

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
- **Cadastro de Ambientes:** Definição de Igreja Matriz, Capelas, Salões Comunitários, Salas de Catequese e Cozinhas industriais.
- **Atributos Físicos:** Capacidade máxima de pessoas sentadas/em pé, climatização, acessibilidade e recursos fixos instalados.
- **Vinculação:** Cada local pertence a uma comunidade ou à paróquia central.

---

## 8. Identidade e Acesso (`iam`)

### Funcionalidades
- **Autenticação Segura:** Autenticação via JSON Web Tokens (JWT) com verificação de assinatura e expiração.
- **Role-Based Access Control (RBAC):**
  - `ADMIN`: Acesso total à administração do sistema e auditoria.
  - `PAROCO`: Autoridade máxima canônica, homologações, bypass de conflito e relatórios gerais.
  - `SECRETARIA`: Gestão diária de fiéis, agendamentos, emissão de certidões e relatórios.
  - `COORDENADOR`: Gerenciamento do seu grupo pastoral e solicitação de eventos/ativos.
  - `MEMBER`: Consulta a calendários e agendas do seu grupo.
- **Proteção contra IDOR (*Insecure Direct Object References*):**
  - Usuários só podem visualizar ou manipular recursos dentro do escopo de organizações autorizadas.

---

## 9. Importação e Exportação de Relatórios (`importacao`)

### Funcionalidades
- **Exportação Multiformato:**
  - **PDF:** Fichas de fiéis, atas de conselho e termos de alocação de bens via OpenPDF e jsPDF.
  - **Excel (`.xlsx`):** Relatórios analíticos de eventos, membros e inventário de patrimônio via Apache POI.
  - **Word (`.docx`):** Documentos e certidões paroquiais parametrizadas via Apache POI (XWPF).
- **Importação em Lote:** Carga inicial de cadastros com validação prévia de linhas e retorno analítico de inconsistências.

---

## 10. Painel Executivo e Dashboard (`dashboard`)

### Funcionalidades
- **Widgets de Logística:** Alertas de choque de horários, eventos órfãos de organização e taxa de ocupação de ambientes.
- **Métricas Comunitárias:** Total de fiéis cadastrados, retenção pastoral, aniversariantes do mês e distribuição por tempo litúrgico.
- **Progresso de Projetos:** Acompanhamento de metas de campanhas e projetos pastorais ativos.
