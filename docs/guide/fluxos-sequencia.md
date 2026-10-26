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

## 2. Fluxo de Reserva de Local ou Ativo Patrimonial (`reserva`)

Este fluxo demonstra como o módulo `reserva` gerencia solicitações, checagem de conflitos/manutenção, retirada e devolução com conferência de avarias de ativos patrimoniais (som, projetores, paramentos) e locais:

```mermaid
sequenceDiagram
    autonumber
    actor Solicitante as "Coordenador / Agente Pastoral"
    actor Operador as "Secretaria / Pároco"
    participant UI as "Reservas UI (/reservas)"
    participant Controller as "ReservaController"
    participant UseCase as "GerenciarReservaUseCase"
    participant ReservaRepo as "ReservaRepository"
    participant AtivoPort as "PatrimonioPort / LocalPort"

    Solicitante->>UI: Seleciona Recurso (ATIVO ou LOCAL), Evento e Período
    UI->>Controller: POST /api/v1/reservas (CriarReservaRequest)
    Controller->>UseCase: solicitar(command)

    activate UseCase
    UseCase->>AtivoPort: verificarExistenciaEManutencao(recursoId, periodo)
    UseCase->>ReservaRepo: buscarReservasConflitantes(recursoId, periodo)
    
    alt Recurso em Manutenção ou com Reserva Confirmada/Retirada no Período
        UseCase-->>Controller: Lança ConflitoReservaException
        Controller-->>UI: 409 Conflict / 422 Unprocessable Entity
        UI-->>Solicitante: Notifica indisponibilidade no período
    else Recurso Disponível
        UseCase->>ReservaRepo: salvar(Reserva em PENDENTE ou CONFIRMADA)
        UseCase-->>Controller: ReservaResponse
        deactivate UseCase
        Controller-->>UI: 201 Created
        UI-->>Solicitante: Exibe reserva registrada
    end

    Operador->>UI: Registra retirada do bem pelo responsável
    UI->>Controller: POST /api/v1/reservas/{id}/retirada
    Controller->>UseCase: registrarRetirada(id, estadoRetirada)
    UseCase->>AtivoPort: marcarStatusAtivo(RESERVADO)
    UseCase-->>Controller: ReservaResponse (status RETIRADO)

    Operador->>UI: Registra devolução e vistoria (com ou sem avaria)
    UI->>Controller: POST /api/v1/reservas/{id}/devolucao
    Controller->>UseCase: registrarDevolucao(id, estadoDevolucao, avariaDetectada)
    alt Avaria Detectada
        UseCase->>AtivoPort: abrirManutencaoAutomatica(ativoId, descricaoAvaria)
    else Sem Avaria
        UseCase->>AtivoPort: marcarStatusAtivo(DISPONIVEL)
    end
    UseCase-->>Controller: ReservaResponse (status FINALIZADA)
```

---

## 3. Fluxo de Homologação de Ata de Reunião

Fluxo da governança eclesiástica de uma ata de tipo que exige homologação (por exemplo, a reunião do CPP). O redator é o secretário do conselho; a homologação é privativa do Pároco:

```mermaid
sequenceDiagram
    autonumber
    actor Secretario as "Secretário do Conselho"
    actor Paroco as "Pároco"
    participant UI as "Conselhos & Atas UI"
    participant Controller as "AtaReuniaoController"
    participant UseCase as "HomologarAtaUseCase"
    participant AtaRepo as "AtaRepository"
    participant Auditoria as "AtaAuditoriaService"

    Secretario->>UI: Redige a ata e a submete
    UI->>Controller: POST /api/v1/conselhos/atas
    Controller->>AtaRepo: salvar(ata em RASCUNHO)
    UI->>Controller: POST /api/v1/conselhos/atas/{id}/submissao
    Note over Controller: Congela o total de convocados do quórum e leva a ata a PENDENTE_HOMOLOGACAO

    Paroco->>UI: Abre as atas pendentes
    UI->>Controller: GET /api/v1/conselhos/atas
    Controller-->>UI: Atas visíveis ao Pároco, com as ações permitidas

    Paroco->>UI: Lê a ata e clica em "Homologar"
    UI->>Controller: POST /api/v1/conselhos/atas/{id}/homologacao
    Controller->>UseCase: execute(ataId)

    activate UseCase
    UseCase->>AtaRepo: buscar(ataId)
    AtaRepo-->>UseCase: AtaReuniao
    UseCase->>UseCase: ata.homologar(...)
    Note over UseCase: Transiciona para HOMOLOGADA, atribui o número (003/2026) e sela a ata
    UseCase->>AtaRepo: salvar(ata)
    UseCase->>Auditoria: registrar(ator, HOMOLOGAR, antes, depois)
    UseCase-->>Controller: AtaVisao
    deactivate UseCase

    Controller-->>UI: 200 OK (Status: HOMOLOGADA)
    UI-->>Paroco: Ata homologada e numerada
```
