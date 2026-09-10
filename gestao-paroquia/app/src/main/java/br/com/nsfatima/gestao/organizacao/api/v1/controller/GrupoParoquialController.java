package br.com.nsfatima.gestao.organizacao.api.v1.controller;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.GrupoParoquialMembroResponse;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.GrupoParoquialRequest;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.GrupoParoquialResponse;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganogramaNodeResponse;
import br.com.nsfatima.gestao.organizacao.application.usecase.AdicionarVinculoUseCase;
import br.com.nsfatima.gestao.organizacao.application.usecase.GetOrganogramaUseCase;
import br.com.nsfatima.gestao.organizacao.application.usecase.ListGrupoParoquialMembrosUseCase;
import br.com.nsfatima.gestao.organizacao.domain.model.GrupoParoquial;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.organizacao.application.service.GrupoParoquialService;
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
@Tag(name = "Organizations", description = "Management of parish organizations (Pastorals, Movements, etc.)")
public class GrupoParoquialController {

    private final GrupoParoquialService grupoParoquialService;
    private final ListGrupoParoquialMembrosUseCase listGrupoParoquialMembrosUseCase;
    private final GetOrganogramaUseCase getOrganogramaUseCase;
    private final AdicionarVinculoUseCase adicionarVinculoUseCase;
    private final UsuarioJpaRepository usuarioRepository;

    public GrupoParoquialController(
            GrupoParoquialService grupoParoquialService,
            ListGrupoParoquialMembrosUseCase listGrupoParoquialMembrosUseCase,
            GetOrganogramaUseCase getOrganogramaUseCase,
            AdicionarVinculoUseCase adicionarVinculoUseCase,
            UsuarioJpaRepository usuarioRepository) {
        this.grupoParoquialService = grupoParoquialService;
        this.listGrupoParoquialMembrosUseCase = listGrupoParoquialMembrosUseCase;
        this.getOrganogramaUseCase = getOrganogramaUseCase;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'CONSELHO_COORDENADOR', 'CONSELHO_VICE_COORDENADOR')")
    @Operation(summary = "Create a new organization", description = "Only administrators can create organizations.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Organização criada com sucesso",
                    content = @Content(schema = @Schema(implementation = GrupoParoquialResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public GrupoParoquialResponse create(@RequestBody @Valid GrupoParoquialRequest request) {
        GrupoParoquial org = grupoParoquialService.createOrganization(
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
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'CONSELHO_COORDENADOR', 'CONSELHO_VICE_COORDENADOR')")
    @Operation(summary = "Update an existing organization", description = "Only administrators can update " +
            "organizations.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organização atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = GrupoParoquialResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Organização não encontrada")
    })
    public GrupoParoquialResponse update(
            @Parameter(description = "Identificador único da organização", required = true)
            @PathVariable UUID id,
            @RequestBody @Valid GrupoParoquialRequest request) {
        GrupoParoquial org = grupoParoquialService.updateOrganization(
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

    private void syncCoordinatorVinculos(GrupoParoquial org, String coordName, String viceCoordName) {
        UUID igrejaId = resolveIgrejaVinculoId(org);
        PapelOrganizacional papel = org.getTipo() == TipoGrupo.MOVIMENTO
                ? PapelOrganizacional.COORD_MOVIMENTO
                : PapelOrganizacional.COORD_PASTORAL_LOCAL;

        syncSingleRole(org, coordName, papel, igrejaId);
        syncSingleRole(org, viceCoordName, papel, igrejaId);
    }

    private void syncSingleRole(GrupoParoquial org,
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

    private UUID resolveIgrejaVinculoId(GrupoParoquial org) {
        // CONSELHO tem organização-mãe diretamente na UnidadeTerritorial (Matriz/Comunidade, track
        // linguagem_ubiqua_dominio); PASTORAL/MOVIMENTO têm organização-mãe um CONSELHO, cuja
        // organização-mãe é que é a UnidadeTerritorial; MINISTERIO não tem igreja de vínculo.
        if (org.getTipo() == TipoGrupo.CONSELHO) {
            return org.getParentId();
        }
        if (org.getParentId() == null) {
            return null;
        }
        var parentOpt = grupoParoquialService.getOrganization(org.getParentId());
        if (parentOpt.isEmpty()) {
            return null;
        }
        var parent = parentOpt.get();
        if (parent.getTipo() == TipoGrupo.CONSELHO) {
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
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'CONSELHO_COORDENADOR', 'CONSELHO_VICE_COORDENADOR')")
    @Operation(summary = "Delete an organization", description = "Only administrators can delete organizations. " +
            "Cannot delete organizations with active dependencies.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Organização excluída com sucesso"),
            @ApiResponse(responseCode = "400", description = "Não é possível excluir organização com dependências ativas"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Organização não encontrada")
    })
    public void delete(
            @Parameter(description = "Identificador único da organização", required = true)
            @PathVariable UUID id) {
        grupoParoquialService.deleteOrganization(id);
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
    @Operation(summary = "Get organization details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Organização encontrada com sucesso",
                    content = @Content(schema = @Schema(implementation = GrupoParoquialResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Organização não encontrada")
    })
    public ResponseEntity<GrupoParoquialResponse> get(
            @Parameter(description = "Identificador único da organização", required = true)
            @PathVariable UUID id) {
        return grupoParoquialService.getOrganization(id)
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
    @Operation(summary = "List all organizations")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de organizações retornada com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = GrupoParoquialResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public List<GrupoParoquialResponse> list() {
        return grupoParoquialService.listOrganizations().stream()
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
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = GrupoParoquialMembroResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Organização não encontrada")
    })
    public List<GrupoParoquialMembroResponse> listMembros(
            @Parameter(description = "Identificador único da organização", required = true)
            @PathVariable UUID id) {
        return listGrupoParoquialMembrosUseCase.execute(id);
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
    public OrganogramaNodeResponse getOrganograma() {
        return getOrganogramaUseCase.execute();
    }

    private GrupoParoquialResponse toResponse(GrupoParoquial domain) {
        return new GrupoParoquialResponse(
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
