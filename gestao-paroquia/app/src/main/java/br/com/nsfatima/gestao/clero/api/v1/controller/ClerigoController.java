package br.com.nsfatima.gestao.clero.api.v1.controller;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.clero.api.v1.dto.ClerigoInstitucionalResponse;
import br.com.nsfatima.gestao.clero.api.v1.dto.ClerigoRequest;
import br.com.nsfatima.gestao.clero.api.v1.dto.ClerigoResponse;
import br.com.nsfatima.gestao.clero.api.v1.dto.DecidirAjusteClericalRequest;
import br.com.nsfatima.gestao.clero.api.v1.dto.EncargoClericalResponse;
import br.com.nsfatima.gestao.clero.api.v1.dto.SolicitacaoAjusteClericalResponse;
import br.com.nsfatima.gestao.clero.api.v1.dto.SolicitarAjusteClericalRequest;
import br.com.nsfatima.gestao.clero.application.usecase.GerenciarAjusteClericalUseCase;
import br.com.nsfatima.gestao.clero.application.usecase.GerenciarClerigoUseCase;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Endpoints protegidos de gestão, autoatendimento e diretório institucional do Clero. */
@RestController
@RequestMapping("/api/v1/clerigos")
@Tag(name = "Clero", description = "Gestão institucional de ministros ordenados")
public class ClerigoController {

    private final GerenciarClerigoUseCase useCase;
    private final GerenciarAjusteClericalUseCase ajusteUseCase;
    private final GerenciarRelacionamentoFamiliarUseCase familiaUseCase;
    private final EventoActorContextResolver actor;

    public ClerigoController(
            GerenciarClerigoUseCase useCase,
            GerenciarAjusteClericalUseCase ajusteUseCase,
            GerenciarRelacionamentoFamiliarUseCase familiaUseCase,
            EventoActorContextResolver actor) {
        this.useCase = useCase;
        this.ajusteUseCase = ajusteUseCase;
        this.familiaUseCase = familiaUseCase;
        this.actor = actor;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_PAROCO','GLOBAL_SECRETARIA_PAROQUIAL','GLOBAL_BISPO_DIOCESANO')")
    @Operation(summary = "Lista clérigos ativos")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de clérigos recuperada com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClerigoResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public List<ClerigoResponse> listar() {
        return useCase.listar();
    }

    @GetMapping("/institucional")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista o diretório institucional sem PII")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Diretório institucional recuperado com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClerigoInstitucionalResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public List<ClerigoInstitucionalResponse> diretorio() {
        return useCase.diretorio();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtém perfil do clérigo autenticado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil próprio recuperado com sucesso",
                    content = @Content(schema = @Schema(implementation = ClerigoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Clérigo não vinculado ao usuário")
    })
    public ClerigoResponse perfilProprio() {
        return useCase.obterPorUsuario(actor.resolveRequired().usuarioId());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_PAROCO','GLOBAL_SECRETARIA_PAROQUIAL','GLOBAL_BISPO_DIOCESANO')")
    @Operation(summary = "Obtém cadastro clerical")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cadastro clerical recuperado com sucesso",
                    content = @Content(schema = @Schema(implementation = ClerigoResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Clérigo não encontrado")
    })
    public ClerigoResponse obter(
            @Parameter(description = "Identificador único do clérigo") @PathVariable UUID id) {
        return useCase.obter(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_SECRETARIA_PAROQUIAL','GLOBAL_BISPO_DIOCESANO')")
    @Operation(summary = "Cadastra clérigo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Clérigo cadastrado com sucesso",
                    content = @Content(schema = @Schema(implementation = ClerigoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados cadastrais inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ClerigoResponse criar(@RequestBody @Valid ClerigoRequest request) {
        return useCase.criar(request, actor.resolveRequired().actor());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_PAROCO','GLOBAL_SECRETARIA_PAROQUIAL','GLOBAL_BISPO_DIOCESANO')")
    @Operation(summary = "Atualiza clérigo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Clérigo atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = ClerigoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Clérigo não encontrado")
    })
    public ClerigoResponse atualizar(
            @Parameter(description = "Identificador único do clérigo") @PathVariable UUID id,
            @RequestBody @Valid ClerigoRequest request) {
        return useCase.atualizar(id, request, actor.resolveRequired().actor());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_BISPO_DIOCESANO')")
    @Operation(summary = "Inativa logicamente clérigo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Clérigo inativado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Clérigo não encontrado")
    })
    public void inativar(
            @Parameter(description = "Identificador único do clérigo") @PathVariable UUID id) {
        var context = actor.resolveRequired();
        useCase.inativar(id, context.actor(), context.usuarioId());
    }

    @GetMapping("/{id}/historico-encargos")
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_PAROCO','GLOBAL_SECRETARIA_PAROQUIAL','GLOBAL_BISPO_DIOCESANO')")
    @Operation(summary = "Lista histórico de encargos e provisões do clérigo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Histórico de encargos recuperado com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = EncargoClericalResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Clérigo não encontrado")
    })
    public List<EncargoClericalResponse> listarEncargos(
            @Parameter(description = "Identificador único do clérigo") @PathVariable UUID id) {
        return useCase.listarEncargos(id);
    }

    @GetMapping("/solicitacoes-ajuste")
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_PAROCO','GLOBAL_SECRETARIA_PAROQUIAL','GLOBAL_BISPO_DIOCESANO')")
    @Operation(summary = "Lista solicitações de ajuste cadastral pendentes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitações pendentes recuperadas com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = SolicitacaoAjusteClericalResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public List<SolicitacaoAjusteClericalResponse> listarSolicitacoesAjuste() {
        return ajusteUseCase.listarPendentes();
    }

    @PostMapping("/solicitacoes-ajuste")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submete solicitação de ajuste cadastral pelo próprio clérigo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Solicitação de ajuste criada com sucesso",
                    content = @Content(schema = @Schema(implementation = UUID.class))),
            @ApiResponse(responseCode = "400", description = "Dados da solicitação inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public UUID solicitarAjuste(@RequestBody @Valid SolicitarAjusteClericalRequest request) {
        return ajusteUseCase.solicitar(actor.resolveRequired().usuarioId(), request);
    }

    @PostMapping("/solicitacoes-ajuste/{id}/decisao")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_PAROCO','GLOBAL_BISPO_DIOCESANO')")
    @Operation(summary = "Aprova ou rejeita solicitação de ajuste clerical")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Decisão registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da decisão inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada")
    })
    public void decidirAjuste(
            @Parameter(description = "Identificador único da solicitação") @PathVariable UUID id,
            @RequestBody @Valid DecidirAjusteClericalRequest request) {
        ajusteUseCase.decidir(id, actor.resolveRequired().usuarioId(), request.aprovar(), request.justificativa());
    }

    @GetMapping("/{clerigoId}/familiares")
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_PAROCO','GLOBAL_SECRETARIA_PAROQUIAL','GLOBAL_BISPO_DIOCESANO')")
    @Operation(summary = "Lista vínculos familiares do clérigo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vínculos familiares recuperados com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = RelacionamentoFamiliarResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Clérigo não encontrado")
    })
    public List<RelacionamentoFamiliarResponse> listarFamiliares(
            @Parameter(description = "Identificador único do clérigo") @PathVariable UUID clerigoId) {
        return familiaUseCase.listar(clerigoId, TipoPessoaFamiliar.CLERIGO);
    }

    @PostMapping("/{clerigoId}/familiares")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_PAROCO','GLOBAL_SECRETARIA_PAROQUIAL','GLOBAL_BISPO_DIOCESANO')")
    @Operation(summary = "Adiciona vínculo familiar para o clérigo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vínculo familiar criado com sucesso",
                    content = @Content(schema = @Schema(implementation = UUID.class))),
            @ApiResponse(responseCode = "400", description = "Dados da relação inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Clérigo ou parente não encontrado")
    })
    public UUID adicionarFamiliar(
            @Parameter(description = "Identificador único do clérigo") @PathVariable UUID clerigoId,
            @RequestBody @Valid CriarRelacionamentoFamiliarRequest request) {
        return familiaUseCase.criar(
                clerigoId,
                TipoPessoaFamiliar.CLERIGO,
                request.pessoaDestinoId(),
                request.tipoPessoaDestinoEfetivo(),
                request.tipo(),
                actor.resolveRequired().usuarioId());
    }

    @DeleteMapping("/{clerigoId}/familiares/{relacaoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','GLOBAL_ADMIN','GLOBAL_PAROCO','GLOBAL_SECRETARIA_PAROQUIAL','GLOBAL_BISPO_DIOCESANO')")
    @Operation(summary = "Remove vínculo familiar do clérigo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Vínculo familiar removido com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Vínculo familiar não encontrado")
    })
    public void removerFamiliar(
            @Parameter(description = "Identificador único do clérigo") @PathVariable UUID clerigoId,
            @Parameter(description = "Identificador único da relação familiar") @PathVariable UUID relacaoId) {
        familiaUseCase.remover(relacaoId, actor.resolveRequired().usuarioId());
    }
}
