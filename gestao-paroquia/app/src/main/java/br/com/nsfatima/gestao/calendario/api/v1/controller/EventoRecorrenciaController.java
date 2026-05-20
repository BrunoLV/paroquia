package br.com.nsfatima.gestao.calendario.api.v1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoRecorrenciaRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoRecorrenciaResponse;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.CreateEventoRecorrenciaUseCase;
import br.com.nsfatima.gestao.calendario.domain.type.RegraRecorrencia;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/eventos")
@Tag(name = "Recorrência de Eventos", description = "Endpoints para configuração de regras de repetição de eventos")
public class EventoRecorrenciaController {

    private final CreateEventoRecorrenciaUseCase createEventoRecorrenciaUseCase;
    private final EventoAuditPublisher auditPublisher;
    private final EventoActorContextResolver actorContextResolver;

    public EventoRecorrenciaController(
            CreateEventoRecorrenciaUseCase createEventoRecorrenciaUseCase,
            EventoAuditPublisher auditPublisher,
            EventoActorContextResolver actorContextResolver) {
        this.createEventoRecorrenciaUseCase = createEventoRecorrenciaUseCase;
        this.auditPublisher = auditPublisher;
        this.actorContextResolver = actorContextResolver;
    }

    /**
     * Automates the generation of future event occurrences based on a specific pattern, reducing manual entry for periodic parish activities.
     * 
     * Usage Example:
     * {@code
     * controller.createRecorrencia(eventoId, new EventoRecorrenciaRequest(Frequencia.WEEKLY, 1, ...));
     * }
     */
    @PutMapping("/{eventoId}/recorrencia")
    @Operation(summary = "Configura recorrência", description = "Define a regra de repetição para o evento informado.")
    public EventoRecorrenciaResponse createRecorrencia(
            @Parameter(description = "ID do evento pai") @PathVariable UUID eventoId,
            @RequestBody @Valid EventoRecorrenciaRequest payload) {
        var context = actorContextResolver.resolveRequired();
        var response = createEventoRecorrenciaUseCase.execute(eventoId, new RegraRecorrencia(
                payload.frequencia() != null ? payload.frequencia().name() : "SINGLE",
                payload.intervalo(),
                payload.diasDaSemana(),
                payload.posicaoNoMes(),
                payload.posicaoNoAno(),
                payload.mesDoAno(),
                payload.diaDoMes(),
                payload.dataLimite()));
        auditPublisher.publish(context.actor(), "update-recurrence", eventoId.toString(), "success");
        return response;
    }
}
