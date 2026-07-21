package br.com.nsfatima.gestao.organizacao.api.v1.controller;

import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoRequest;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoResponse;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoMembroResponse;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganogramaNodeResponse;
import br.com.nsfatima.gestao.organizacao.domain.model.Organizacao;
import br.com.nsfatima.gestao.organizacao.domain.service.OrganizacaoService;
import br.com.nsfatima.gestao.organizacao.application.usecase.ListOrganizacaoMembrosUseCase;
import br.com.nsfatima.gestao.organizacao.application.usecase.GetOrganogramaUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/organizacoes")
@Tag(name = "Organizations", description = "Management of parish organizations (Pastorals, Movements, etc.)")
public class OrganizacaoController {

    private final OrganizacaoService organizacaoService;
    private final ListOrganizacaoMembrosUseCase listOrganizacaoMembrosUseCase;
    private final GetOrganogramaUseCase getOrganogramaUseCase;
    private final br.com.nsfatima.gestao.organizacao.application.usecase.AddMembershipUseCase addMembershipUseCase;
    private final br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository usuarioRepository;

    public OrganizacaoController(
            OrganizacaoService organizacaoService,
            ListOrganizacaoMembrosUseCase listOrganizacaoMembrosUseCase,
            GetOrganogramaUseCase getOrganogramaUseCase,
            br.com.nsfatima.gestao.organizacao.application.usecase.AddMembershipUseCase addMembershipUseCase,
            br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository usuarioRepository) {
        this.organizacaoService = organizacaoService;
        this.listOrganizacaoMembrosUseCase = listOrganizacaoMembrosUseCase;
        this.getOrganogramaUseCase = getOrganogramaUseCase;
        this.addMembershipUseCase = addMembershipUseCase;
        this.usuarioRepository = usuarioRepository;
    }

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
        syncCoordinatorMemberships(org, request.coordenador(), request.viceCoordenador());
        return toResponse(org);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'CONSELHO_COORDENADOR', 'CONSELHO_VICE_COORDENADOR')")
    @Operation(summary = "Update an existing organization", description = "Only administrators can update organizations.")
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
        syncCoordinatorMemberships(org, request.coordenador(), request.viceCoordenador());
        return toResponse(org);
    }

    private void syncCoordinatorMemberships(Organizacao org, String coordName, String viceCoordName) {
        UUID igrejaId = resolveIgrejaVinculoId(org);
        syncSingleRole(org, coordName, br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional.COORDENADOR, igrejaId);
        syncSingleRole(org, viceCoordName, br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional.VICE_COORDENADOR, igrejaId);
    }

    private void syncSingleRole(Organizacao org, String nameInput, br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional papel, UUID igrejaVinculoId) {
        if (nameInput == null || nameInput.trim().isEmpty()) {
            return;
        }
        String input = nameInput.trim();
        UUID userId = null;
        try {
            userId = UUID.fromString(input);
        } catch (IllegalArgumentException e) {
            var userOpt = usuarioRepository.findByNomeIgnoreCase(input);
            if (userOpt.isEmpty()) {
                userOpt = usuarioRepository.findByUsernameIgnoreCase(input);
            }
            if (userOpt.isPresent()) {
                userId = userOpt.get().getId();
            }
        }

        if (userId != null && usuarioRepository.existsById(userId)) {
            try {
                addMembershipUseCase.execute(userId, org.getId(), org.getTipo(), papel, igrejaVinculoId);
            } catch (Exception ignored) {
            }
        }
    }

    private UUID resolveIgrejaVinculoId(Organizacao org) {
        if (org.getTipo() == br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao.MATRIZ 
                || org.getTipo() == br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao.COMUNIDADE) {
            return org.getId();
        }
        if (org.getParentId() != null) {
            var parentOpt = organizacaoService.getOrganization(org.getParentId());
            if (parentOpt.isPresent()) {
                var parent = parentOpt.get();
                if (parent.getTipo() == br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao.MATRIZ 
                        || parent.getTipo() == br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao.COMUNIDADE) {
                    return parent.getId();
                }
                if (parent.getParentId() != null) {
                    var grandParentOpt = organizacaoService.getOrganization(parent.getParentId());
                    if (grandParentOpt.isPresent() && (grandParentOpt.get().getTipo() == br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao.MATRIZ 
                            || grandParentOpt.get().getTipo() == br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao.COMUNIDADE)) {
                        return grandParentOpt.get().getId();
                    }
                }
            }
        }
        return null;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'CONSELHO_COORDENADOR', 'CONSELHO_VICE_COORDENADOR')")
    @Operation(summary = "Delete an organization", description = "Only administrators can delete organizations. Cannot delete organizations with active dependencies.")
    /**
     * Remove o registro de uma organização do sistema, garantindo que não existam vínculos ou dependências ativas que impeçam a exclusão segura.
     * 
     * Exemplo: DELETE /api/v1/organizacoes/{id}
     */
    public void delete(@PathVariable UUID id) {
        organizacaoService.deleteOrganization(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organization details")
    /**
     * Recupera os dados detalhados de uma organização específica para consulta, exibição de perfil ou preparação de edições.
     * 
     * Exemplo: GET /api/v1/organizacoes/{id}
     */
    public ResponseEntity<OrganizacaoResponse> get(@PathVariable UUID id) {
        return organizacaoService.getOrganization(id)
            .map(this::toResponse)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "List all organizations")
    /**
     * Fornece uma listagem de todas as organizações cadastradas, permitindo a navegação, seleção e visão geral das entidades paroquiais.
     * 
     * Exemplo: GET /api/v1/organizacoes
     */
    public List<OrganizacaoResponse> list() {
        return organizacaoService.listOrganizations().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @GetMapping("/{id}/membros")
    @Operation(summary = "Lista os membros de uma organização")
    public List<OrganizacaoMembroResponse> listMembros(@PathVariable UUID id) {
        return listOrganizacaoMembrosUseCase.execute(id);
    }

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
