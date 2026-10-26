package br.com.nsfatima.gestao.conselho.api.v1.controller;

import br.com.nsfatima.gestao.conselho.api.v1.dto.ClerigoOpcaoResponse;
import br.com.nsfatima.gestao.conselho.application.usecase.ListarClerigosParaAtaUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Clérigos que podem constar na presença de uma ata, já que o clero não é cadastrado como fiel. */
@RestController
@RequestMapping("/api/v1/conselhos/clero")
@Tag(name = "Conselhos", description = "Atas de reunião, deliberações, homologação e exportação")
public class ClerigosParaAtaController {

    private final ListarClerigosParaAtaUseCase useCase;

    public ClerigosParaAtaController(ListarClerigosParaAtaUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Clérigos ativos para a presença da ata",
            description = "Só nome eclesiástico e a função que o encargo sugere; vazio para quem não tem vínculo "
                    + "com nenhum órgão.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Clérigos listados"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public List<ClerigoOpcaoResponse> listar() {
        return useCase.execute().stream()
                .map(c -> new ClerigoOpcaoResponse(c.id(), c.nome(), c.funcao(), c.paroco())).toList();
    }
}
