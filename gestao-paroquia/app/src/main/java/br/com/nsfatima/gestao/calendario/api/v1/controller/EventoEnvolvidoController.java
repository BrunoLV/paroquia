package br.com.nsfatima.gestao.calendario.api.v1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoEnvolvidosRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoEnvolvidosResponse;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.ClearEventoEnvolvidosUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.UpdateEventoEnvolvidosUseCase;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/eventos")
@Tag(name = "Envolvidos no Evento", description = "Endpoints para gerenciar pastorais e movimentos envolvidos em um evento")
public class EventoEnvolvidoController {

    private final UpdateEventoEnvolvidosUseCase updateEventoEnvolvidosUseCase;
    private final ClearEventoEnvolvidosUseCase clearEventoEnvolvidosUseCase;
    private final EventoAuditPublisher auditPublisher;
    private final EventoActorContextResolver actorContextResolver;

    public EventoEnvolvidoController(
            UpdateEventoEnvolvidosUseCase updateEventoEnvolvidosUseCase,
            ClearEventoEnvolvidosUseCase clearEventoEnvolvidosUseCase,
            EventoAuditPublisher auditPublisher,
            EventoActorContextResolver actorContextResolver) {
        this.updateEventoEnvolvidosUseCase = updateEventoEnvolvidosUseCase;
        this.clearEventoEnvolvidosUseCase = clearEventoEnvolvidosUseCase;
        this.auditPublisher = auditPublisher;
        this.actorContextResolver = actorContextResolver;
    }

    /**
     * Updates the multi-disciplinary involvement in an event, ensuring all relevant pastorals and groups are correctly linked for logistical coordination.
     * 
     * Usage Example:
     * {@code
     * controller.putEnvolvidos(eventoId, new EventoEnvolvidosRequest(List.of(...)));
     * }
     */
    @PutMapping("/{eventoId}/envolvidos")
    @Operation(summary = "Atualiza envolvidos", description = "Substitui a lista de organizações envolvidas no evento.")
    public EventoEnvolvidosResponse putEnvolvidos(
            @Parameter(description = "ID do evento") @PathVariable UUID eventoId,
            @RequestBody @Valid EventoEnvolvidosRequest payload) {
        var context = actorContextResolver.resolveRequired();
        var response = updateEventoEnvolvidosUseCase.execute(
                eventoId,
                payload.envolvidos());
        auditPublisher.publish(context.actor(), "update-involved", eventoId.toString(), "success");
        return response;
    }

    /**
     * Resets the involvement list of an event to its default state, used when a collaboration plan is completely overhauled.
     * 
     * Usage Example:
     * {@code
     * controller.clearEnvolvidos(eventoId);
     * }
     */
    @DeleteMapping("/{eventoId}/envolvidos")
    @Operation(summary = "Remove todos os envolvidos", description = "Limpa a lista de organizações envolvidas no evento.")
    public EventoEnvolvidosResponse clearEnvolvidos(@Parameter(description = "ID do evento") @PathVariable UUID eventoId) {
        var context = actorContextResolver.resolveRequired();
        var response = clearEventoEnvolvidosUseCase.execute(eventoId);
        auditPublisher.publish(context.actor(), "clear-involved", eventoId.toString(), "success");
        return response;
    }
}
