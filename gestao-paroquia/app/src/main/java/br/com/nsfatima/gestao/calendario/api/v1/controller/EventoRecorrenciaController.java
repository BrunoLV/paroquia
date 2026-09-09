package br.com.nsfatima.gestao.calendario.api.v1.controller;

import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoRecorrenciaRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoRecorrenciaResponse;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.CreateEventoRecorrenciaUseCase;
import br.com.nsfatima.gestao.calendario.domain.type.RegraRecorrencia;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Endpoints REST para agendamento em lote de ocorrências periódicas e regras de recorrência.
 */
@RestController
@RequestMapping("/api/v1/eventos")
@Tag(name = "Recorrência de Eventos", description = "Endpoints para configuração de regras de repetição de eventos")
@PreAuthorize("isAuthenticated()")
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
     * Automates the generation of future event occurrences based on a specific pattern, reducing manual entry for
     * periodic parish activities.
     * <p>
     * Usage Example:
     * {@code
     * controller.createRecorrencia(eventoId, new EventoRecorrenciaRequest(Frequencia.WEEKLY, 1, ...));
     * }
     */
    @PutMapping("/{eventoId}/recorrencia")
    @Operation(summary = "Configura recorrência", description = "Define a regra de repetição para o evento informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recorrência configurada com sucesso",
                    content = @Content(schema = @Schema(implementation = EventoRecorrenciaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da regra de recorrência inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Evento pai não encontrado")
    })
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
                payload.dataLimite(),
                payload.numeroOcorrencias()));
        auditPublisher.publish(context.actor(), "update-recurrence", eventoId.toString(), "success");
        return response;
    }
}
