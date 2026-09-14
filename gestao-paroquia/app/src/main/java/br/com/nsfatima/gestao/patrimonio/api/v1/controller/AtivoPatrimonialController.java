package br.com.nsfatima.gestao.patrimonio.api.v1.controller;

import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoManutencaoResponse;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoPatrimonialResponse;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.CadastrarAtivoPatrimonialRequest;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.EditarDescricaoAtivoRequest;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.EnviarParaManutencaoRequest;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.MetricasOciosidadeResponse;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.ReservaImpeditivaResponse;
import br.com.nsfatima.gestao.patrimonio.application.usecase.BuscarAtivoPatrimonialUseCase;
import br.com.nsfatima.gestao.patrimonio.application.usecase.CadastrarAtivoPatrimonialUseCase;
import br.com.nsfatima.gestao.patrimonio.application.usecase.ConcluirManutencaoAtivoUseCase;
import br.com.nsfatima.gestao.patrimonio.application.usecase.EditarDescricaoAtivoUseCase;
import br.com.nsfatima.gestao.patrimonio.application.usecase.EnviarAtivoParaManutencaoUseCase;
import br.com.nsfatima.gestao.patrimonio.application.usecase.ExcluirAtivoPatrimonialUseCase;
import br.com.nsfatima.gestao.patrimonio.application.usecase.ObterMetricasOciosidadeUseCase;
import br.com.nsfatima.gestao.patrimonio.application.usecase.ListarAtivosPorGrupoParoquialUseCase;
import br.com.nsfatima.gestao.patrimonio.application.usecase.ListarHistoricoManutencaoAtivoUseCase;
import br.com.nsfatima.gestao.patrimonio.application.usecase.VerificarConflitoManutencaoUseCase;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Expõe o ciclo de vida de ativos patrimoniais: cadastro, edição restrita (apenas descrição),
 * envio/conclusão de manutenção (com pré-checagem de conflito de reservas), exclusão lógica,
 * consulta de detalhe/histórico e métricas de ociosidade. O uso temporário do bem é feito pelo
 * módulo de Reservas.
 */
@RestController
@RequestMapping("/api/v1/ativos")
@Tag(name = "Patrimônio", description = "Endpoints para gestão de ativos patrimoniais")
public class AtivoPatrimonialController {

    private static final String AUTHENTICATED = "isAuthenticated()";
    private static final String DESC_ATIVO_ID = "Identificador único do ativo";
    private static final String RESP_NAO_AUTENTICADO = "Não autenticado";
    private static final String RESP_ACESSO_NEGADO = "Acesso negado";
    private static final String RESP_ATIVO_NAO_ENCONTRADO = "Ativo não encontrado";

    private final CadastrarAtivoPatrimonialUseCase cadastrarUseCase;
    private final ListarAtivosPorGrupoParoquialUseCase listarUseCase;
    private final BuscarAtivoPatrimonialUseCase buscarUseCase;
    private final EditarDescricaoAtivoUseCase editarDescricaoUseCase;
    private final ExcluirAtivoPatrimonialUseCase excluirUseCase;
    private final EnviarAtivoParaManutencaoUseCase enviarManutencaoUseCase;
    private final ConcluirManutencaoAtivoUseCase concluirManutencaoUseCase;
    private final VerificarConflitoManutencaoUseCase verificarConflitoUseCase;
    private final ListarHistoricoManutencaoAtivoUseCase historicoManutencaoUseCase;
    private final ObterMetricasOciosidadeUseCase metricasOciosidadeUseCase;

    public AtivoPatrimonialController(CadastrarAtivoPatrimonialUseCase cadastrarUseCase,
                                      ListarAtivosPorGrupoParoquialUseCase listarUseCase,
                                      BuscarAtivoPatrimonialUseCase buscarUseCase,
                                      EditarDescricaoAtivoUseCase editarDescricaoUseCase,
                                      ExcluirAtivoPatrimonialUseCase excluirUseCase,
                                      EnviarAtivoParaManutencaoUseCase enviarManutencaoUseCase,
                                      ConcluirManutencaoAtivoUseCase concluirManutencaoUseCase,
                                      VerificarConflitoManutencaoUseCase verificarConflitoUseCase,
                                      ListarHistoricoManutencaoAtivoUseCase historicoManutencaoUseCase,
                                      ObterMetricasOciosidadeUseCase metricasOciosidadeUseCase) {
        this.cadastrarUseCase = cadastrarUseCase;
        this.listarUseCase = listarUseCase;
        this.buscarUseCase = buscarUseCase;
        this.editarDescricaoUseCase = editarDescricaoUseCase;
        this.excluirUseCase = excluirUseCase;
        this.enviarManutencaoUseCase = enviarManutencaoUseCase;
        this.concluirManutencaoUseCase = concluirManutencaoUseCase;
        this.verificarConflitoUseCase = verificarConflitoUseCase;
        this.historicoManutencaoUseCase = historicoManutencaoUseCase;
        this.metricasOciosidadeUseCase = metricasOciosidadeUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Cadastra um novo ativo patrimonial (status inicial: DISPONIVEL)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ativo cadastrado",
                    content = @Content(schema = @Schema(implementation = AtivoPatrimonialResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = RESP_NAO_AUTENTICADO),
            @ApiResponse(responseCode = "403", description = RESP_ACESSO_NEGADO)
    })
    public AtivoPatrimonialResponse cadastrar(@RequestBody @Valid CadastrarAtivoPatrimonialRequest request) {
        return cadastrarUseCase.execute(request);
    }

    @GetMapping
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Lista todos os ativos patrimoniais não excluídos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = AtivoPatrimonialResponse.class)))),
            @ApiResponse(responseCode = "401", description = RESP_NAO_AUTENTICADO),
            @ApiResponse(responseCode = "403", description = RESP_ACESSO_NEGADO)
    })
    public List<AtivoPatrimonialResponse> listarTodos() {
        return listarUseCase.executeTodos();
    }

    @GetMapping("/organizacao/{grupoParoquialId}")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Lista o inventário de ativos de uma unidade territorial")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Inventário retornado",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = AtivoPatrimonialResponse.class)))),
            @ApiResponse(responseCode = "401", description = RESP_NAO_AUTENTICADO),
            @ApiResponse(responseCode = "403", description = RESP_ACESSO_NEGADO)
    })
    public List<AtivoPatrimonialResponse> listarPorGrupoParoquial(
            @Parameter(description = "Identificador da unidade territorial proprietária", required = true)
            @PathVariable UUID grupoParoquialId) {
        return listarUseCase.executePorGrupo(grupoParoquialId);
    }

    @GetMapping("/{id}")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Detalhe de um ativo patrimonial")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ativo retornado",
                    content = @Content(schema = @Schema(implementation = AtivoPatrimonialResponse.class))),
            @ApiResponse(responseCode = "401", description = RESP_NAO_AUTENTICADO),
            @ApiResponse(responseCode = "403", description = RESP_ACESSO_NEGADO),
            @ApiResponse(responseCode = "404", description = RESP_ATIVO_NAO_ENCONTRADO)
    })
    public AtivoPatrimonialResponse buscar(
            @Parameter(description = DESC_ATIVO_ID, required = true) @PathVariable UUID id) {
        return buscarUseCase.execute(id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Edita o ativo — apenas o campo descrição é aceito (imutabilidade estrutural)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Descrição atualizada",
                    content = @Content(schema = @Schema(implementation = AtivoPatrimonialResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = RESP_NAO_AUTENTICADO),
            @ApiResponse(responseCode = "403", description = RESP_ACESSO_NEGADO),
            @ApiResponse(responseCode = "404", description = RESP_ATIVO_NAO_ENCONTRADO)
    })
    public AtivoPatrimonialResponse editarDescricao(
            @Parameter(description = DESC_ATIVO_ID, required = true) @PathVariable UUID id,
            @RequestBody @Valid EditarDescricaoAtivoRequest request) {
        return editarDescricaoUseCase.execute(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Exclusão lógica (soft delete) de um ativo — desincorporação patrimonial")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Ativo excluído"),
            @ApiResponse(responseCode = "401", description = RESP_NAO_AUTENTICADO),
            @ApiResponse(responseCode = "403", description = RESP_ACESSO_NEGADO),
            @ApiResponse(responseCode = "404", description = RESP_ATIVO_NAO_ENCONTRADO),
            @ApiResponse(responseCode = "409", description = "Exclusão bloqueada por manutenção ou reserva futura")
    })
    public void excluir(
            @Parameter(description = DESC_ATIVO_ID, required = true) @PathVariable UUID id) {
        excluirUseCase.execute(id);
    }

    @PostMapping("/{id}/manutencoes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Envia um ativo DISPONIVEL para manutenção por um período determinado")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Manutenção registrada; ativo em EM_MANUTENCAO",
                    content = @Content(schema = @Schema(implementation = AtivoManutencaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Período inválido ou dados inválidos"),
            @ApiResponse(responseCode = "401", description = RESP_NAO_AUTENTICADO),
            @ApiResponse(responseCode = "403", description = RESP_ACESSO_NEGADO),
            @ApiResponse(responseCode = "404", description = RESP_ATIVO_NAO_ENCONTRADO),
            @ApiResponse(responseCode = "409", description = "Ativo indisponível ou reservas conflitantes")
    })
    public AtivoManutencaoResponse enviarParaManutencao(
            @Parameter(description = DESC_ATIVO_ID, required = true) @PathVariable UUID id,
            @RequestBody @Valid EnviarParaManutencaoRequest request) {
        return enviarManutencaoUseCase.execute(id, request);
    }

    @GetMapping("/{id}/manutencoes")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Linha do tempo de manutenções do ativo (mais recente primeiro)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico retornado",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = AtivoManutencaoResponse.class)))),
            @ApiResponse(responseCode = "401", description = RESP_NAO_AUTENTICADO),
            @ApiResponse(responseCode = "403", description = RESP_ACESSO_NEGADO)
    })
    public List<AtivoManutencaoResponse> historicoManutencao(
            @Parameter(description = DESC_ATIVO_ID, required = true) @PathVariable UUID id) {
        return historicoManutencaoUseCase.execute(id);
    }

    @GetMapping("/{id}/manutencoes/conflitos")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Pré-checa reservas que impediriam a manutenção no intervalo informado")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de reservas impeditivas (vazia se não houver)",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = ReservaImpeditivaResponse.class)))),
            @ApiResponse(responseCode = "401", description = RESP_NAO_AUTENTICADO),
            @ApiResponse(responseCode = "403", description = RESP_ACESSO_NEGADO),
            @ApiResponse(responseCode = "404", description = RESP_ATIVO_NAO_ENCONTRADO)
    })
    public List<ReservaImpeditivaResponse> conflitosManutencao(
            @Parameter(description = DESC_ATIVO_ID, required = true) @PathVariable UUID id,
            @Parameter(description = "Início da janela de manutenção (ISO-8601)", required = true)
            @RequestParam Instant dataInicio,
            @Parameter(description = "Fim previsto da janela de manutenção (ISO-8601)", required = true)
            @RequestParam Instant dataFimPrevista) {
        return verificarConflitoUseCase.execute(id, dataInicio, dataFimPrevista);
    }

    @PatchMapping("/{id}/manutencoes/atual/concluir")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Conclui a manutenção em aberto, devolvendo o ativo para DISPONIVEL")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Manutenção concluída",
                    content = @Content(schema = @Schema(implementation = AtivoPatrimonialResponse.class))),
            @ApiResponse(responseCode = "401", description = RESP_NAO_AUTENTICADO),
            @ApiResponse(responseCode = "403", description = RESP_ACESSO_NEGADO),
            @ApiResponse(responseCode = "404", description = RESP_ATIVO_NAO_ENCONTRADO),
            @ApiResponse(responseCode = "409", description = "Não há manutenção em aberto")
    })
    public AtivoPatrimonialResponse concluirManutencao(
            @Parameter(description = DESC_ATIVO_ID, required = true) @PathVariable UUID id) {
        return concluirManutencaoUseCase.execute(id);
    }

    @GetMapping("/metricas/ociosidade")
    @PreAuthorize(AUTHENTICATED)
    @Operation(summary = "Métricas de ociosidade do acervo patrimonial")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Métricas retornadas",
                    content = @Content(schema = @Schema(implementation = MetricasOciosidadeResponse.class))),
            @ApiResponse(responseCode = "401", description = RESP_NAO_AUTENTICADO),
            @ApiResponse(responseCode = "403", description = RESP_ACESSO_NEGADO)
    })
    public MetricasOciosidadeResponse metricasOciosidade() {
        return metricasOciosidadeUseCase.execute();
    }
}
