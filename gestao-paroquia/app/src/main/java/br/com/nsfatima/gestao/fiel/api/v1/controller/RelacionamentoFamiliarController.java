package br.com.nsfatima.gestao.fiel.api.v1.controller;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.clero.domain.model.TipoPessoaFamiliar;
import br.com.nsfatima.gestao.fiel.api.v1.dto.CriarRelacionamentoFamiliarRequest;
import br.com.nsfatima.gestao.fiel.api.v1.dto.RelacionamentoFamiliarResponse;
import br.com.nsfatima.gestao.fiel.application.usecase.GerenciarRelacionamentoFamiliarUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Gestão administrativa dos vínculos familiares ativos de fiéis. */
@RestController
@RequestMapping("/api/v1/fieis/{fielId}/familiares")
@Tag(name = "Família", description = "Vínculos familiares de fiéis")
public class RelacionamentoFamiliarController {

    private final GerenciarRelacionamentoFamiliarUseCase useCase;
    private final EventoActorContextResolver ator;

    public RelacionamentoFamiliarController(
            GerenciarRelacionamentoFamiliarUseCase useCase,
            EventoActorContextResolver ator) {
        this.useCase = useCase;
        this.ator = ator;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_PAROCO','GLOBAL_SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Lista familiares ativos do fiel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de familiares recuperada com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = RelacionamentoFamiliarResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Fiel não encontrado")
    })
    public List<RelacionamentoFamiliarResponse> listar(
            @Parameter(description = "Identificador único do fiel") @PathVariable UUID fielId) {
        return useCase.listar(fielId, TipoPessoaFamiliar.FIEL);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_PAROCO','GLOBAL_SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Adiciona vínculo familiar")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vínculo familiar criado com sucesso",
                    content = @Content(schema = @Schema(implementation = UUID.class))),
            @ApiResponse(responseCode = "400", description = "Dados da relação inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Fiel de origem ou destino não encontrado")
    })
    public UUID criar(
            @Parameter(description = "Identificador único do fiel") @PathVariable UUID fielId,
            @RequestBody @Valid CriarRelacionamentoFamiliarRequest request) {
        return useCase.criar(
                fielId,
                TipoPessoaFamiliar.FIEL,
                request.pessoaDestinoId(),
                request.tipoPessoaDestinoEfetivo(),
                request.tipo(),
                ator.resolveRequired().usuarioId());
    }

    @DeleteMapping("/{relacaoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_PAROCO','GLOBAL_SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Remove vínculo familiar")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Vínculo familiar removido com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Vínculo familiar não encontrado")
    })
    public void remover(
            @Parameter(description = "Identificador único da relação familiar") @PathVariable UUID relacaoId) {
        useCase.remover(relacaoId, ator.resolveRequired().usuarioId());
    }
}
