# 🔄 Diagramas de Sequência

Este documento apresenta os fluxos de interação temporal entre os atores do sistema, controllers, use cases de aplicação, domain policies puras e a camada de persistência através de **Diagramas de Sequência Mermaid**.

---

## 1. Fluxo de Criação de Evento com Validação de Choque e Fila de Aprovação

O diagrama abaixo descreve o fluxo completo de submissão de um evento, desde o clique no frontend até a verificação de sobreposição de horários e decisão de alçada:

```mermaid
sequenceDiagram
    autonumber
    actor Usuario as "Usuário (Coordenador)"
    participant UI as "React UI (CadastroEventoModal)"
    participant Controller as "EventoController (@RestController)"
    participant UseCase as "CriarEventoUseCase (@Transactional)"
    participant LocalRepo as "LocalRepository (Port)"
    participant ChoquePolicy as "ChoqueHorarioPolicy (Domain Policy)"
    participant EventoRepo as "EventoRepository (Port)"
    participant Publisher as "DomainEventPublisher"

    Usuario->>UI: Preenche formulário e clica em "Salvar"
    UI->>UI: Valida formulário no cliente (Zod Schema)
    UI->>Controller: POST /api/v1/eventos (CriarEventoRequest)
    
    Controller->>Controller: @PreAuthorize e validação DTO (Bean Validation)
    Controller->>UseCase: executar(CriarEventoCommand)

    activate UseCase
    UseCase->>LocalRepo: buscarPorId(localId)
    LocalRepo-->>UseCase: Retorna Local (ou lança LocalNaoEncontradoException)

    UseCase->>EventoRepo: buscarEventosConcorrentesNoPeriodo(localId, periodo)
    EventoRepo-->>UseCase: List[Evento] eventosExistentes

    UseCase->>ChoquePolicy: existeChoque(periodo, localId, eventosExistentes)
    activate ChoquePolicy
    ChoquePolicy-->>UseCase: boolean conflitoDetectado
    deactivate ChoquePolicy

    alt Conflito detectado e Usuário sem papel de Pároco
        UseCase-->>Controller: Lança ConflitoHorarioException(motivo)
        Controller-->>UI: 409 Conflict (ProblemDetail JSON)
        UI-->>Usuario: Exibe alerta visual de choque de horário
    else Sem conflito OU Usuário possui bypass de Pároco
        UseCase->>UseCase: Evento.criarNovoEvento(...)
        
        alt Status é ADICIONADO_EXTRA
            UseCase->>UseCase: Valida presença de justificativaExtra
        end

        UseCase->>EventoRepo: salvar(novoEvento)
        EventoRepo-->>UseCase: Evento salvo com ID gerado

        UseCase->>Publisher: publicar(new EventoCriadoEvent(novoEvento.getId()))
        UseCase-->>Controller: EventoResponseDTO
        deactivate UseCase

        Controller-->>UI: 201 Created (Location Header + JSON)
        UI->>UI: Invalida cache do TanStack Query ('/eventos')
        UI-->>Usuario: Feedback de sucesso e fecha modal
    end
```

---

## 2. Fluxo de Alocação Concorrente de Ativo Patrimonial

Este fluxo demonstra como a concorrência na alocação de itens patrimoniais (som, projetores, cadeiras) é tratada:

```mermaid
sequenceDiagram
    autonumber
    actor Operador as "Operador de Logística"
    participant UI as "Ativos Page / Modal"
    participant Controller as "AtivoController"
    participant UseCase as "AlocarAtivoUseCase"
    participant AtivoRepo as "AtivoRepository"
    participant ConflitoPolicy as "ConflitoAlocacaoPolicy"

    Operador->>UI: Seleciona Ativo e Evento para alocação
    UI->>Controller: POST /api/v1/ativos/{id}/alocacoes (AlocarAtivoRequest)
    Controller->>UseCase: executar(command)

    activate UseCase
    UseCase->>AtivoRepo: buscarPorIdComLock(ativoId)
    AtivoRepo-->>UseCase: AtivoPatrimonial

    UseCase->>AtivoRepo: buscarAlocacoesNoPeriodo(ativoId, periodo)
    AtivoRepo-->>UseCase: List[Alocacao] alocacoesExistentes

    UseCase->>ConflitoPolicy: verificarDisponibilidade(ativo, periodo, alocacoesExistentes)
    
    alt Ativo em Manutenção ou com Alocação Conflitante
        ConflitoPolicy-->>UseCase: Falha (Indisponível)
        UseCase-->>Controller: Lança AtivoIndisponivelException
        Controller-->>UI: 422 Unprocessable Entity
        UI-->>Operador: Notifica indisponibilidade do ativo
    else Ativo Disponível
        UseCase->>UseCase: ativo.alocarParaEvento(eventoId, periodo)
        UseCase->>AtivoRepo: salvar(ativo)
        UseCase-->>Controller: AlocacaoConfirmadaDTO
        deactivate UseCase
        Controller-->>UI: 200 OK
        UI-->>Operador: Exibe ativo com status ALOCADO
    end
```

---

## 3. Fluxo de Homologação de Ata do Conselho Pastoral Paroquial (CPC)

Fluxo demonstrando a governança eclesiástica de atas do conselho paroquial:

```mermaid
sequenceDiagram
    autonumber
    actor Secretario as "Secretário do CPC"
    actor Paroco as "Pároco"
    participant UI as "Conselhos & Atas UI"
    participant Controller as "AtaConselhoController"
    participant UseCase as "HomologarAtaUseCase"
    participant AtaRepo as "AtaConselhoRepository"
    participant Notifier as "NotificationService"

    Secretario->>UI: Submete minuta da ata concluída
    UI->>Controller: POST /api/v1/conselhos/atas (CriarAtaRequest)
    Controller->>AtaRepo: salvar(novaAta em status MINUTA)

    Paroco->>UI: Abre tela de deliberações pendentes
    UI->>Controller: GET /api/v1/conselhos/atas?status=MINUTA
    Controller-->>UI: Retorna atas aguardando homologação

    Paroco->>UI: Lê documento e clica em "Homologar Ata"
    UI->>Controller: POST /api/v1/conselhos/atas/{id}/homologar
    Controller->>UseCase: homologar(ataId, parocoContext)

    activate UseCase
    UseCase->>AtaRepo: buscarPorId(ataId)
    AtaRepo-->>UseCase: AtaConselho
    
    UseCase->>UseCase: ata.homologar(parocoContext)
    Note over UseCase: Transiciona status para HOMOLOGADA e bloqueia edições

    UseCase->>AtaRepo: salvar(ata)
    UseCase->>Notifier: notificarMembrosConselho(ataId)
    UseCase-->>Controller: AtaHomologadaDTO
    deactivate UseCase

    Controller-->>UI: 200 OK (Status: HOMOLOGADA)
    UI-->>Paroco: Ata homologada com sucesso e arquivada
```
