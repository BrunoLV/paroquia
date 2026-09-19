package br.com.nsfatima.gestao.calendario.api.v1.controller;

import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoEnvolvidosRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoEnvolvidosResponse;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.LimparEventoEnvolvidosUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.AtualizarEventoEnvolvidosUseCase;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Endpoints REST para gerenciamento de pastorais, movimentos e comissões envolvidas em eventos.
 */
@RestController
@RequestMapping("/api/v1/eventos")
@Tag(name = "Envolvidos no Evento", description = "Endpoints para gerenciar pastorais e movimentos envolvidos em um " +
        "evento")
@PreAuthorize("isAuthenticated()")
public class EventoEnvolvidoController {

    private final AtualizarEventoEnvolvidosUseCase updateEventoEnvolvidosUseCase;
    private final LimparEventoEnvolvidosUseCase clearEventoEnvolvidosUseCase;
    private final EventoAuditPublisher auditPublisher;
    private final EventoActorContextResolver actorContextResolver;

    public EventoEnvolvidoController(
            AtualizarEventoEnvolvidosUseCase updateEventoEnvolvidosUseCase,
            LimparEventoEnvolvidosUseCase clearEventoEnvolvidosUseCase,
            EventoAuditPublisher auditPublisher,
            EventoActorContextResolver actorContextResolver) {
        this.updateEventoEnvolvidosUseCase = updateEventoEnvolvidosUseCase;
        this.clearEventoEnvolvidosUseCase = clearEventoEnvolvidosUseCase;
        this.auditPublisher = auditPublisher;
        this.actorContextResolver = actorContextResolver;
    }

    /**
     * Atualiza o envolvimento multidisciplinar em um evento, garantindo que todas as pastorais e grupos
     * relevantes estejam corretamente vinculados para coordenação logística.
     * <p>
     * Exemplo de uso:
     * {@code
     * controller.putEnvolvidos(eventoId, new EventoEnvolvidosRequest(List.of(...)));
     * }
     */
    @PutMapping("/{eventoId}/envolvidos")
    @Operation(summary = "Atualiza envolvidos", description = "Substitui a lista de organizações envolvidas no evento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de envolvidos atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = EventoEnvolvidosResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
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
     * Restaura a lista de envolvidos de um evento ao seu estado padrão, usado quando um plano de colaboração é
     * completamente refeito.
     * <p>
     * Exemplo de uso:
     * {@code
     * controller.clearEnvolvidos(eventoId);
     * }
     */
    @DeleteMapping("/{eventoId}/envolvidos")
    @Operation(summary = "Remove todos os envolvidos", description = "Limpa a lista de organizações envolvidas no " +
            "evento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Envolvidos removidos com sucesso",
                    content = @Content(schema = @Schema(implementation = EventoEnvolvidosResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    public EventoEnvolvidosResponse clearEnvolvidos(
            @Parameter(description = "ID do evento") @PathVariable UUID eventoId) {
        var context = actorContextResolver.resolveRequired();
        var response = clearEventoEnvolvidosUseCase.execute(eventoId);
        auditPublisher.publish(context.actor(), "clear-involved", eventoId.toString(), "success");
        return response;
    }
}
