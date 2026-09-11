package br.com.nsfatima.gestao.calendario.api.v1.controller;

import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoRecorrenciaDetalheResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoRecorrenciaRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoRecorrenciaResponse;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.CreateEventoRecorrenciaUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.GetEventoRecorrenciaUseCase;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
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
    private final GetEventoRecorrenciaUseCase getEventoRecorrenciaUseCase;
    private final EventoAuditPublisher auditPublisher;
    private final EventoActorContextResolver actorContextResolver;

    public EventoRecorrenciaController(
            CreateEventoRecorrenciaUseCase createEventoRecorrenciaUseCase,
            GetEventoRecorrenciaUseCase getEventoRecorrenciaUseCase,
            EventoAuditPublisher auditPublisher,
            EventoActorContextResolver actorContextResolver) {
        this.createEventoRecorrenciaUseCase = createEventoRecorrenciaUseCase;
        this.getEventoRecorrenciaUseCase = getEventoRecorrenciaUseCase;
        this.auditPublisher = auditPublisher;
        this.actorContextResolver = actorContextResolver;
    }

    /**
     * Devolve a regra de recorrência completa do evento (frequência, dias, limites) — necessária
     * para pré-preencher o formulário de edição e a ficha de detalhes de um evento gerado por
     * recorrência; até aqui só o {@code recorrenciaId} chegava ao cliente, nunca o conteúdo da regra.
     */
    @GetMapping("/{eventoId}/recorrencia")
    @Operation(summary = "Consulta a regra de recorrência", description = "Retorna a regra de repetição " +
            "completa do evento informado, quando ele pertence a uma série.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Regra de recorrência retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = EventoRecorrenciaDetalheResponse.class))),
            @ApiResponse(responseCode = "204", description = "Evento não pertence a nenhuma série de recorrência"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    public ResponseEntity<EventoRecorrenciaDetalheResponse> getRecorrencia(
            @Parameter(description = "ID do evento") @PathVariable UUID eventoId) {
        return getEventoRecorrenciaUseCase.execute(eventoId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NO_CONTENT).build());
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
