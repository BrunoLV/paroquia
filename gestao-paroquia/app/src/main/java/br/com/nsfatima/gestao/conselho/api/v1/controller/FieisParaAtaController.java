package br.com.nsfatima.gestao.conselho.api.v1.controller;

import br.com.nsfatima.gestao.conselho.api.v1.dto.PessoaOpcaoResponse;
import br.com.nsfatima.gestao.conselho.application.usecase.BuscarFieisParaAtaUseCase;
import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Busca de fiéis pelo nome para a presença das reuniões em que qualquer fiel ativo pode constar. */
@RestController
@RequestMapping("/api/v1/conselhos/fieis")
@Tag(name = "Conselhos", description = "Atas de reunião, deliberações, homologação e exportação")
public class FieisParaAtaController {

    private final BuscarFieisParaAtaUseCase useCase;

    public FieisParaAtaController(BuscarFieisParaAtaUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Busca fiéis ativos pelo nome para a presença da ata",
            description = "Só para o redator do tipo no órgão e só nos tipos de presença livre. Devolve "
                    + "identificador e nome, no máximo 20, e exige ao menos 3 letras.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Fiéis encontrados"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Sem papel de redator, ou tipo de presença só de membros"),
            @ApiResponse(responseCode = "422", description = "Termo curto demais")
    })
    public List<PessoaOpcaoResponse> buscar(
            @Parameter(description = "Tipo de reunião") @RequestParam TipoReuniao tipo,
            @Parameter(description = "Órgão convocante") @RequestParam UUID organizacaoId,
            @Parameter(description = "Trecho do nome") @RequestParam String busca) {
        return useCase.execute(tipo, organizacaoId, busca).stream()
                .map(p -> new PessoaOpcaoResponse(p.id(), p.nome())).toList();
    }
}
