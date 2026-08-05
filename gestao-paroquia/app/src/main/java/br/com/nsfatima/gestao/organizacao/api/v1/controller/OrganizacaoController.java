package br.com.nsfatima.gestao.organizacao.api.v1.controller;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoMembroResponse;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoRequest;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoResponse;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganogramaNodeResponse;
import br.com.nsfatima.gestao.organizacao.application.usecase.AdicionarVinculoUseCase;
import br.com.nsfatima.gestao.organizacao.application.usecase.GetOrganogramaUseCase;
import br.com.nsfatima.gestao.organizacao.application.usecase.ListOrganizacaoMembrosUseCase;
import br.com.nsfatima.gestao.organizacao.domain.model.Organizacao;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.service.OrganizacaoService;
import io.swagger.v3.oas.annotations.Operation;
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
public class OrganizacaoController {

    private final OrganizacaoService organizacaoService;
    private final ListOrganizacaoMembrosUseCase listOrganizacaoMembrosUseCase;
    private final GetOrganogramaUseCase getOrganogramaUseCase;
    private final AdicionarVinculoUseCase adicionarVinculoUseCase;
    private final UsuarioJpaRepository usuarioRepository;

    public OrganizacaoController(
            OrganizacaoService organizacaoService,
            ListOrganizacaoMembrosUseCase listOrganizacaoMembrosUseCase,
            GetOrganogramaUseCase getOrganogramaUseCase,
            AdicionarVinculoUseCase adicionarVinculoUseCase,
            UsuarioJpaRepository usuarioRepository) {
        this.organizacaoService = organizacaoService;
        this.listOrganizacaoMembrosUseCase = listOrganizacaoMembrosUseCase;
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
    public OrganizacaoResponse create(@RequestBody @Valid OrganizacaoRequest request) {
        Organizacao org = organizacaoService.createOrganization(
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
    public OrganizacaoResponse update(@PathVariable UUID id, @RequestBody @Valid OrganizacaoRequest request) {
        Organizacao org = organizacaoService.updateOrganization(
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
        PapelOrganizacional papel = org.getTipo() == TipoOrganizacao.MOVIMENTO
                ? PapelOrganizacional.COORD_MOVIMENTO
                : PapelOrganizacional.COORD_PASTORAL_LOCAL;

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
        if (org.getTipo() == TipoOrganizacao.MATRIZ || org.getTipo() == TipoOrganizacao.COMUNIDADE) {
            return org.getId();
        }
        if (org.getParentId() == null) {
            return null;
        }
        var parentOpt = organizacaoService.getOrganization(org.getParentId());
        if (parentOpt.isEmpty()) {
            return null;
        }
        var parent = parentOpt.get();
        if (parent.getTipo() == TipoOrganizacao.MATRIZ || parent.getTipo() == TipoOrganizacao.COMUNIDADE) {
            return parent.getId();
        }
        if (parent.getParentId() == null) {
            return null;
        }
        var grandParentOpt = organizacaoService.getOrganization(parent.getParentId());
        if (grandParentOpt.isPresent() && (grandParentOpt.get().getTipo() == TipoOrganizacao.MATRIZ
                || grandParentOpt.get().getTipo() == TipoOrganizacao.COMUNIDADE)) {
            return grandParentOpt.get().getId();
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
    public void delete(@PathVariable UUID id) {
        organizacaoService.deleteOrganization(id);
    }

    /**
     * Recupera os dados detalhados de uma organização específica para consulta, exibição de perfil ou
     * preparação de edições.
     *
     * @param id identificador da organização
     * @return a organização encontrada com status 200, ou 404 se não existir
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get organization details")
    public ResponseEntity<OrganizacaoResponse> get(@PathVariable UUID id) {
        return organizacaoService.getOrganization(id)
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
    @Operation(summary = "List all organizations")
    public List<OrganizacaoResponse> list() {
        return organizacaoService.listOrganizations().stream()
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
    @Operation(summary = "Lista os membros de uma organização")
    public List<OrganizacaoMembroResponse> listMembros(@PathVariable UUID id) {
        return listOrganizacaoMembrosUseCase.execute(id);
    }

    /**
     * Obtém a árvore completa do organograma paroquial, partindo da organização raiz do clero.
     *
     * @return o nó raiz da árvore do organograma
     */
    @GetMapping("/organograma")
    @Operation(summary = "Obtém a árvore do organograma paroquial")
    public OrganogramaNodeResponse getOrganograma() {
        return getOrganogramaUseCase.execute();
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
