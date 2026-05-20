package br.com.nsfatima.gestao.projeto.api.v1.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoCreateRequest;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoPatchRequest;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoResponse;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoResumoDTO;
import br.com.nsfatima.gestao.projeto.application.usecase.CreateProjetoUseCase;
import br.com.nsfatima.gestao.projeto.application.usecase.ListProjetosUseCase;
import br.com.nsfatima.gestao.projeto.application.usecase.ProjetoAgregacaoService;
import br.com.nsfatima.gestao.projeto.application.usecase.UpdateProjetoUseCase;
import br.com.nsfatima.gestao.projeto.infrastructure.observability.ProjetoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/projetos")
@Tag(name = "Projetos", description = "Endpoints para gerenciamento de projetos paroquiais")
public class ProjetoController {

    private final CreateProjetoUseCase createProjetoUseCase;
    private final ListProjetosUseCase listProjetosUseCase;
    private final UpdateProjetoUseCase updateProjetoUseCase;
    private final ProjetoAgregacaoService projetoAgregacaoService;
    private final ProjetoAuditPublisher auditPublisher;
    private final EventoActorContextResolver actorContextResolver;

    /**
     * Inicializa o controller de projetos com suas dependências necessárias para orquestração de casos de uso e auditoria.
     *
     * @param createProjetoUseCase Caso de uso para criação de novos projetos
     * @param listProjetosUseCase Caso de uso para listagem de projetos existentes
     * @param updateProjetoUseCase Caso de uso para atualização de dados de projetos
     * @param projetoAgregacaoService Serviço para consolidação de indicadores e resumos do projeto
     * @param auditPublisher Publicador de eventos de auditoria para rastreabilidade de ações
     * @param actorContextResolver Resolvedor de contexto do usuário/ator da requisição
     */
    public ProjetoController(
            CreateProjetoUseCase createProjetoUseCase,
            ListProjetosUseCase listProjetosUseCase,
            UpdateProjetoUseCase updateProjetoUseCase,
            ProjetoAgregacaoService projetoAgregacaoService,
            ProjetoAuditPublisher auditPublisher,
            EventoActorContextResolver actorContextResolver) {
        this.createProjetoUseCase = createProjetoUseCase;
        this.listProjetosUseCase = listProjetosUseCase;
        this.updateProjetoUseCase = updateProjetoUseCase;
        this.projetoAgregacaoService = projetoAgregacaoService;
        this.auditPublisher = auditPublisher;
        this.actorContextResolver = actorContextResolver;
    }

    /**
     * Disponibiliza a criação de projetos paroquiais para permitir o planejamento e acompanhamento formal de iniciativas da comunidade.
     *
     * <p>Usage Example:
     * {@code POST /api/v1/projetos} com corpo JSON contendo nome, descrição, datas e organização responsável.
     *
     * @param request Dados para criação do projeto
     * @return Dados do projeto criado
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um novo projeto", description = "Registra um novo projeto no sistema.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Projeto criado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    public ProjetoResponse create(@RequestBody @Valid ProjetoCreateRequest request) {
        var context = actorContextResolver.resolveRequired();
        var response = createProjetoUseCase.create(request);
        auditPublisher.publish(context.actor(), "create-project", response.id().toString(), "success");
        return response;
    }

    /**
     * Expõe a listagem de todos os projetos para permitir a visibilidade global das atividades e facilitar a gestão paroquial.
     *
     * <p>Usage Example:
     * {@code GET /api/v1/projetos}
     *
     * @return Lista de todos os projetos cadastrados
     */
    @GetMapping
    @Operation(summary = "Lista todos os projetos", description = "Retorna uma lista de todos os projetos cadastrados.")
    public List<ProjetoResponse> list() {
        return listProjetosUseCase.execute();
    }

    /**
     * Permite a atualização parcial de projetos para manter as informações sincronizadas com a evolução real das atividades.
     *
     * <p>Usage Example:
     * {@code PATCH /api/v1/projetos/{uuid}} com corpo JSON contendo apenas os campos que devem ser alterados.
     *
     * @param projetoId ID único do projeto
     * @param request Dados para atualização parcial
     * @return Dados atualizados do projeto
     */
    @PatchMapping("/{projetoId}")
    @Operation(summary = "Atualiza um projeto", description = "Atualiza parcialmente os dados de um projeto existente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projeto atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    public ProjetoResponse patch(
            @Parameter(description = "ID único do projeto") @PathVariable UUID projetoId,
            @RequestBody @Valid ProjetoPatchRequest request) {
        var context = actorContextResolver.resolveRequired();
        var response = updateProjetoUseCase.execute(projetoId, request);
        auditPublisher.publish(context.actor(), "update-project", projetoId.toString(), "success");
        return response;
    }

    /**
     * Fornece uma visão consolidada do projeto para apoiar a tomada de decisão baseada em indicadores de saúde e execução.
     *
     * <p>Usage Example:
     * {@code GET /api/v1/projetos/{uuid}/resumo}
     *
     * @param projetoId ID único do projeto
     * @return Resumo consolidado de indicadores do projeto
     */
    @GetMapping("/{projetoId}/resumo")
    @Operation(summary = "Obtém resumo do projeto", description = "Retorna dados agregados de execução, colaboração e saúde temporal do projeto.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resumo retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Projeto não encontrado")
    })
    public ProjetoResumoDTO getResumo(
            @Parameter(description = "ID único do projeto") @PathVariable UUID projetoId) {
        return projetoAgregacaoService.obterResumo(projetoId);
    }
}
