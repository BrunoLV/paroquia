package br.com.nsfatima.gestao.iam.api.v1.controller;

import br.com.nsfatima.gestao.iam.api.v1.dto.PapelGlobalRequest;
import br.com.nsfatima.gestao.iam.application.usecase.AtribuirPapelGlobalUseCase;
import br.com.nsfatima.gestao.iam.application.usecase.RevogarPapelGlobalUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Expõe a concessão/revogação de papéis globais (`PAROCO`, `VIGARIO`, `ADMIN`, `SECRETARIA_PAROQUIAL`),
 * restrita a `ADMIN`.
 */
@RestController
@RequestMapping("/api/v1/papeis-globais")
@Tag(name = "Papéis Globais", description = "Concessão/revogação de acesso irrestrito (Pároco/Vigário/Admin/Secretaria)")
public class PapelGlobalController {

    private final AtribuirPapelGlobalUseCase atribuirPapelGlobalUseCase;
    private final RevogarPapelGlobalUseCase revogarPapelGlobalUseCase;

    public PapelGlobalController(
            AtribuirPapelGlobalUseCase atribuirPapelGlobalUseCase,
            RevogarPapelGlobalUseCase revogarPapelGlobalUseCase) {
        this.atribuirPapelGlobalUseCase = atribuirPapelGlobalUseCase;
        this.revogarPapelGlobalUseCase = revogarPapelGlobalUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Concede um papel global a um Fiel")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Papel global concedido com sucesso",
                    content = @Content(schema = @Schema(implementation = UUID.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Fiel ou papel não encontrado")
    })
    public UUID atribuir(@RequestBody @Valid PapelGlobalRequest request) {
        return atribuirPapelGlobalUseCase.execute(request.fielId(), request.papel());
    }

    @PostMapping("/revogar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Revoga um papel global de um Fiel")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Papel global revogado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Fiel ou papel não encontrado")
    })
    public void revogar(@RequestBody @Valid PapelGlobalRequest request) {
        revogarPapelGlobalUseCase.execute(request.fielId(), request.papel());
    }
}
