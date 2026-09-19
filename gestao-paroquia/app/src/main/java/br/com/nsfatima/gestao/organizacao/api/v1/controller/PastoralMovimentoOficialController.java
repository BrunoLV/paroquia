package br.com.nsfatima.gestao.organizacao.api.v1.controller;

import br.com.nsfatima.gestao.organizacao.api.v1.dto.PastoralMovimentoOficialRequest;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.PastoralMovimentoOficialResponse;
import br.com.nsfatima.gestao.organizacao.domain.exception.PastoralMovimentoOficialNegocioException;
import br.com.nsfatima.gestao.organizacao.domain.model.PastoralMovimentoOficial;
import br.com.nsfatima.gestao.organizacao.application.service.PastoralMovimentoOficialService;
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
 * Expõe a API REST de gestão do catálogo oficial de Pastorais e Movimentos da paróquia. Restrita a ADMIN,
 * diferente da API de {@code GrupoParoquial} que também aceita coordenadores locais, pois este catálogo define
 * o registro-mestre compartilhado por todas as instâncias locais de uma pastoral/movimento.
 */
@RestController
@RequestMapping("/api/v1/pastorais-oficiais")
@Tag(name = "Official Pastorals and Movements Catalog", description = "Management of official catalog of Pastorals " +
        "and Movements")
public class PastoralMovimentoOficialController {

    private final PastoralMovimentoOficialService service;

    public PastoralMovimentoOficialController(PastoralMovimentoOficialService service) {
        this.service = service;
    }

    /**
     * Cria um novo item do catálogo oficial de Pastorais e Movimentos.
     *
     * @param request dados do item a criar
     * @return o item criado
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new official pastoral/movement", description = "Only admins can manage the " +
            "official catalog.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item criado com sucesso",
                    content = @Content(schema = @Schema(implementation = PastoralMovimentoOficialResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public PastoralMovimentoOficialResponse criar(@RequestBody @Valid PastoralMovimentoOficialRequest request) {
        PastoralMovimentoOficial oficial = service.create(request.nome(), request.tipo());
        return toResponse(oficial);
    }

    /**
     * Atualiza um item existente do catálogo oficial de Pastorais e Movimentos.
     *
     * @param id identificador do item a atualizar
     * @param request novos dados do item
     * @return o item atualizado
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing official pastoral/movement", description = "Only admins can manage the " +
            "official catalog.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = PastoralMovimentoOficialResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado")
    })
    public PastoralMovimentoOficialResponse atualizar(
            @Parameter(description = "Identificador único do item oficial", required = true)
            @PathVariable UUID id,
            @RequestBody @Valid PastoralMovimentoOficialRequest request) {

        PastoralMovimentoOficial oficial = service.update(id, request.nome(), request.tipo());
        return toResponse(oficial);
    }

    /**
     * Remove definitivamente um item do catálogo oficial de Pastorais e Movimentos.
     *
     * @param id identificador do item a remover
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an official pastoral/movement", description = "Only admins can manage the official " +
            "catalog.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Item excluído com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado")
    })
    public void excluir(
            @Parameter(description = "Identificador único do item oficial", required = true)
            @PathVariable UUID id) {
        service.delete(id);
    }

    /**
     * Busca um item do catálogo oficial pelo seu identificador.
     *
     * @param id identificador do item
     * @return o item encontrado
     * @throws PastoralMovimentoOficialNegocioException se o item não existir
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get official pastoral/movement by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Item encontrado com sucesso",
                    content = @Content(schema = @Schema(implementation = PastoralMovimentoOficialResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado")
    })
    public PastoralMovimentoOficialResponse obter(
            @Parameter(description = "Identificador único do item oficial", required = true)
            @PathVariable UUID id) {
        PastoralMovimentoOficial oficial = service.get(id)
                .orElseThrow(() -> new PastoralMovimentoOficialNegocioException(
                        "Pastoral ou Movimento Oficial não encontrado com ID: " + id));
        return toResponse(oficial);
    }

    /**
     * Lista todos os itens do catálogo oficial de Pastorais e Movimentos cadastrados.
     *
     * @return lista completa do catálogo
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List all official pastorals/movements")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = PastoralMovimentoOficialResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public List<PastoralMovimentoOficialResponse> listar() {
        return service.list().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private PastoralMovimentoOficialResponse toResponse(PastoralMovimentoOficial domain) {
        return new PastoralMovimentoOficialResponse(
                domain.getId(),
                domain.getNome(),
                domain.getTipo()
        );
    }
}
