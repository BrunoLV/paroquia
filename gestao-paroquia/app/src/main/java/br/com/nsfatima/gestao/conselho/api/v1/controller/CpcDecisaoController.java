package br.com.nsfatima.gestao.conselho.api.v1.controller;

import br.com.nsfatima.gestao.conselho.api.v1.dto.CreateCpcDecisaoRequest;
import br.com.nsfatima.gestao.conselho.api.v1.dto.CpcDecisaoResponse;
import br.com.nsfatima.gestao.conselho.api.v1.dto.HomologarCpcDecisaoRequest;
import br.com.nsfatima.gestao.conselho.application.usecase.CreateCpcDecisaoUseCase;
import br.com.nsfatima.gestao.conselho.application.usecase.HomologarCpcDecisaoUseCase;
import br.com.nsfatima.gestao.conselho.application.usecase.ListCpcDecisoesUseCase;
import br.com.nsfatima.gestao.conselho.domain.model.StatusHomologacao;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints REST para atas, decisões e homologações dos conselhos paroquiais (CPC/CPP).
 * Materializa o fluxo de duas etapas do contexto: qualquer membro autorizado do conselho
 * pode registrar uma decisão ({@code POST}), mas apenas Pároco, Admin ou Mesa CPP podem
 * homologá-la ou vetá-la ({@code PATCH .../homologacao}) para que produza efeito oficial.
 */
@RestController
@RequestMapping("/api/v1/conselhos/decisoes")
@Tag(name = "Conselhos", description = "Endpoints para atas, decisões e homologações do CPP e CPC")
public class CpcDecisaoController {

    private final CreateCpcDecisaoUseCase createCpcDecisaoUseCase;
    private final HomologarCpcDecisaoUseCase homologarCpcDecisaoUseCase;
    private final ListCpcDecisoesUseCase listCpcDecisoesUseCase;

    public CpcDecisaoController(
            CreateCpcDecisaoUseCase createCpcDecisaoUseCase,
            HomologarCpcDecisaoUseCase homologarCpcDecisaoUseCase,
            ListCpcDecisoesUseCase listCpcDecisoesUseCase) {
        this.createCpcDecisaoUseCase = createCpcDecisaoUseCase;
        this.homologarCpcDecisaoUseCase = homologarCpcDecisaoUseCase;
        this.listCpcDecisoesUseCase = listCpcDecisoesUseCase;
    }

    /**
     * Lista atas/decisões de conselho, opcionalmente filtradas por comunidade ou por status
     * de homologação (ver {@link ListCpcDecisoesUseCase} para a regra de precedência dos filtros).
     *
     * @param comunidadeId filtro opcional por comunidade
     * @param status filtro opcional por status de homologação
     * @return decisões correspondentes ao filtro, mais recentes primeiro
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista as atas e decisões dos conselhos com filtros por comunidade ou status")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Decisões listadas com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CpcDecisaoResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public List<CpcDecisaoResponse> list(
            @Parameter(description = "Filtro opcional por comunidade")
            @RequestParam(required = false) UUID comunidadeId,
            @Parameter(description = "Filtro opcional por status de homologação")
            @RequestParam(required = false) StatusHomologacao status) {
        return listCpcDecisoesUseCase.execute(comunidadeId, status);
    }

    /**
     * Registra uma nova ata/decisão de conselho, sempre nascendo como
     * {@link br.com.nsfatima.gestao.conselho.domain.model.StatusHomologacao#PENDENTE_HOMOLOGACAO}.
     * Acessível a membros e presidência do CPC/CPP, além de Pároco e Admin — mas o registro
     * em si não aprova a decisão, apenas a documenta para posterior homologação.
     *
     * @param request dados da pauta/ata a registrar
     * @return a decisão criada, com status pendente de homologação
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'PRESIDENTE_CPC', 'MESA_CPP', 'MEMBRO_CPC', 'MEMBRO_CPP')")
    @Operation(summary = "Registra uma nova ata ou decisão de conselho (padrão: PENDENTE_HOMOLOGACAO)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Decisão registrada com sucesso",
                    content = @Content(schema = @Schema(implementation = CpcDecisaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public CpcDecisaoResponse create(@RequestBody @Valid CreateCpcDecisaoRequest request) {
        return createCpcDecisaoUseCase.execute(request);
    }

    /**
     * Homologa ou veta uma ata/decisão, restrito a Pároco, Admin ou Mesa CPP — a autoridade
     * exclusiva que confere efeito oficial à decisão. Vetar sem informar o motivo é rejeitado
     * pela camada de aplicação ({@link HomologarCpcDecisaoUseCase}).
     *
     * @param id identificador da decisão a homologar/vetar
     * @param request status decidido e, se veto, o motivo
     * @return a decisão já homologada/vetada
     */
    @PatchMapping("/{id}/homologacao")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLERO_PAROCO', 'MESA_CPP')")
    @Operation(summary = "Homologa ou veta uma ata/decisão de conselho (exclusivo para Pároco, Admin ou Mesa CPP)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Decisão homologada ou vetada com sucesso",
                    content = @Content(schema = @Schema(implementation = CpcDecisaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida ou veto sem justificativa"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Decisão não encontrada")
    })
    public CpcDecisaoResponse homologar(
            @Parameter(description = "Identificador único da decisão", required = true)
            @PathVariable UUID id,
            @RequestBody @Valid HomologarCpcDecisaoRequest request) {
        return homologarCpcDecisaoUseCase.execute(id, request);
    }
}
