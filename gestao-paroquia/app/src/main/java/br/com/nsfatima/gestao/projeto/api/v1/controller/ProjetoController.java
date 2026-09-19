package br.com.nsfatima.gestao.projeto.api.v1.controller;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoAprovacaoResponse;
import br.com.nsfatima.gestao.projeto.api.v1.dto.CriarProjetoRequest;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoDecisaoRequest;
import br.com.nsfatima.gestao.projeto.api.v1.dto.AtualizarProjetoRequest;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoResponse;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoResumoDTO;
import br.com.nsfatima.gestao.projeto.application.service.ProjetoAgregacaoService;
import br.com.nsfatima.gestao.projeto.application.usecase.CriarProjetoUseCase;
import br.com.nsfatima.gestao.projeto.application.usecase.ListarProjetosUseCase;
import br.com.nsfatima.gestao.projeto.application.usecase.TransicionarProjetoUseCase;
import br.com.nsfatima.gestao.projeto.application.usecase.AtualizarProjetoUseCase;
import br.com.nsfatima.gestao.projeto.infrastructure.observability.ProjetoAuditPublisher;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.mapper.ProjetoMapper;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoSolicitacaoAprovacaoJpaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints REST para criação, consulta e transição de estado de Projetos pastorais.
 */
@RestController
@RequestMapping("/api/v1/projetos")
@Tag(name = "Projetos", description = "Endpoints para gerenciamento de projetos paroquiais")
public class ProjetoController {

    private static final String RESULT_SUCCESS = "success";
    private final CriarProjetoUseCase createProjetoUseCase;
    private final ListarProjetosUseCase listProjetosUseCase;
    private final AtualizarProjetoUseCase updateProjetoUseCase;
    private final ProjetoAgregacaoService projetoAgregacaoService;
    private final TransicionarProjetoUseCase transitionProjetoUseCase;
    private final ProjetoAuditPublisher auditPublisher;
    private final EventoActorContextResolver actorContextResolver;
    private final ProjetoSolicitacaoAprovacaoJpaRepository aprovacaoRepository;
    private final ProjetoMapper mapper;

    /**
     * Inicializa o controller de projetos com suas dependências necessárias para orquestração de casos de uso e
     * auditoria.
     *
     * @param createProjetoUseCase     Caso de uso para criação de novos projetos
     * @param listProjetosUseCase      Caso de uso para listagem de projetos existentes
     * @param updateProjetoUseCase     Caso de uso para atualização de dados de projetos
     * @param projetoAgregacaoService  Serviço para consolidação de indicadores e resumos do projeto
     * @param transitionProjetoUseCase Caso de uso para transição e aprovação de projetos
     * @param auditPublisher           Publicador de eventos de auditoria para rastreabilidade de ações
     * @param actorContextResolver     Resolvedor de contexto do usuário/ator da requisição
     * @param aprovacaoRepository       Repositório para solicitações de aprovação
     * @param mapper                   Mapeador de projetos
     */
    public ProjetoController(
            CriarProjetoUseCase createProjetoUseCase,
            ListarProjetosUseCase listProjetosUseCase,
            AtualizarProjetoUseCase updateProjetoUseCase,
            ProjetoAgregacaoService projetoAgregacaoService,
            TransicionarProjetoUseCase transitionProjetoUseCase,
            ProjetoAuditPublisher auditPublisher,
            EventoActorContextResolver actorContextResolver,
            ProjetoSolicitacaoAprovacaoJpaRepository aprovacaoRepository,
            ProjetoMapper mapper) {
        this.createProjetoUseCase = createProjetoUseCase;
        this.listProjetosUseCase = listProjetosUseCase;
        this.updateProjetoUseCase = updateProjetoUseCase;
        this.projetoAgregacaoService = projetoAgregacaoService;
        this.transitionProjetoUseCase = transitionProjetoUseCase;
        this.auditPublisher = auditPublisher;
        this.actorContextResolver = actorContextResolver;
        this.aprovacaoRepository = aprovacaoRepository;
        this.mapper = mapper;
    }

    /**
     * Disponibiliza a criação de projetos paroquiais para permitir o planejamento e acompanhamento formal de
     * initiatives da comunidade.
     *
     * <p>Exemplo de uso:
     * {@code POST /api/v1/projetos} com corpo JSON contendo nome, descrição, datas e organização responsável.
     *
     * @param request Dados para criação do projeto
     * @return Dados do projeto criado ou da solicitação de aprovação
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cria um novo projeto", description = "Registra um novo projeto no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Projeto criado com sucesso"),
            @ApiResponse(responseCode = "202", description = "Solicitação de criação encaminhada para aprovação"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ResponseEntity<?> criar(@RequestBody @Valid CriarProjetoRequest request) {
        var context = actorContextResolver.resolveRequired();
        var result = createProjetoUseCase.create(request);
        if (result.isPendenteAprovacao()) {
            auditPublisher.publish(context.actor(),
                    "request-create-project", result.aprovacao().id().toString(), RESULT_SUCCESS);

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(result.aprovacao());
        }
        auditPublisher.publish(context.actor(), "create-project", result.projeto().id().toString(), RESULT_SUCCESS);
        return ResponseEntity.status(HttpStatus.CREATED).body(result.projeto());
    }

    /**
     * Expõe a listagem de todos os projetos para permitir a visibilidade global das atividades e facilitar a gestão
     * paroquial.
     *
     * <p>Exemplo de uso:
     * {@code GET /api/v1/projetos}
     *
     * @return Lista de todos os projetos cadastrados
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista todos os projetos", description = "Retorna uma lista de todos os projetos cadastrados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projetos listados com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public List<ProjetoResponse> listar() {
        return listProjetosUseCase.execute();
    }

    /**
     * Permite a atualização parcial de projetos para manter as informações sincronizadas com a evolução real das
     * atividades.
     *
     * <p>Exemplo de uso:
     * {@code PATCH /api/v1/projetos/{uuid}} com corpo JSON contendo apenas os campos que devem ser alterados.
     *
     * @param projetoId ID único do projeto
     * @param request   Dados para atualização parcial
     * @return Dados atualizados do projeto
     */
    @PatchMapping("/{projetoId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Atualiza um projeto", description = "Atualiza parcialmente os dados de um projeto existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projeto atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    public ProjetoResponse atualizar(
            @Parameter(description = "ID único do projeto") @PathVariable UUID projetoId,
            @RequestBody @Valid AtualizarProjetoRequest request) {
        var context = actorContextResolver.resolveRequired();
        var response = updateProjetoUseCase.execute(projetoId, request);
        auditPublisher.publish(context.actor(), "update-project", projetoId.toString(), RESULT_SUCCESS);
        return response;
    }

    /**
     * Fornece uma visão consolidada do projeto para apoiar a tomada de decisão baseada em indicadores de saúde e
     * execução.
     *
     * <p>Exemplo de uso:
     * {@code GET /api/v1/projetos/{uuid}/resumo}
     *
     * @param projetoId ID único do projeto
     * @return Resumo consolidado de indicadores do projeto
     */
    @GetMapping("/{projetoId}/resumo")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtém resumo do projeto", description = "Retorna dados agregados de execução, colaboração e" +
            " saúde temporal do projeto.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumo retornado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    public ProjetoResumoDTO getResumo(
            @Parameter(description = "ID único do projeto") @PathVariable UUID projetoId) {
        return projetoAgregacaoService.obterResumo(projetoId);
    }

    @PostMapping("/{projetoId}/iniciar")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Inicia um projeto", description = "Altera o status de um projeto de ABERTO para " +
            "EM_ANDAMENTO.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projeto iniciado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Transição inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    public ProjetoResponse iniciar(
            @Parameter(description = "ID único do projeto", required = true)
            @PathVariable UUID projetoId) {
        return transitionProjetoUseCase.iniciar(projetoId);
    }

    @PostMapping("/{projetoId}/concluir")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Conclui um projeto", description = "Altera o status de um projeto de EM_ANDAMENTO para " +
            "CONCLUIDO.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projeto concluído com sucesso"),
            @ApiResponse(responseCode = "400", description = "Transição inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    public ProjetoResponse concluir(
            @Parameter(description = "ID único do projeto", required = true)
            @PathVariable UUID projetoId) {
        return transitionProjetoUseCase.concluir(projetoId);
    }

    @PostMapping("/{projetoId}/cancelar")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancela um projeto", description = "Cancela um projeto e atualiza o status de seus eventos " +
            "conforme a opção selecionada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projeto cancelado diretamente com sucesso"),
            @ApiResponse(responseCode = "202", description = "Solicitação de cancelamento encaminhada para aprovação"),
            @ApiResponse(responseCode = "400", description = "Transição inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    public ResponseEntity<?> cancelar(
            @Parameter(description = "ID único do projeto", required = true)
            @PathVariable UUID projetoId,
            @RequestBody CancelarProjetoRequest request) {
        var context = actorContextResolver.resolveRequired();
        var result = transitionProjetoUseCase.cancelar(projetoId, request.cancelarEventosVinculados());
        if (result.isPendenteAprovacao()) {
            auditPublisher.publish(context.actor(),
                    "request-cancel-project", result.aprovacao().id().toString(), RESULT_SUCCESS);

            return ResponseEntity.status(HttpStatus.ACCEPTED).body(result.aprovacao());
        }
        auditPublisher.publish(context.actor(), "cancel-project", result.projeto().id().toString(), RESULT_SUCCESS);
        return ResponseEntity.ok(result.projeto());
    }

    @PostMapping("/solicitacoes/{solicitacaoId}/decisao")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Decide sobre uma solicitação", description = "Aprova ou rejeita uma solicitação de criação " +
            "ou cancelamento de projeto.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Decisão registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Decisão inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada")
    })
    public ResponseEntity<?> decidir(
            @Parameter(description = "ID único da solicitação", required = true)
            @PathVariable UUID solicitacaoId,
            @RequestBody @Valid ProjetoDecisaoRequest request) {
        var response = transitionProjetoUseCase.decidir(solicitacaoId, request.aprovado(), request.observacao());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/solicitacoes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista solicitações de aprovação de projetos", description = "Retorna uma lista de " +
            "solicitações de criação ou cancelamento de projetos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitações listadas com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public List<ProjetoAprovacaoResponse> listarSolicitacoes(
            @Parameter(description = "Filtro opcional por status da solicitação")
            @RequestParam(required = false) String status) {
        var solicitacoes = status != null ?
                aprovacaoRepository.findByStatus(AprovacaoStatus.fromJson(status)) :
                aprovacaoRepository.findAll();
        return solicitacoes.stream()
                .map(mapper::toAprovacaoResponse)
                .toList();
    }

    public record CancelarProjetoRequest(boolean cancelarEventosVinculados) {
    }
}
