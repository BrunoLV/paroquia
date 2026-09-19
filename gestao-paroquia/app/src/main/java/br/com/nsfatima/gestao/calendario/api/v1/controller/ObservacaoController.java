package br.com.nsfatima.gestao.calendario.api.v1.controller;

import br.com.nsfatima.gestao.calendario.api.v1.dto.observacao.CriarObservacaoRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.observacao.ObservacaoResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.observacao.AtualizarObservacaoRequest;
import br.com.nsfatima.gestao.calendario.application.usecase.observacao.CriarNotaObservacaoUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.observacao.ExcluirObservacaoUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.observacao.ListarMinhasObservacoesUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.observacao.ListarObservacoesUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.observacao.AtualizarObservacaoUseCase;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.ObservacaoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Endpoints REST para notas e observações colaborativas vinculadas a eventos paroquiais.
 */
@RestController
@RequestMapping("/api/v1/eventos/{eventoId}/observacoes")
@Tag(name = "Observações e Colaboração", description = "Endpoints para adicionar notas e observações em eventos")
@PreAuthorize("isAuthenticated()")
public class ObservacaoController {

    private final CriarNotaObservacaoUseCase createNotaObservacaoUseCase;
    private final ListarObservacoesUseCase listObservacoesUseCase;
    private final ListarMinhasObservacoesUseCase listMinhasObservacoesUseCase;
    private final AtualizarObservacaoUseCase updateObservacaoUseCase;
    private final ExcluirObservacaoUseCase deleteObservacaoUseCase;
    private final ObservacaoAuditPublisher observacaoAuditPublisher;
    private final EventoActorContextResolver actorContextResolver;

    public ObservacaoController(
            CriarNotaObservacaoUseCase createNotaObservacaoUseCase,
            ListarObservacoesUseCase listObservacoesUseCase,
            ListarMinhasObservacoesUseCase listMinhasObservacoesUseCase,
            AtualizarObservacaoUseCase updateObservacaoUseCase,
            ExcluirObservacaoUseCase deleteObservacaoUseCase,
            ObservacaoAuditPublisher observacaoAuditPublisher,
            EventoActorContextResolver actorContextResolver) {
        this.createNotaObservacaoUseCase = createNotaObservacaoUseCase;
        this.listObservacoesUseCase = listObservacoesUseCase;
        this.listMinhasObservacoesUseCase = listMinhasObservacoesUseCase;
        this.updateObservacaoUseCase = updateObservacaoUseCase;
        this.deleteObservacaoUseCase = deleteObservacaoUseCase;
        this.observacaoAuditPublisher = observacaoAuditPublisher;
        this.actorContextResolver = actorContextResolver;
    }

    /**
     * Permite o enriquecimento qualitativo de dados dos eventos, possibilitando que usuários anexem notas
     * textuais e observações técnicas.
     * <p>
     * Exemplo de uso:
     * {@code
     * controller.create(eventoId, new CriarObservacaoRequest("NOTA", "Lembrar dos bancos extras"));
     * }
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Adiciona uma observação", description = "Cria uma nova nota ou observação vinculada ao " +
            "evento informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Observação criada com sucesso",
                    content = @Content(schema = @Schema(implementation = ObservacaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da observação inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    @Transactional
    public ObservacaoResponse criar(
            @Parameter(description = "ID do evento") @PathVariable UUID eventoId,
            @RequestBody @Valid CriarObservacaoRequest request) {
        var context = actorContextResolver.resolveRequired();
        return createNotaObservacaoUseCase.execute(
                eventoId,
                context.usuarioId(),
                context.actor(),
                request.tipo(),
                request.conteudo());
    }

    /**
     * Promove transparência colaborativa, agregando todos os comentários e notas relevantes associados a um evento.
     * <p>
     * Exemplo de uso:
     * {@code
     * List<ObservacaoResponse> list = controller.listar(eventoId);
     * }
     */
    @GetMapping
    @Operation(summary = "Lista todas as observações", description = "Retorna todas as observações associadas ao " +
            "evento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de observações recuperada com sucesso",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = ObservacaoResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    public List<ObservacaoResponse> listar(@Parameter(description = "ID do evento") @PathVariable UUID eventoId) {
        var context = actorContextResolver.resolveRequired();
        List<ObservacaoResponse> result = listObservacoesUseCase.execute(eventoId);
        observacaoAuditPublisher.publishList(
                context.actor(),
                eventoId.toString(),
                "success",
                Map.of("modo", "todas", "count", result.size()));
        return result;
    }

    /**
     * Permite que usuários encontrem e gerenciem rapidamente suas próprias contribuições à discussão ou
     * documentação de um evento.
     * <p>
     * Exemplo de uso:
     * {@code
     * List<ObservacaoResponse> minhas = controller.listarMinhas(eventoId);
     * }
     */
    @GetMapping("/minhas")
    @Operation(summary = "Lista minhas observações", description = "Retorna apenas as observações criadas pelo " +
            "usuário atual no evento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista das próprias observações recuperada com sucesso",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = ObservacaoResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Evento não encontrado")
    })
    public List<ObservacaoResponse> listarMinhas(@Parameter(description = "ID do evento") @PathVariable UUID eventoId) {
        var context = actorContextResolver.resolveRequired();
        List<ObservacaoResponse> result = listMinhasObservacoesUseCase.execute(eventoId, context.usuarioId());
        observacaoAuditPublisher.publishList(
                context.actor(),
                eventoId.toString(),
                "success",
                Map.of("modo", "minhas", "count", result.size()));
        return result;
    }

    /**
     * Garante que a informação permaneça precisa e relevante, permitindo que autores refinem observações anteriores.
     * <p>
     * Exemplo de uso:
     * {@code
     * controller.atualizar(eventoId, observacaoId, new AtualizarObservacaoRequest("Conteúdo revisado"));
     * }
     */
    @PatchMapping("/{observacaoId}")
    @Operation(summary = "Atualiza uma observação", description = "Altera o conteúdo de uma observação existente. " +
            "Requer que o ator seja o autor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Observação atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = ObservacaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da atualização inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Observação ou evento não encontrado")
    })
    @Transactional
    public ObservacaoResponse atualizar(
            @Parameter(description = "ID do evento") @PathVariable UUID eventoId,
            @Parameter(description = "ID da observação") @PathVariable UUID observacaoId,
            @RequestBody @Valid AtualizarObservacaoRequest request) {
        var context = actorContextResolver.resolveRequired();
        return updateObservacaoUseCase.execute(
                eventoId,
                observacaoId,
                context.usuarioId(),
                context.actor(),
                request.conteudo());
    }

    /**
     * Mantém a higiene e privacidade dos dados, permitindo a remoção de notas obsoletas ou incorretas de um evento.
     * <p>
     * Exemplo de uso:
     * {@code
     * controller.excluir(eventoId, observacaoId);
     * }
     */
    @DeleteMapping("/{observacaoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove uma observação", description = "Exclui permanentemente uma observação do sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Observação removida com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Observação ou evento não encontrado")
    })
    @Transactional
    public void excluir(
            @Parameter(description = "ID do evento") @PathVariable UUID eventoId,
            @Parameter(description = "ID da observação") @PathVariable UUID observacaoId) {
        var context = actorContextResolver.resolveRequired();
        deleteObservacaoUseCase.execute(eventoId, observacaoId, context.usuarioId(), context.actor());
    }
}
