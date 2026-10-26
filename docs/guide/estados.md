# ⚙️ Diagramas de Estados

Este documento mapeia os ciclos de vida das entidades centrais do **Projeto Paróquia** utilizando **Diagramas de Estado Mermaid**, especificando transições válidas, condições de guarda e eventos causadores.

---

## 1. Ciclo de Vida do Evento Paroquial (`EventoEntity`)

O status do evento (`EventoStatusInput`) tem quatro valores. A **aprovação pendente não é um status**: quando a mudança é desviada para a aprovação, o evento mantém o status oficial anterior e a resposta traz campos paralelos (aprovação pendente e tipo de solicitação pendente) até a decisão:

```mermaid
stateDiagram-v2
    [*] --> RASCUNHO: Criado em elaboração
    [*] --> CONFIRMADO: Criado já confirmado na agenda
    [*] --> ADICIONADO_EXTRA: Criado fora do provisionamento anual (ano litúrgico fechado)

    RASCUNHO --> CONFIRMADO
    RASCUNHO --> CANCELADO

    CONFIRMADO --> CANCELADO: Cancelamento com motivo
    ADICIONADO_EXTRA --> CANCELADO

    CANCELADO --> [*]

    note right of CONFIRMADO
        Horário e local ocupam a agenda.
        Os ativos do evento ocupam o calendário por reserva.
    end note
```

---

## 2. Ciclo de Vida da Solicitação de Aprovação (`Aprovacao`)

As solicitações tramitam de forma desacoplada do evento. Uma solicitação pendente é decidida pelo Pároco; depois de aprovada, a ação de negócio é executada e pode falhar:

```mermaid
stateDiagram-v2
    [*] --> PENDENTE: Criação, edição ou cancelamento solicitado

    PENDENTE --> APROVADA: Decisão favorável do Pároco
    PENDENTE --> REPROVADA: Decisão desfavorável

    APROVADA --> APROVADA: Execução da ação registrada com sucesso
    APROVADA --> FALHA_EXECUCAO: A ação aprovada falhou ao ser aplicada
    FALHA_EXECUCAO --> APROVADA: Reexecução (só falha sistêmica)

    REPROVADA --> [*]
```

A falha de execução é classificada como `NEGOCIO_CADASTRAL` ou `SISTEMICA_APLICACAO`; só a sistêmica admite reexecução. A decisão da aprovação-mãe é propagada às filhas.

---

## 3. Ciclo de Vida do Ativo Patrimonial (`AtivoPatrimonial`)

O status (`AtivoStatus`) reflete reservas e manutenção; a exclusão é lógica e não é um status:

```mermaid
stateDiagram-v2
    [*] --> DISPONIVEL: Cadastro

    DISPONIVEL --> RESERVADO: Existe reserva ativa
    RESERVADO --> DISPONIVEL: Sem reserva ativa

    DISPONIVEL --> EM_MANUTENCAO: Envio para manutenção (sem reservas conflitantes)
    EM_MANUTENCAO --> DISPONIVEL: Manutenção concluída

    note right of EM_MANUTENCAO
        Tem precedência sobre RESERVADO e bloqueia
        novas reservas no período da manutenção.
    end note
```

A exclusão lógica é recusada com manutenção em aberto ou reservas futuras pendentes ou confirmadas.

---

## 4. Ciclo de Vida da Ata de Reunião (`AtaReuniao`)

Garante a fé pública eclesiástica e a rastreabilidade documental das atas dos conselhos e das demais reuniões pastorais. O caminho depende do tipo: os que exigem homologação passam pelo Pároco; os demais são registrados na submissão (`conselho.md`, seção 3):

```mermaid
stateDiagram-v2
    [*] --> RASCUNHO: Redator abre a ata

    RASCUNHO --> PENDENTE_HOMOLOGACAO: Submissão (tipo que exige homologação)
    RASCUNHO --> REGISTRADA: Submissão (tipo sem homologação)

    PENDENTE_HOMOLOGACAO --> HOMOLOGADA: Pároco homologa e a ata recebe número
    PENDENTE_HOMOLOGACAO --> VETADA: Pároco veta com motivo

    VETADA --> RASCUNHO: Reabertura pelo redator

    HOMOLOGADA --> [*]: Selada (100% imutável)
    REGISTRADA --> [*]: Selada (100% imutável)
```

Ata selada nunca é editada nem excluída: a correção é uma **ata retificadora**, nova ata em `RASCUNHO` ligada à original, com justificativa obrigatória.
