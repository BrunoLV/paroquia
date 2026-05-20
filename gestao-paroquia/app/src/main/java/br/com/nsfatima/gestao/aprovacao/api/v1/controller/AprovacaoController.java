package br.com.nsfatima.gestao.aprovacao.api.v1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoCreateRequest;
import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoDecisionRequest;
import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoDecisionResponse;
import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoFiltroRequest;
import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoResponse;
import br.com.nsfatima.gestao.aprovacao.application.usecase.CreateSolicitacaoAprovacaoUseCase;
import br.com.nsfatima.gestao.aprovacao.application.usecase.DecideSolicitacaoAprovacaoUseCase;
import br.com.nsfatima.gestao.aprovacao.application.usecase.ListAprovacoesUseCase;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import java.util.Map;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/aprovacoes")
@Tag(name = "Aprovações", description = "Endpoints para gerenciamento do fluxo de aprovação de eventos")
public class AprovacaoController {

    private final CreateSolicitacaoAprovacaoUseCase createSolicitacaoAprovacaoUseCase;
    private final DecideSolicitacaoAprovacaoUseCase decideSolicitacaoAprovacaoUseCase;
    private final ListAprovacoesUseCase listAprovacoesUseCase;
    private final EventoAuditPublisher eventoAuditPublisher;
    private final br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver actorContextResolver;

    public AprovacaoController(
            CreateSolicitacaoAprovacaoUseCase createSolicitacaoAprovacaoUseCase,
            DecideSolicitacaoAprovacaoUseCase decideSolicitacaoAprovacaoUseCase,
            ListAprovacoesUseCase listAprovacoesUseCase,
            EventoAuditPublisher eventoAuditPublisher,
            br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver actorContextResolver) {
        this.createSolicitacaoAprovacaoUseCase = createSolicitacaoAprovacaoUseCase;
        this.decideSolicitacaoAprovacaoUseCase = decideSolicitacaoAprovacaoUseCase;
        this.listAprovacoesUseCase = listAprovacoesUseCase;
        this.eventoAuditPublisher = eventoAuditPublisher;
        this.actorContextResolver = actorContextResolver;
    }

    /**
     * Entry point for requesting formal authorization for event-related actions that require governance oversight.
     * 
     * Usage Example:
     * {@code
     * AprovacaoCreateRequest request = new AprovacaoCreateRequest(eventoId, TipoSolicitacaoInput.CREATE);
     * AprovacaoResponse response = controller.create(request);
     * }
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria uma solicitação de aprovação", description = "Registra uma nova solicitação de aprovação para um evento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Solicitação criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public AprovacaoResponse create(@RequestBody @Valid AprovacaoCreateRequest request) {
        return createSolicitacaoAprovacaoUseCase.create(
                request.eventoId(),
                request.tipoSolicitacao());
    }

    /**
     * Provides visibility into the approval pipeline for administrators and coordinators to monitor pending tasks.
     * 
     * Usage Example:
     * {@code
     * AprovacaoFiltroRequest filters = new AprovacaoFiltroRequest(eventoId, "PENDENTE");
     * Page<AprovacaoResponse> page = controller.list(filters, PageRequest.of(0, 10));
     * }
     */
    @GetMapping
    @Operation(summary = "Lista solicitações de aprovação", description = "Retorna uma página de solicitações de aprovação com filtros opcionais.")
    public Page<AprovacaoResponse> list(AprovacaoFiltroRequest filters, Pageable pageable) {
        return listAprovacoesUseCase.execute(filters.eventoId(), filters.status(), pageable);
    }

    /**
     * Records the final verdict on a request, triggering the subsequent automated actions (e.g., event creation or cancellation) if approved.
     * 
     * Usage Example:
     * {@code
     * AprovacaoDecisionRequest request = new AprovacaoDecisionRequest(AprovacaoStatus.APROVADA, "Validated by coordination");
     * AprovacaoDecisionResponse response = controller.decide(id, request);
     * }
     */
    @PatchMapping("/{id}")
    @Operation(summary = "Decide sobre uma solicitação", description = "Aprova ou rejeita uma solicitação de aprovação existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Decisão registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada")
    })
    public AprovacaoDecisionResponse decide(
            @Parameter(description = "ID único da aprovação") @PathVariable UUID id,
            @RequestBody @Valid AprovacaoDecisionRequest request) {
        eventoAuditPublisher.publish(
                "system",
                "approval-decision-request",
                id.toString(),
                "received",
                Map.of("correlationId", MDC.get("correlationId") == null ? "n/a" : MDC.get("correlationId")));

        return decideSolicitacaoAprovacaoUseCase.decide(id, request);
    }
}
