package br.com.nsfatima.gestao.organizacao.api.v1.controller;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoMembroResponse;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoRequest;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoResponse;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganogramaNodeResponse;
import br.com.nsfatima.gestao.organizacao.application.usecase.AdicionarVinculoUseCase;
import br.com.nsfatima.gestao.organizacao.application.usecase.ObterOrganogramaUseCase;
import br.com.nsfatima.gestao.organizacao.application.usecase.ListarOrganizacaoMembrosUseCase;
import br.com.nsfatima.gestao.organizacao.domain.model.Organizacao;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.application.service.OrganizacaoService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Expõe a API REST de gestão de organizações da paróquia (Matriz, Comunidades, Pastorais, Movimentos,
 * Conselhos, Clero), incluindo consulta do organograma e dos membros de cada organização. Ao criar ou
 * atualizar uma organização, sincroniza automaticamente os vínculos formais de coordenador/vice-coordenador
 * com base no nome/username informado em texto livre, via {@link AdicionarVinculoUseCase}.
 */
@RestController
@RequestMapping("/api/v1/organizacoes")
@Tag(name = "Organizações", description = "Gerenciamento de organizações paroquiais (Pastorais, Movimentos, etc.)")
public class OrganizacaoController {

    private final OrganizacaoService organizacaoService;
    private final ListarOrganizacaoMembrosUseCase listarOrganizacaoMembrosUseCase;
    private final ObterOrganogramaUseCase obterOrganogramaUseCase;
    private final AdicionarVinculoUseCase adicionarVinculoUseCase;
    private final UsuarioJpaRepository usuarioRepository;

    public OrganizacaoController(
            OrganizacaoService organizacaoService,
            ListarOrganizacaoMembrosUseCase listarOrganizacaoMembrosUseCase,
            ObterOrganogramaUseCase obterOrganogramaUseCase,
            AdicionarVinculoUseCase adicionarVinculoUseCase,
            UsuarioJpaRepository usuarioRepository) {
        this.organizacaoService = organizacaoService;
        this.listarOrganizacaoMembrosUseCase = listarOrganizacaoMembrosUseCase;
        this.obterOrganogramaUseCase = obterOrganogramaUseCase;
        this.adicionarVinculoUseCase = adicionarVinculoUseCase;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Cria uma nova organização e sincroniza os vínculos de coordenador/vice-coordenador informados em texto
     * livre na requisição.
     *
     * @param request dados da organização a criar
     * @return a organização criada
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('PAROCO')")
    @Operation(summary = "Cria uma nova organização", description = "Registra uma nova organização paroquial.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Organização criada com sucesso",
                    content = @Content(schema = @Schema(implementation = OrganizacaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public OrganizacaoResponse criar(@RequestBody @Valid OrganizacaoRequest request) {
        Organizacao org = organizacaoService.criar(
                request.nome(),
                request.tipo(),
                request.contato(),
                request.coordenador(),
                request.viceCoordenador(),
                request.parentId(),
                request.pastoralOficialId()
        );
        syncCoordinatorVinculos(org, request.coordenador(), request.viceCoordenador());
        return toResponse(org);
    }

    /**
     * Atualiza uma organização existente e ressincroniza os vínculos de coordenador/vice-coordenador com os
     * novos valores informados.
     *
     * @param id identificador da organização a atualizar
     * @param request novos dados da organização
     * @return a organização atualizada
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PAROCO')")
    @Operation(summary = "Atualiza uma organização", description = "Atualiza os dados cadastrais e lideranças de uma organização.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organização atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = OrganizacaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Organização não encontrada")
    })
    public OrganizacaoResponse atualizar(
            @Parameter(description = "Identificador único da organização", required = true)
            @PathVariable UUID id,
            @RequestBody @Valid OrganizacaoRequest request) {
        Organizacao org = organizacaoService.atualizar(
                id,
                request.nome(),
                request.tipo(),
                request.contato(),
                request.coordenador(),
                request.viceCoordenador(),
                request.ativo(),
                request.parentId(),
                request.pastoralOficialId()
        );
        syncCoordinatorVinculos(org, request.coordenador(), request.viceCoordenador());
        return toResponse(org);
    }

    private void syncCoordinatorVinculos(Organizacao org, String coordName, String viceCoordName) {
        UUID igrejaId = resolveIgrejaVinculoId(org);
        PapelOrganizacional papel = PapelOrganizacional.COORDENADOR;

        syncSingleRole(org, coordName, papel, igrejaId);
        syncSingleRole(org, viceCoordName, papel, igrejaId);
    }

    private void syncSingleRole(Organizacao org,
                                String nameInput, PapelOrganizacional papel,
                                UUID igrejaVinculoId) {

        if (nameInput == null || nameInput.trim().isEmpty()) {
            return;
        }
        String input = nameInput.trim();
        UUID userId = null;
        try {
            userId = UUID.fromString(input);
        } catch (IllegalArgumentException e) {
            var userOpt = usuarioRepository.findByUsernameIgnoreCase(input);
            if (userOpt.isPresent()) {
                userId = userOpt.get().getId();
            }
        }

        if (userId != null && usuarioRepository.existsById(userId)) {
            try {
                adicionarVinculoUseCase.execute(userId, org.getId(), org.getTipo(), papel, igrejaVinculoId);
            } catch (Exception ignored) {
            }
        }
    }

    private UUID resolveIgrejaVinculoId(Organizacao org) {
        // CONSELHO tem organização-mãe diretamente na UnidadeTerritorial (Matriz/Comunidade, track
        // linguagem_ubiqua_dominio); PASTORAL/MOVIMENTO têm organização-mãe um CONSELHO, cuja
        // organização-mãe é que é a UnidadeTerritorial; MINISTERIO não tem igreja de vínculo.
        if (org.getTipo() == TipoOrganizacao.CONSELHO) {
            return org.getParentId();
        }
        if (org.getParentId() == null) {
            return null;
        }
        var parentOpt = organizacaoService.obterPorId(org.getParentId());
        if (parentOpt.isEmpty()) {
            return null;
        }
        var parent = parentOpt.get();
        if (parent.getTipo() == TipoOrganizacao.CONSELHO) {
            return parent.getParentId();
        }
        return null;
    }

    /**
     * Remove o registro de uma organização do sistema, garantindo que não existam vínculos ou dependências
     * ativas (membros ou eventos) que impeçam a exclusão segura.
     *
     * @param id identificador da organização a excluir
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('PAROCO')")
    @Operation(summary = "Exclui uma organização", description = "Remove o cadastro de uma organização sem vínculos ou dependências ativas.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Organização excluída com sucesso"),
            @ApiResponse(responseCode = "400", description = "Não é possível excluir organização com dependências ativas"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Organização não encontrada")
    })
    public void excluir(
            @Parameter(description = "Identificador único da organização", required = true)
            @PathVariable UUID id) {
        organizacaoService.excluir(id);
    }

    /**
     * Recupera os dados detalhados de uma organização específica para consulta, exibição de perfil ou
     * preparação de edições.
     *
     * @param id identificador da organização
     * @return a organização encontrada com status 200, ou 404 se não existir
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtém detalhes de uma organização", description = "Retorna os dados completos de uma organização pelo seu identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organização encontrada com sucesso",
                    content = @Content(schema = @Schema(implementation = OrganizacaoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Organização não encontrada")
    })
    public ResponseEntity<OrganizacaoResponse> obter(
            @Parameter(description = "Identificador único da organização", required = true)
            @PathVariable UUID id) {
        return organizacaoService.obterPorId(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Fornece uma listagem de todas as organizações cadastradas, permitindo a navegação, seleção e visão
     * geral das entidades paroquiais.
     *
     * @return lista de todas as organizações cadastradas
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista todas as organizações", description = "Retorna a listagem de todas as organizações cadastradas na paróquia.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de organizações retornada com sucesso",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = OrganizacaoResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public List<OrganizacaoResponse> listar() {
        return organizacaoService.listar().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lista os membros efetivos da organização informada, incluindo os vínculos formais e o
     * coordenador/vice-coordenador registrados em texto livre.
     *
     * @param id identificador da organização
     * @return lista de membros efetivos da organização
     */
    @GetMapping("/{id}/membros")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista os membros de uma organização")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de membros retornada com sucesso",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = OrganizacaoMembroResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Organização não encontrada")
    })
    public List<OrganizacaoMembroResponse> listarMembros(
            @Parameter(description = "Identificador único da organização", required = true)
            @PathVariable UUID id) {
        return listarOrganizacaoMembrosUseCase.execute(id);
    }

    /**
     * Obtém a árvore completa do organograma paroquial, partindo da organização raiz do clero.
     *
     * @return o nó raiz da árvore do organograma
     */
    @GetMapping("/organograma")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtém a árvore do organograma paroquial")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organograma retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = OrganogramaNodeResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public OrganogramaNodeResponse obterOrganograma() {
        return obterOrganogramaUseCase.execute();
    }

    private OrganizacaoResponse toResponse(Organizacao domain) {
        return new OrganizacaoResponse(
                domain.getId(),
                domain.getNome(),
                domain.getTipo(),
                domain.getContato(),
                domain.getCoordenador(),
                domain.getViceCoordenador(),
                domain.isAtivo(),
                domain.getParentId(),
                domain.getPastoralOficialId()
        );
    }
}
