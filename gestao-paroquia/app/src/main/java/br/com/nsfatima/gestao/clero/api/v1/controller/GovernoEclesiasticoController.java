package br.com.nsfatima.gestao.clero.api.v1.controller;

import br.com.nsfatima.gestao.clero.api.v1.dto.GovernoEclesiasticoRequest;
import br.com.nsfatima.gestao.clero.api.v1.dto.GovernoEclesiasticoResponse;
import br.com.nsfatima.gestao.clero.application.usecase.GerenciarGovernoEclesiasticoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints REST para gerenciamento das informações de governo eclesial da Paróquia.
 */
@RestController
@RequestMapping("/api/v1/governo-eclesiastico")
@Tag(name = "Governo Eclesiástico", description = "Gestão canônica do governo paroquial e vínculo diocesano")
public class GovernoEclesiasticoController {

    private final GerenciarGovernoEclesiasticoUseCase useCase;

    public GovernoEclesiasticoController(GerenciarGovernoEclesiasticoUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtém as configurações de governo eclesiástico", description = "Retorna os dados canônicos da diocese, pároco titular, posse e decretos de ereção.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados de governo eclesiástico retornados com sucesso",
                    content = @Content(schema = @Schema(implementation = GovernoEclesiasticoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Governo eclesiástico não configurado")
    })
    public ResponseEntity<GovernoEclesiasticoResponse> obter() {
        return useCase.obter()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO')")
    @Operation(summary = "Salva as configurações de governo eclesiástico", description = "Registra ou atualiza o vínculo diocesano e clérigo pároco responsável pela paróquia.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Governo eclesiástico salvo com sucesso",
                    content = @Content(schema = @Schema(implementation = GovernoEclesiasticoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da requisição inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public GovernoEclesiasticoResponse salvar(@RequestBody @Valid GovernoEclesiasticoRequest request) {
        return useCase.salvar(request);
    }
}
