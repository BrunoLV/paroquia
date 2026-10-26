package br.com.nsfatima.gestao.conselho.api.v1.controller;

import br.com.nsfatima.gestao.conselho.api.v1.dto.AtaResponse;
import br.com.nsfatima.gestao.conselho.api.v1.dto.ConteudoAtaRequest;
import br.com.nsfatima.gestao.conselho.api.v1.dto.PaginaAtasResponse;
import br.com.nsfatima.gestao.conselho.api.v1.dto.RascunharAtaRequest;
import br.com.nsfatima.gestao.conselho.api.v1.dto.RegistroAuditoriaResponse;
import br.com.nsfatima.gestao.conselho.api.v1.dto.RetificarAtaRequest;
import br.com.nsfatima.gestao.conselho.api.v1.dto.TipoReuniaoResponse;
import br.com.nsfatima.gestao.conselho.api.v1.dto.VetarAtaRequest;
import br.com.nsfatima.gestao.conselho.api.v1.mapper.AtaApiMapper;
import br.com.nsfatima.gestao.conselho.application.dto.RascunharAtaComando;
import br.com.nsfatima.gestao.conselho.application.usecase.AtualizarRascunhoAtaUseCase;
import br.com.nsfatima.gestao.conselho.application.usecase.HomologarAtaUseCase;
import br.com.nsfatima.gestao.conselho.application.usecase.ListarAtasUseCase;
import br.com.nsfatima.gestao.conselho.application.usecase.ListarTiposReuniaoUseCase;
import br.com.nsfatima.gestao.conselho.application.usecase.ListarTrilhaAtaUseCase;
import br.com.nsfatima.gestao.conselho.application.usecase.ObterAtaUseCase;
import br.com.nsfatima.gestao.conselho.application.usecase.RascunharAtaUseCase;
import br.com.nsfatima.gestao.conselho.application.usecase.ReabrirAtaUseCase;
import br.com.nsfatima.gestao.conselho.application.usecase.RetificarAtaUseCase;
import br.com.nsfatima.gestao.conselho.application.usecase.SubmeterAtaUseCase;
import br.com.nsfatima.gestao.conselho.application.usecase.VetarAtaUseCase;
import br.com.nsfatima.gestao.conselho.domain.model.StatusAta;
import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Endpoints das atas de reunião da paróquia e das comunidades. A autorização fina (papel, escopo por órgão e
 * estado da ata) é decidida na camada de aplicação pela {@code AutorizacaoAtaPolicy}; aqui basta estar
 * autenticado. Quem não pode ler uma ata recebe 404, para não revelar a existência de atas de outros órgãos.
 */
@RestController
@RequestMapping("/api/v1/conselhos")
@Validated
@Tag(name = "Conselhos", description = "Atas de reunião, deliberações, homologação e exportação")
public class AtaReuniaoController {

    private static final String RESPOSTA_ATA = "Ata";
    private static final String NAO_AUTENTICADO = "Não autenticado";
    private static final String ACESSO_NEGADO = "Perfil sem permissão para a operação";
    private static final String NAO_ENCONTRADA = "Ata não encontrada ou fora do escopo do usuário";
    private static final String CONFLITO = "Operação incompatível com a situação da ata";
    private static final String INVALIDA = "Conteúdo da ata viola uma regra do tipo de reunião";
    private static final String DESC_ID = "Identificador da ata";

    private final RascunharAtaUseCase rascunharUseCase;
    private final AtualizarRascunhoAtaUseCase atualizarUseCase;
    private final SubmeterAtaUseCase submeterUseCase;
    private final HomologarAtaUseCase homologarUseCase;
    private final VetarAtaUseCase vetarUseCase;
    private final ReabrirAtaUseCase reabrirUseCase;
    private final RetificarAtaUseCase retificarUseCase;
    private final ObterAtaUseCase obterUseCase;
    private final ListarAtasUseCase listarUseCase;
    private final ListarTrilhaAtaUseCase trilhaUseCase;
    private final ListarTiposReuniaoUseCase tiposUseCase;
    private final AtaApiMapper mapper;

    public AtaReuniaoController(RascunharAtaUseCase rascunhar, AtualizarRascunhoAtaUseCase atualizar,
                                SubmeterAtaUseCase submeter, HomologarAtaUseCase homologar, VetarAtaUseCase vetar,
                                ReabrirAtaUseCase reabrir, RetificarAtaUseCase retificar, ObterAtaUseCase obter,
                                ListarAtasUseCase listar, ListarTrilhaAtaUseCase trilha,
                                ListarTiposReuniaoUseCase tipos, AtaApiMapper mapper) {
        this.rascunharUseCase = rascunhar;
        this.atualizarUseCase = atualizar;
        this.submeterUseCase = submeter;
        this.homologarUseCase = homologar;
        this.vetarUseCase = vetar;
        this.reabrirUseCase = reabrir;
        this.retificarUseCase = retificar;
        this.obterUseCase = obter;
        this.listarUseCase = listar;
        this.trilhaUseCase = trilha;
        this.tiposUseCase = tipos;
        this.mapper = mapper;
    }

    @GetMapping("/tipos-reuniao")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista os tipos de reunião e as regras de cada um",
            description = "Homologação, quórum, modo de presença, votação, órgãos convocantes e redatores por tipo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tipos listados",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = TipoReuniaoResponse.class)))),
            @ApiResponse(responseCode = "401", description = NAO_AUTENTICADO)
    })
    public List<TipoReuniaoResponse> listarTipos() {
        return tiposUseCase.execute().stream().map(mapper::paraTipo).toList();
    }

    @GetMapping("/atas")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista as atas visíveis ao usuário",
            description = "O escopo é aplicado na consulta: clero e secretaria paroquial veem todas; os demais, "
                    + "só as dos órgãos a que estão vinculados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de atas",
                    content = @Content(schema = @Schema(implementation = PaginaAtasResponse.class))),
            @ApiResponse(responseCode = "401", description = NAO_AUTENTICADO)
    })
    public PaginaAtasResponse listarAtas(
            @Parameter(description = "Órgão convocante") @RequestParam(required = false) UUID organizacaoId,
            @Parameter(description = "Tipo de reunião") @RequestParam(required = false) TipoReuniao tipo,
            @Parameter(description = "Situação da ata") @RequestParam(required = false) StatusAta status,
            @Parameter(description = "Início do período da reunião")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @Parameter(description = "Fim do período da reunião")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @Parameter(description = "Página, iniciada em zero") @RequestParam(defaultValue = "0") @Min(0) int pagina,
            @Parameter(description = "Itens por página") @RequestParam(defaultValue = "20") @Min(1) @Max(100)
            int tamanho) {
        return mapper.paraPagina(listarUseCase.execute(organizacaoId, tipo, status, de, ate, pagina, tamanho),
                pagina, tamanho);
    }

    @GetMapping("/atas/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtém uma ata com as ações permitidas ao usuário")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = RESPOSTA_ATA,
                    content = @Content(schema = @Schema(implementation = AtaResponse.class))),
            @ApiResponse(responseCode = "401", description = NAO_AUTENTICADO),
            @ApiResponse(responseCode = "404", description = NAO_ENCONTRADA)
    })
    public AtaResponse obterAta(@Parameter(description = DESC_ID, required = true) @PathVariable UUID id) {
        return mapper.paraResposta(obterUseCase.execute(id));
    }

    @PostMapping("/atas")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Abre uma ata em rascunho",
            description = "Só o redator do tipo, no órgão convocante, pode abrir. Status e redator são definidos "
                    + "pelo servidor.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Rascunho criado",
                    content = @Content(schema = @Schema(implementation = AtaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requisição malformada"),
            @ApiResponse(responseCode = "401", description = NAO_AUTENTICADO),
            @ApiResponse(responseCode = "403", description = ACESSO_NEGADO),
            @ApiResponse(responseCode = "422", description = INVALIDA)
    })
    public AtaResponse criar(@RequestBody @Valid RascunharAtaRequest request) {
        return mapper.paraResposta(rascunharUseCase.execute(new RascunharAtaComando(request.tipo(),
                request.organizacaoConvocanteId(), request.organizacaoAssociadaId(),
                mapper.paraConteudo(request.conteudo()))));
    }

    @PutMapping("/atas/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Substitui o conteúdo de uma ata em rascunho")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rascunho atualizado",
                    content = @Content(schema = @Schema(implementation = AtaResponse.class))),
            @ApiResponse(responseCode = "401", description = NAO_AUTENTICADO),
            @ApiResponse(responseCode = "403", description = ACESSO_NEGADO),
            @ApiResponse(responseCode = "404", description = NAO_ENCONTRADA),
            @ApiResponse(responseCode = "409", description = CONFLITO),
            @ApiResponse(responseCode = "422", description = INVALIDA)
    })
    public AtaResponse atualizar(@Parameter(description = DESC_ID, required = true) @PathVariable UUID id,
                                 @RequestBody @Valid ConteudoAtaRequest request) {
        return mapper.paraResposta(atualizarUseCase.execute(id, mapper.paraConteudo(request)));
    }

    @PostMapping("/atas/{id}/submissao")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Conclui a redação e submete a ata",
            description = "Leva à homologação do Pároco ou, nos tipos que a dispensam, registra e sela a ata.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ata submetida",
                    content = @Content(schema = @Schema(implementation = AtaResponse.class))),
            @ApiResponse(responseCode = "401", description = NAO_AUTENTICADO),
            @ApiResponse(responseCode = "403", description = ACESSO_NEGADO),
            @ApiResponse(responseCode = "404", description = NAO_ENCONTRADA),
            @ApiResponse(responseCode = "409", description = CONFLITO),
            @ApiResponse(responseCode = "422", description = INVALIDA)
    })
    public AtaResponse submeter(@Parameter(description = DESC_ID, required = true) @PathVariable UUID id) {
        return mapper.paraResposta(submeterUseCase.execute(id));
    }

    @PostMapping("/atas/{id}/homologacao")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Homologa a ata (privativo do Pároco)",
            description = "Sela a ata e lhe atribui o número sequencial da série do órgão no ano.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ata homologada",
                    content = @Content(schema = @Schema(implementation = AtaResponse.class))),
            @ApiResponse(responseCode = "401", description = NAO_AUTENTICADO),
            @ApiResponse(responseCode = "403", description = ACESSO_NEGADO),
            @ApiResponse(responseCode = "404", description = NAO_ENCONTRADA),
            @ApiResponse(responseCode = "409", description = CONFLITO)
    })
    public AtaResponse homologar(@Parameter(description = DESC_ID, required = true) @PathVariable UUID id) {
        return mapper.paraResposta(homologarUseCase.execute(id));
    }

    @PostMapping("/atas/{id}/veto")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Veta a ata (privativo do Pároco), com motivo obrigatório")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ata vetada",
                    content = @Content(schema = @Schema(implementation = AtaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Motivo ausente"),
            @ApiResponse(responseCode = "401", description = NAO_AUTENTICADO),
            @ApiResponse(responseCode = "403", description = ACESSO_NEGADO),
            @ApiResponse(responseCode = "404", description = NAO_ENCONTRADA),
            @ApiResponse(responseCode = "409", description = CONFLITO)
    })
    public AtaResponse vetar(@Parameter(description = DESC_ID, required = true) @PathVariable UUID id,
                             @RequestBody @Valid VetarAtaRequest request) {
        return mapper.paraResposta(vetarUseCase.execute(id, request.motivo()));
    }

    @PostMapping("/atas/{id}/reabertura")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Devolve ao redator uma ata vetada, mantendo o motivo do veto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ata reaberta",
                    content = @Content(schema = @Schema(implementation = AtaResponse.class))),
            @ApiResponse(responseCode = "401", description = NAO_AUTENTICADO),
            @ApiResponse(responseCode = "403", description = ACESSO_NEGADO),
            @ApiResponse(responseCode = "404", description = NAO_ENCONTRADA),
            @ApiResponse(responseCode = "409", description = CONFLITO)
    })
    public AtaResponse reabrir(@Parameter(description = DESC_ID, required = true) @PathVariable UUID id) {
        return mapper.paraResposta(reabrirUseCase.execute(id));
    }

    @PostMapping("/atas/{id}/retificacao")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Abre a ata retificadora de uma ata selada",
            description = "A original permanece intacta; a retificadora nasce em rascunho, ligada a ela.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Ata retificadora criada",
                    content = @Content(schema = @Schema(implementation = AtaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Justificativa ausente"),
            @ApiResponse(responseCode = "401", description = NAO_AUTENTICADO),
            @ApiResponse(responseCode = "403", description = ACESSO_NEGADO),
            @ApiResponse(responseCode = "404", description = NAO_ENCONTRADA),
            @ApiResponse(responseCode = "409", description = CONFLITO)
    })
    public AtaResponse retificar(@Parameter(description = DESC_ID, required = true) @PathVariable UUID id,
                                 @RequestBody @Valid RetificarAtaRequest request) {
        return mapper.paraResposta(retificarUseCase.execute(id, request.justificativa()));
    }

    @GetMapping("/atas/{id}/historico")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Consulta a trilha de auditoria da ata",
            description = "Restrita a Pároco, Bispo diocesano e secretaria paroquial.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Trilha em ordem cronológica",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = RegistroAuditoriaResponse.class)))),
            @ApiResponse(responseCode = "401", description = NAO_AUTENTICADO),
            @ApiResponse(responseCode = "403", description = ACESSO_NEGADO),
            @ApiResponse(responseCode = "404", description = NAO_ENCONTRADA)
    })
    public List<RegistroAuditoriaResponse> historico(
            @Parameter(description = DESC_ID, required = true) @PathVariable UUID id) {
        return trilhaUseCase.execute(id).stream().map(mapper::paraTrilha).toList();
    }
}
