package br.com.nsfatima.gestao.clero.api.v1.controller;

import br.com.nsfatima.gestao.clero.api.v1.dto.DioceseRequest;
import br.com.nsfatima.gestao.clero.api.v1.dto.DioceseResponse;
import br.com.nsfatima.gestao.clero.application.usecase.GerenciarDioceseUseCase;
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

/**
 * Endpoints REST para gerenciamento do cadastro da Diocese circunscricional.
 */
@RestController
@RequestMapping("/api/v1/dioceses")
@Tag(name = "Dioceses", description = "Gestão da Diocese e circunscrição eclesiástica")
public class DioceseController {

    private final GerenciarDioceseUseCase useCase;

    public DioceseController(GerenciarDioceseUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista todas as dioceses", description = "Retorna a listagem de dioceses cadastradas no sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = DioceseResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public List<DioceseResponse> listar() {
        return useCase.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtém detalhes de uma diocese", description = "Busca uma diocese pelo identificador único.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Diocese encontrada",
                    content = @Content(schema = @Schema(implementation = DioceseResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Diocese não encontrada")
    })
    public ResponseEntity<DioceseResponse> obter(
            @Parameter(description = "Identificador único da diocese", required = true)
            @PathVariable UUID id) {
        return useCase.obterPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO')")
    @Operation(summary = "Cadastra uma nova diocese", description = "Registra uma diocese com seus dados canônicos e civis.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Diocese criada com sucesso",
                    content = @Content(schema = @Schema(implementation = DioceseResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public DioceseResponse criar(@RequestBody @Valid DioceseRequest request) {
        return useCase.criar(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO')")
    @Operation(summary = "Atualiza os dados de uma diocese", description = "Atualiza o cadastro de uma diocese existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Diocese atualizada com sucesso",
                    content = @Content(schema = @Schema(implementation = DioceseResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Diocese não encontrada")
    })
    public DioceseResponse atualizar(
            @Parameter(description = "Identificador único da diocese", required = true)
            @PathVariable UUID id,
            @RequestBody @Valid DioceseRequest request) {
        return useCase.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO')")
    @Operation(summary = "Exclui uma diocese", description = "Remove uma diocese pelo seu identificador único.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Diocese excluída com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Diocese não encontrada")
    })
    public void excluir(
            @Parameter(description = "Identificador único da diocese", required = true)
            @PathVariable UUID id) {
        useCase.excluir(id);
    }
}
