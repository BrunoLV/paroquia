package br.com.nsfatima.gestao.fiel.api.v1.controller;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.fiel.api.v1.dto.NotificacaoResponse;
import br.com.nsfatima.gestao.fiel.application.usecase.GerenciarNotificacaoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Controller para consulta e marcação de leitura de notificações. */
@RestController
@RequestMapping("/api/v1/notificacoes")
@Tag(name = "Notificações", description = "Central de notificações e avisos do usuário autenticado")
public class NotificacaoController {

    private final GerenciarNotificacaoUseCase useCase;
    private final EventoActorContextResolver ator;

    public NotificacaoController(GerenciarNotificacaoUseCase useCase, EventoActorContextResolver ator) {
        this.useCase = useCase;
        this.ator = ator;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista notificações do usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de notificações recuperada com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = NotificacaoResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public List<NotificacaoResponse> listar() {
        return useCase.listar(ator.resolveRequired().usuarioId());
    }

    @PostMapping("/{id}/marcar-lida")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Marca notificação como lida")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Notificação marcada como lida com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Notificação não encontrada")
    })
    public void marcarLida(
            @Parameter(description = "Identificador único da notificação") @PathVariable UUID id) {
        useCase.marcarLida(id, ator.resolveRequired().usuarioId());
    }
}
