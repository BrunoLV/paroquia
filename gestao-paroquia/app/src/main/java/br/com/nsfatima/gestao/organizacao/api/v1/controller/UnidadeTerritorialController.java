package br.com.nsfatima.gestao.organizacao.api.v1.controller;

import br.com.nsfatima.gestao.organizacao.api.v1.dto.UnidadeTerritorialRequest;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.UnidadeTerritorialResponse;
import br.com.nsfatima.gestao.organizacao.domain.service.UnidadeTerritorialService;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
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
 * Expõe a API REST de gestão de unidades territoriais da paróquia (Clero, Matriz, Comunidades).
 */
@RestController
@RequestMapping("/api/v1/unidades-territoriais")
@Tag(name = "Territorial Units", description = "Management of territorial units (Clergy, Main Church, Communities)")
public class UnidadeTerritorialController {

    private final UnidadeTerritorialService service;

    public UnidadeTerritorialController(UnidadeTerritorialService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List all territorial units")
    public List<UnidadeTerritorialResponse> list() {
        return service.listAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get territorial unit details")
    public ResponseEntity<UnidadeTerritorialResponse> get(@PathVariable UUID id) {
        return service.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'CONSELHO_COORDENADOR', 'CONSELHO_VICE_COORDENADOR')")
    @Operation(summary = "Create a new territorial unit")
    public UnidadeTerritorialResponse create(@RequestBody @Valid UnidadeTerritorialRequest request) {
        UnidadeTerritorialEntity entity = service.create(
                request.nome(),
                request.tipo(),
                request.contato(),
                request.ativo(),
                request.parentId()
        );
        return toResponse(entity);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'CONSELHO_COORDENADOR', 'CONSELHO_VICE_COORDENADOR')")
    @Operation(summary = "Update an existing territorial unit")
    public UnidadeTerritorialResponse update(@PathVariable UUID id, @RequestBody @Valid UnidadeTerritorialRequest request) {
        UnidadeTerritorialEntity entity = service.update(
                id,
                request.nome(),
                request.tipo(),
                request.contato(),
                request.ativo(),
                request.parentId()
        );
        return toResponse(entity);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'CONSELHO_COORDENADOR', 'CONSELHO_VICE_COORDENADOR')")
    @Operation(summary = "Delete a territorial unit")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    private UnidadeTerritorialResponse toResponse(UnidadeTerritorialEntity entity) {
        return new UnidadeTerritorialResponse(
                entity.getId(),
                entity.getNome(),
                entity.getTipo(),
                entity.getContato(),
                entity.isAtivo(),
                entity.getParentId()
        );
    }
}
