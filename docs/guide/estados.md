# ⚙️ Diagramas de Estados

Este documento mapeia os ciclos de vida das entidades centrais do **Projeto Paróquia** utilizando **Diagramas de Estado Mermaid**, especificando transições válidas, condições de guarda e eventos causadores.

---

## 1. Ciclo de Vida do Evento Paroquial (`Evento`)

O agregado `Evento` gerencia seu status através de uma máquina de estados finitos que impede mutações inválidas ou inconsistências no calendário:

```mermaid
stateDiagram-v2
    [*] --> RASCUNHO: Criado pelo Coordenador

    RASCUNHO --> PENDENTE_APROVACAO: Submeter para Análise
    RASCUNHO --> CANCELADO: Descartar Rascunho

    PENDENTE_APROVACAO --> CONFIRMADO: Aprovado pelo Pároco / Coord. Geral
    PENDENTE_APROVACAO --> REJEITADO: Rejeitado com Justificativa Obrigatória

    REJEITADO --> RASCUNHO: Devolver para Correção
    REJEITADO --> CANCELADO: Cancelamento Definitivo

    CONFIRMADO --> CONCLUIDO: Data Fim Ultrapassada (Automático)
    CONFIRMADO --> CANCELADO: Cancelamento com Motivo Formal

    CONCLUIDO --> [*]
    CANCELADO --> [*]

    note right of PENDENTE_APROVACAO
        Edições diretas ficam bloqueadas
        enquanto houver solicitação pendente.
    end note

    note right of CONFIRMADO
        Horário e local estão reservados no calendário.
        Recursos patrimoniais vinculados tornam-se alocados.
    end note
```

---

## 2. Ciclo de Vida da Solicitação de Aprovação (`SolicitacaoAprovacao`)

As solicitações tramitam de forma desacoplada do evento, gerando um histórico rastreável de pareceres:

```mermaid
stateDiagram-v2
    [*] --> SUBMETIDA: Criação, Edição ou Cancelamento Solicitado

    SUBMETIDA --> EM_ANALISE: Triagem pela Secretaria Geral
    SUBMETIDA --> APROVADA: Decisão Direta do Pároco
    SUBMETIDA --> REJEITADA: Rejeição com Motivo

    EM_ANALISE --> APROVADA: Parecer Favorável Emitido
    EM_ANALISE --> REJEITADA: Parecer Desfavorável com Justificativa
    EM_ANALISE --> DEVOLVIDA: Solicitação de Esclarecimentos / Ajustes

    DEVOLVIDA --> SUBMETIDA: Reenvio pelo Autor com Correções

    APROVADA --> [*]: Mutação aplicada no Agregado Evento
    REJEITADA --> [*]: Notificação enviada ao Solicitante
```

---

## 3. Ciclo de Vida do Ativo Patrimonial (`AtivoPatrimonial`)

Controla a integridade física e o agendamento dos bens pertencentes à paróquia ou comunidades:

```mermaid
stateDiagram-v2
    [*] --> DISPONIVEL: Cadastro e Tombamento Inicial

    DISPONIVEL --> ALOCADO: Início da Janela do Evento / Termo Assinado
    ALOCADO --> DISPONIVEL: Devolução e Vistoria Concluída sem Danos

    DISPONIVEL --> EM_MANUTENCAO: Avaria Detectada / Manutenção Preventiva
    ALOCADO --> EM_MANUTENCAO: Devolução com Avaria / Laudo Emitido

    EM_MANUTENCAO --> DISPONIVEL: Reparo Concluído com Sucesso
    EM_MANUTENCAO --> BAIXADO: Laudo de Inviabilidade Técnica / Obsolescência

    DISPONIVEL --> BAIXADO: Doação / Descarte Homologado pelo CAEP

    BAIXADO --> [*]: Desincorporação Definitiva do Patrimônio

    note right of ALOCADO
        Ativo não pode ser vinculado a outro evento
        cuja janela de tempo seja coincidente.
    end note

    note right of EM_MANUTENCAO
        Indisponível para qualquer nova alocação.
    end note
```

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
