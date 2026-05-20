package br.com.nsfatima.gestao.calendario.api.v1.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.CreateEventoRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.CancelEventoRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.CancelarEventoRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoApprovalPendingResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoFiltroRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoOperationResult;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.UpdateEventoRequest;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.CancelEventoUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.CreateEventoUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.ListEventosUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.UpdateEventoUseCase;
import br.com.nsfatima.gestao.calendario.domain.service.EventoService;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.mapper.EventoMapper;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Duration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/eventos")
@Tag(name = "Eventos", description = "Endpoints para gerenciamento do calendário de eventos")
public class EventoController {

    private final CreateEventoUseCase createEventoUseCase;
    private final ListEventosUseCase listEventosUseCase;
    private final UpdateEventoUseCase updateEventoUseCase;
    private final CancelEventoUseCase cancelEventoUseCase;
    private final EventoService eventoService;
    private final EventoMapper eventoMapper;
    private final EventoActorContextResolver actorContextResolver;
    private final EventoAuditPublisher eventoAuditPublisher;
    private final CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher;

    public EventoController(
            CreateEventoUseCase createEventoUseCase,
            ListEventosUseCase listEventosUseCase,
            UpdateEventoUseCase updateEventoUseCase,
            CancelEventoUseCase cancelEventoUseCase,
            EventoService eventoService,
            EventoMapper eventoMapper,
            EventoActorContextResolver actorContextResolver,
            EventoAuditPublisher eventoAuditPublisher,
            CadastroEventoMetricsPublisher cadastroEventoMetricsPublisher) {
        this.createEventoUseCase = createEventoUseCase;
        this.listEventosUseCase = listEventosUseCase;
        this.updateEventoUseCase = updateEventoUseCase;
        this.cancelEventoUseCase = cancelEventoUseCase;
        this.eventoService = eventoService;
        this.eventoMapper = eventoMapper;
        this.actorContextResolver = actorContextResolver;
        this.eventoAuditPublisher = eventoAuditPublisher;
        this.cadastroEventoMetricsPublisher = cadastroEventoMetricsPublisher;
    }

    /**
     * Serves as the primary entry point for adding new activities to the parish calendar, ensuring business rules and governance policies are applied from the start.
     * 
     * Usage Example:
     * {@code
     * CreateEventoRequest request = new CreateEventoRequest("Missa de Sábado", Instant.now(), ...);
     * ResponseEntity<Object> response = controller.create("idempotency-key", request);
     * }
     */
    @PostMapping
    @Operation(summary = "Cria um novo evento", description = "Cria um evento no calendário. Pode exigir aprovação dependendo do papel do usuário.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Evento criado com sucesso", content = @Content(schema = @Schema(implementation = EventoResponse.class))),
            @ApiResponse(responseCode = "202", description = "Solicitação de criação enviada para aprovação", content = @Content(schema = @Schema(implementation = EventoApprovalPendingResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou violação de regras de negócio"),
            @ApiResponse(responseCode = "409", description = "Conflito de idempotência")
    })
    @SuppressWarnings("null")
    public ResponseEntity<Object> create(
            @Parameter(description = "Chave de idempotência para garantir que a operação não seja repetida acidentalmente")
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody @Valid CreateEventoRequest request) {
        EventoOperationResult result = createEventoUseCase.execute(idempotencyKey, request);
        return ResponseEntity.status(result.status()).body(result.body());
    }

    /**
     * Facilitates calendar discovery and scheduling by providing a searchable list of events for various stakeholders.
     * 
     * Usage Example:
     * {@code
     * EventoFiltroRequest filters = new EventoFiltroRequest(null, null, "LITURGICO", null);
     * Page<EventoResponse> page = controller.list(filters, PageRequest.of(0, 10));
     * }
     */
    @GetMapping
    @Operation(summary = "Lista eventos com filtros", description = "Retorna uma página de eventos baseada nos filtros de data e organização.")
    public Page<EventoResponse> list(EventoFiltroRequest filters, Pageable pageable) {
        long startedAt = System.nanoTime();
        Page<EventoResponse> response = listEventosUseCase.execute(filters, pageable);
        cadastroEventoMetricsPublisher.publishCalendarQueryLatency(
                "/api/v1/eventos",
                Duration.ofNanos(System.nanoTime() - startedAt));
        eventoAuditPublisher.publishListSuccess("system", (int) response.getTotalElements());
        return response;
    }

    /**
     * Provides full descriptive details of a specific event to support deep-link views and editing interfaces.
     * 
     * Usage Example:
     * {@code
     * EventoResponse response = controller.get(eventoId);
     * }
     */
    @GetMapping("/{eventoId}")
    @Operation(summary = "Obtém detalhes de um evento", description = "Retorna os detalhes completos de um evento específico pelo seu ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento encontrado"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    public EventoResponse get(@Parameter(description = "ID único do evento") @PathVariable UUID eventoId) {
        long startedAt = System.nanoTime();
        var actorContext = actorContextResolver.resolveRequired();
        
        var entity = eventoService.findById(eventoId, actorContext);
        var response = eventoMapper.toResponse(entity);

        cadastroEventoMetricsPublisher.publishCalendarQueryLatency(
                "/api/v1/eventos/" + eventoId,
                Duration.ofNanos(System.nanoTime() - startedAt));
        eventoAuditPublisher.publish(actorContext.actor(), "read", eventoId.toString(), "success");

        return response;
    }

    /**
     * Allows for incremental updates to event details while maintaining structural integrity and triggering re-approvals when necessary.
     * 
     * Usage Example:
     * {@code
     * UpdateEventoRequest request = new UpdateEventoRequest("Nova Descrição", null, ...);
     * ResponseEntity<Object> response = controller.patch(eventoId, request);
     * }
     */
    @PatchMapping("/{eventoId}")
    @Operation(summary = "Atualiza parcialmente um evento", description = "Atualiza campos específicos de um evento. Alterações em campos sensíveis (como datas) podem exigir aprovação.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento atualizado com sucesso"),
            @ApiResponse(responseCode = "202", description = "Solicitação de atualização enviada para aprovação"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    @Transactional
    @SuppressWarnings("null")
    public ResponseEntity<Object> patch(
            @Parameter(description = "ID único do evento") @PathVariable UUID eventoId,
            @RequestBody @Valid UpdateEventoRequest request) {
        try {
            EventoOperationResult result = updateEventoUseCase.execute(eventoId, request);
            return ResponseEntity.status(result.status()).body(result.body());

        } catch (RuntimeException ex) {
            String actor = resolveActor();
            Map<String, Object> metadata = Map.of(
                    "sensitiveChange", request.changesSensitiveFields());
            eventoAuditPublisher.publish(actor, "patch", eventoId.toString(), "failure", metadata);
            throw ex;
        }
    }

    /**
     * Prevents further occurrences and removes the event from active visibility while maintaining an audit trail of why it was cancelled (Deprecated).
     * 
     * Usage Example:
     * {@code
     * controller.cancel(eventoId, new CancelEventoRequest("Conflito de agenda"));
     * }
     * 
     * @deprecated Utilize POST /{eventoId}/cancel para maior resiliência.
     */
    @DeleteMapping("/{eventoId}")
    @Operation(summary = "Cancela um evento (Depreciado)", description = "Utilize POST /{eventoId}/cancel para maior resiliência.", deprecated = true)
    @Deprecated
    @SuppressWarnings("null")
    public ResponseEntity<Object> cancel(
            @Parameter(description = "ID único do evento") @PathVariable UUID eventoId,
            @RequestBody @Valid CancelEventoRequest request) {
        EventoOperationResult result = cancelEventoUseCase.execute(eventoId, request);
        return ResponseEntity.status(result.status())
                .header("Warning", "299 - \"This endpoint is deprecated\"")
                .body(result.body());
    }

    /**
     * Safely retires an event from the schedule with a mandatory explanation to ensure organizational transparency.
     * 
     * Usage Example:
     * {@code
     * controller.cancelResilient(eventoId, new CancelarEventoRequest("Necessidade urgente de manutenção"));
     * }
     */
    @PostMapping("/{eventoId}/cancel")
    @Operation(summary = "Cancela um evento", description = "Marca um evento como cancelado. Exige um motivo para o cancelamento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Evento cancelado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Motivo não informado"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    @SuppressWarnings("null")
    public ResponseEntity<Object> cancelResilient(
            @Parameter(description = "ID único do evento") @PathVariable UUID eventoId,
            @RequestBody @Valid CancelarEventoRequest request) {
        var legacyRequest = new CancelEventoRequest(request.motivo());
        EventoOperationResult result = cancelEventoUseCase.execute(eventoId, legacyRequest);
        return ResponseEntity.status(result.status()).body(result.body());
    }

    private String resolveActor() {
        return actorContextResolver.resolveRequired().actor();
    }
}
