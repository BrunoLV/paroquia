package br.com.nsfatima.gestao.organizacao.api.v1.controller;

import br.com.nsfatima.gestao.organizacao.api.v1.dto.UnidadeTerritorialRequest;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.UnidadeTerritorialResponse;
import br.com.nsfatima.gestao.organizacao.domain.service.UnidadeTerritorialService;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
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
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all territorial units")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de unidades territoriais retornada com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UnidadeTerritorialResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public List<UnidadeTerritorialResponse> list() {
        return service.listAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get territorial unit details")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unidade territorial encontrada com sucesso",
                    content = @Content(schema = @Schema(implementation = UnidadeTerritorialResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Unidade territorial não encontrada")
    })
    public ResponseEntity<UnidadeTerritorialResponse> get(
            @Parameter(description = "Identificador único da unidade territorial", required = true)
            @PathVariable UUID id) {
        return service.findById(id)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'CONSELHO_COORDENADOR', 'CONSELHO_VICE_COORDENADOR')")
    @Operation(summary = "Create a new territorial unit")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Unidade territorial criada com sucesso",
                    content = @Content(schema = @Schema(implementation = UnidadeTerritorialResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unidade territorial atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = UnidadeTerritorialResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Unidade territorial não encontrada")
    })
    public UnidadeTerritorialResponse update(
            @Parameter(description = "Identificador único da unidade territorial", required = true)
            @PathVariable UUID id,
            @RequestBody @Valid UnidadeTerritorialRequest request) {
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
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Unidade territorial excluída com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Unidade territorial não encontrada")
    })
    public void delete(
            @Parameter(description = "Identificador único da unidade territorial", required = true)
            @PathVariable UUID id) {
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
