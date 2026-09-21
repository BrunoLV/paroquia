package br.com.nsfatima.gestao.aprovacao.api.v1.controller;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.CriarAprovacaoRequest;
import br.com.nsfatima.gestao.aprovacao.api.v1.dto.DecidirAprovacaoRequest;
import br.com.nsfatima.gestao.aprovacao.api.v1.dto.DecisaoAprovacaoResponse;
import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoFiltroRequest;
import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoResponse;
import br.com.nsfatima.gestao.aprovacao.application.usecase.CriarSolicitacaoAprovacaoUseCase;
import br.com.nsfatima.gestao.aprovacao.application.usecase.DecidirSolicitacaoAprovacaoUseCase;
import br.com.nsfatima.gestao.aprovacao.application.usecase.ListarAprovacoesUseCase;
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
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
 * Endpoints REST do fluxo formal de aprovação e governança eclesiástica de eventos.
 */
@RestController
@RequestMapping("/api/v1/aprovacoes")
@Tag(name = "Aprovações", description = "Endpoints para gerenciamento do fluxo de aprovação de eventos")
public class AprovacaoController {

    private final CriarSolicitacaoAprovacaoUseCase criarSolicitacaoAprovacaoUseCase;
    private final DecidirSolicitacaoAprovacaoUseCase decidirSolicitacaoAprovacaoUseCase;
    private final ListarAprovacoesUseCase listarAprovacoesUseCase;
    private final EventoAuditPublisher eventoAuditPublisher;

    public AprovacaoController(
            CriarSolicitacaoAprovacaoUseCase criarSolicitacaoAprovacaoUseCase,
            DecidirSolicitacaoAprovacaoUseCase decidirSolicitacaoAprovacaoUseCase,
            ListarAprovacoesUseCase listarAprovacoesUseCase,
            EventoAuditPublisher eventoAuditPublisher,
            EventoActorContextResolver actorContextResolver) {
        this.criarSolicitacaoAprovacaoUseCase = criarSolicitacaoAprovacaoUseCase;
        this.decidirSolicitacaoAprovacaoUseCase = decidirSolicitacaoAprovacaoUseCase;
        this.listarAprovacoesUseCase = listarAprovacoesUseCase;
        this.eventoAuditPublisher = eventoAuditPublisher;
    }

    /**
     * Ponto de entrada para solicitar autorização formal de ações relacionadas a eventos que exigem supervisão
     * de governança.
     * <p>
     * Exemplo de uso:
     * {@code
     * CriarAprovacaoRequest request = new CriarAprovacaoRequest(eventoId, TipoSolicitacaoInput.CREATE);
     * AprovacaoResponse response = controller.create(request);
     * }
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cria uma solicitação de aprovação", description = "Registra uma nova solicitação de " +
            "aprovação para um evento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Solicitação criada com sucesso",
                    content = @Content(schema = @Schema(implementation = AprovacaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public AprovacaoResponse criar(@RequestBody @Valid CriarAprovacaoRequest request) {
        return criarSolicitacaoAprovacaoUseCase.criar(
                request.eventoId(),
                request.tipoSolicitacao());
    }

    /**
     * Fornece visibilidade sobre o fluxo de aprovações para que administradores e coordenadores acompanhem
     * tarefas pendentes.
     * <p>
     * Exemplo de uso:
     * {@code
     * AprovacaoFiltroRequest filters = new AprovacaoFiltroRequest(eventoId, "PENDENTE");
     * Page<AprovacaoResponse> page = controller.listar(filters, PageRequest.of(0, 10));
     * }
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista solicitações de aprovação", description = "Retorna uma página de solicitações de " +
            "aprovação com filtros opcionais.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitações listadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public Page<AprovacaoResponse> listar(AprovacaoFiltroRequest filtros, Pageable pageable) {
        return listarAprovacoesUseCase.execute(filtros.eventoId(), filtros.status(), pageable);
    }

    /**
     * Registra o veredito final de uma solicitação, disparando as ações automatizadas subsequentes (ex.: criação
     * ou cancelamento de evento) se aprovada.
     * <p>
     * Exemplo de uso:
     * {@code
     * DecidirAprovacaoRequest request = new DecidirAprovacaoRequest(AprovacaoStatus.APROVADA, "Validated by
     * coordination");
     * DecisaoAprovacaoResponse response = controller.decidir(id, request);
     * }
     */
    @PatchMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Decide sobre uma solicitação", description = "Aprova ou rejeita uma solicitação de " +
            "aprovação existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Decisão registrada com sucesso",
                    content = @Content(schema = @Schema(implementation = DecisaoAprovacaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada")
    })
    public DecisaoAprovacaoResponse decidir(
            @Parameter(description = "ID único da aprovação", required = true) @PathVariable UUID id,
            @RequestBody @Valid DecidirAprovacaoRequest request) {
        eventoAuditPublisher.publish(
                "system",
                "aprovacao-decisao-solicitacao",
                id.toString(),
                "recebido",
                Map.of("correlationId", MDC.get("correlationId") == null ? "n/a" : MDC.get("correlationId")));

        return decidirSolicitacaoAprovacaoUseCase.decidir(id, request);
    }

    /**
     * Lista todas as ocorrências filhas programadas de uma aprovação mestre recorrente.
     *
     * @param id ID da aprovação mestre
     * @return lista de aprovações das ocorrências
     */
    @GetMapping("/{id}/ocorrencias")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista ocorrências de aprovação recorrente",
            description = "Retorna todas as solicitações de ocorrências filhas vinculadas à aprovação mestre informada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ocorrências listadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public List<AprovacaoResponse> listarOcorrencias(
            @Parameter(description = "ID único da aprovação mestre", required = true) @PathVariable UUID id) {
        return listarAprovacoesUseCase.listarOcorrencias(id);
    }
}
