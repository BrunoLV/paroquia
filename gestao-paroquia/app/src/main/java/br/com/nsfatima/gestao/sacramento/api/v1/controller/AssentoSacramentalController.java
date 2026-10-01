package br.com.nsfatima.gestao.sacramento.api.v1.controller;

import br.com.nsfatima.gestao.sacramento.api.v1.dto.AssentoResponse;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.AssentoResumoResponse;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.AtualizarRascunhoRequest;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.AverbacaoRequest;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.CancelamentoRequest;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.CertidaoResponse;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.HistoricoResponse;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.LavrarAssentoRequest;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.NotificacaoRequest;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.RetificacaoRequest;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.VincularParticipanteRequest;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.VinculoPendenteResponse;
import br.com.nsfatima.gestao.sacramento.application.usecase.AtualizarRascunhoUseCase;
import br.com.nsfatima.gestao.sacramento.application.usecase.AverbarBatismoUseCase;
import br.com.nsfatima.gestao.sacramento.application.usecase.CancelarAssentoUseCase;
import br.com.nsfatima.gestao.sacramento.application.usecase.ConsultarAssentosUseCase;
import br.com.nsfatima.gestao.sacramento.application.usecase.EmitirCertidaoUseCase;
import br.com.nsfatima.gestao.sacramento.application.usecase.HomologarAssentoUseCase;
import br.com.nsfatima.gestao.sacramento.application.usecase.ListarVinculosPendentesUseCase;
import br.com.nsfatima.gestao.sacramento.application.usecase.LavrarAssentoUseCase;
import br.com.nsfatima.gestao.sacramento.application.usecase.RegistrarNotificacaoCanonicaUseCase;
import br.com.nsfatima.gestao.sacramento.application.usecase.RetificarAssentoUseCase;
import br.com.nsfatima.gestao.sacramento.application.usecase.VincularParticipanteUseCase;
import br.com.nsfatima.gestao.sacramento.domain.model.TipoSacramento;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Livro de assentos sacramentais. Não há verbo DELETE: o registro é perpétuo (Cân. 535) e só admite
 * cancelamento motivado. Leitura e escrituração preparatória: Pároco e Secretaria; fé pública
 * (homologar, retificar, cancelar): exclusivamente o Pároco. ADMIN não tem acesso.
 */
@RestController
@RequestMapping("/api/v1/sacramentos")
@Tag(name = "Sacramentos", description = "Assentos sacramentais: lavratura, homologação, averbações e certidões")
public class AssentoSacramentalController {

    private static final String LEITURA = "hasAnyRole('PAROCO', 'GLOBAL_PAROCO', 'SECRETARIA_PAROQUIAL',"
            + " 'GLOBAL_SECRETARIA_PAROQUIAL', 'BISPO_DIOCESANO', 'GLOBAL_BISPO_DIOCESANO')";
    private static final String ESCRITURACAO = "hasAnyRole('PAROCO', 'GLOBAL_PAROCO', 'SECRETARIA_PAROQUIAL',"
            + " 'GLOBAL_SECRETARIA_PAROQUIAL')";
    private static final String PAROCO = "hasAnyRole('PAROCO', 'GLOBAL_PAROCO')";
    private static final String ID_ASSENTO = "Identificador do assento sacramental";
    private static final String ID_PARTICIPANTE = "Identificador do participante";
    private static final String R_OK = "Operação realizada";
    private static final String R_400 = "Requisição inválida";
    private static final String R_401 = "Não autenticado";
    private static final String R_403 = "Acesso negado ao perfil";
    private static final String R_404 = "Assento inexistente ou inacessível";
    private static final String R_409 = "Estado do assento não permite a operação";
    private static final String R_422 = "Requisitos canônicos não atendidos";

    private final LavrarAssentoUseCase lavrarUseCase;
    private final AtualizarRascunhoUseCase atualizarUseCase;
    private final HomologarAssentoUseCase homologarUseCase;
    private final VincularParticipanteUseCase vincularUseCase;
    private final AverbarBatismoUseCase averbarUseCase;
    private final RetificarAssentoUseCase retificarUseCase;
    private final CancelarAssentoUseCase cancelarUseCase;
    private final RegistrarNotificacaoCanonicaUseCase notificarUseCase;
    private final ConsultarAssentosUseCase consultar;
    private final EmitirCertidaoUseCase certidaoUseCase;
    private final ListarVinculosPendentesUseCase vinculosPendentesUseCase;

    public AssentoSacramentalController(LavrarAssentoUseCase lavrar, AtualizarRascunhoUseCase atualizar,
                                        HomologarAssentoUseCase homologar, VincularParticipanteUseCase vincular,
                                        AverbarBatismoUseCase averbar, RetificarAssentoUseCase retificar,
                                        CancelarAssentoUseCase cancelar, RegistrarNotificacaoCanonicaUseCase notificar,
                                        ConsultarAssentosUseCase consultar, EmitirCertidaoUseCase certidao,
                                        ListarVinculosPendentesUseCase vinculosPendentes) {
        this.lavrarUseCase = lavrar;
        this.atualizarUseCase = atualizar;
        this.homologarUseCase = homologar;
        this.vincularUseCase = vincular;
        this.averbarUseCase = averbar;
        this.retificarUseCase = retificar;
        this.cancelarUseCase = cancelar;
        this.notificarUseCase = notificar;
        this.consultar = consultar;
        this.certidaoUseCase = certidao;
        this.vinculosPendentesUseCase = vinculosPendentes;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ESCRITURACAO)
    @Operation(summary = "Lavra rascunho de assento", description = "Cria o assento em DRAFT; cadastro de fiel é opcional.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Rascunho lavrado"),
            @ApiResponse(responseCode = "400", description = R_400), @ApiResponse(responseCode = "401", description = R_401),
            @ApiResponse(responseCode = "403", description = R_403),
            @ApiResponse(responseCode = "422", description = R_422)})
    public AssentoResponse lavrar(@Valid @RequestBody LavrarAssentoRequest request) {
        return lavrarUseCase.execute(request);
    }

    @GetMapping
    @PreAuthorize(LEITURA)
    @Operation(summary = "Lista assentos", description = "Livro reservado só aparece a Pároco e Bispo Diocesano.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = R_OK),
            @ApiResponse(responseCode = "401", description = R_401), @ApiResponse(responseCode = "403", description = R_403)})
    public List<AssentoResumoResponse> listar(
            @Parameter(description = "Filtra por sacramento") @RequestParam(required = false) TipoSacramento tipo) {
        return consultar.listar(tipo);
    }

    @GetMapping("/vinculos-pendentes")
    @PreAuthorize(PAROCO)
    @Operation(summary = "Vínculos propostos aguardando aprovação", description = "Fila da tela de Aprovações; exclusiva do Pároco.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = R_OK),
            @ApiResponse(responseCode = "401", description = R_401), @ApiResponse(responseCode = "403", description = R_403)})
    public List<VinculoPendenteResponse> vinculosPendentes() {
        return vinculosPendentesUseCase.execute();
    }

    @GetMapping("/{id}")
    @PreAuthorize(LEITURA)
    @Operation(summary = "Detalha assento")
    @ApiResponses({@ApiResponse(responseCode = "200", description = R_OK),
            @ApiResponse(responseCode = "401", description = R_401), @ApiResponse(responseCode = "403", description = R_403),
            @ApiResponse(responseCode = "404", description = R_404)})
    public AssentoResponse buscar(@Parameter(description = ID_ASSENTO) @PathVariable UUID id) {
        return consultar.buscar(id);
    }

    @GetMapping("/vinculados/{pessoaId}")
    @PreAuthorize(LEITURA)
    @Operation(summary = "Assentos vinculados a um fiel ou clérigo", description = "Visão 360º pelo vínculo tardio.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = R_OK),
            @ApiResponse(responseCode = "401", description = R_401), @ApiResponse(responseCode = "403", description = R_403)})
    public List<AssentoResumoResponse> porPessoa(
            @Parameter(description = "Identificador do fiel ou clérigo") @PathVariable UUID pessoaId) {
        return consultar.porPessoa(pessoaId);
    }

    @PutMapping("/{id}")
    @PreAuthorize(ESCRITURACAO)
    @Operation(summary = "Atualiza teor do rascunho", description = "Assento selado rejeita a edição (409).")
    @ApiResponses({@ApiResponse(responseCode = "200", description = R_OK),
            @ApiResponse(responseCode = "400", description = R_400), @ApiResponse(responseCode = "401", description = R_401),
            @ApiResponse(responseCode = "403", description = R_403), @ApiResponse(responseCode = "404", description = R_404),
            @ApiResponse(responseCode = "409", description = R_409)})
    public AssentoResponse atualizar(@Parameter(description = ID_ASSENTO) @PathVariable UUID id,
                                     @Valid @RequestBody AtualizarRascunhoRequest request) {
        return atualizarUseCase.execute(id, request);
    }

    @PostMapping("/{id}/homologacao")
    @PreAuthorize(PAROCO)
    @Operation(summary = "Homologa (sela) o assento", description = "Ato privativo do Pároco; torna o teor somente leitura.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = R_OK),
            @ApiResponse(responseCode = "401", description = R_401), @ApiResponse(responseCode = "403", description = R_403),
            @ApiResponse(responseCode = "404", description = R_404), @ApiResponse(responseCode = "409", description = R_409),
            @ApiResponse(responseCode = "422", description = R_422)})
    public AssentoResponse homologar(@Parameter(description = ID_ASSENTO) @PathVariable UUID id) {
        return homologarUseCase.execute(id);
    }

    @PatchMapping("/{id}/participantes/{participanteId}/vinculo")
    @PreAuthorize(ESCRITURACAO)
    @Operation(summary = "Vincula participante a fiel ou clérigo",
            description = "Pároco vincula direto; o vínculo da Secretaria fica pendente de aprovação.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = R_OK),
            @ApiResponse(responseCode = "400", description = R_400), @ApiResponse(responseCode = "401", description = R_401),
            @ApiResponse(responseCode = "403", description = R_403), @ApiResponse(responseCode = "404", description = R_404),
            @ApiResponse(responseCode = "409", description = R_409)})
    public AssentoResponse vincular(@Parameter(description = ID_ASSENTO) @PathVariable UUID id,
                                    @Parameter(description = ID_PARTICIPANTE) @PathVariable UUID participanteId,
                                    @RequestBody VincularParticipanteRequest request) {
        return vincularUseCase.vincular(id, participanteId, request);
    }

    @PostMapping("/{id}/participantes/{participanteId}/vinculo/aprovacao")
    @PreAuthorize(PAROCO)
    @Operation(summary = "Aprova vínculo proposto pela Secretaria", description = "Ato privativo do Pároco.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = R_OK),
            @ApiResponse(responseCode = "401", description = R_401), @ApiResponse(responseCode = "403", description = R_403),
            @ApiResponse(responseCode = "404", description = R_404), @ApiResponse(responseCode = "409", description = R_409)})
    public AssentoResponse aprovarVinculo(@Parameter(description = ID_ASSENTO) @PathVariable UUID id,
                                          @Parameter(description = ID_PARTICIPANTE) @PathVariable UUID participanteId) {
        return vincularUseCase.aprovar(id, participanteId);
    }

    @PostMapping("/{id}/participantes/{participanteId}/vinculo/rejeicao")
    @PreAuthorize(PAROCO)
    @Operation(summary = "Rejeita vínculo proposto pela Secretaria", description = "Remove o ponteiro; ato do Pároco.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = R_OK),
            @ApiResponse(responseCode = "401", description = R_401), @ApiResponse(responseCode = "403", description = R_403),
            @ApiResponse(responseCode = "404", description = R_404), @ApiResponse(responseCode = "409", description = R_409)})
    public AssentoResponse rejeitarVinculo(@Parameter(description = ID_ASSENTO) @PathVariable UUID id,
                                           @Parameter(description = ID_PARTICIPANTE) @PathVariable UUID participanteId) {
        return vincularUseCase.rejeitar(id, participanteId);
    }

    @PostMapping("/{id}/participantes/{participanteId}/desvinculo")
    @PreAuthorize(PAROCO)
    @Operation(summary = "Desvincula participante", description = "Ato do Pároco; remove só o ponteiro, o texto permanece.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = R_OK),
            @ApiResponse(responseCode = "401", description = R_401), @ApiResponse(responseCode = "403", description = R_403),
            @ApiResponse(responseCode = "404", description = R_404), @ApiResponse(responseCode = "409", description = R_409)})
    public AssentoResponse desvincular(@Parameter(description = ID_ASSENTO) @PathVariable UUID id,
                                       @Parameter(description = ID_PARTICIPANTE) @PathVariable UUID participanteId) {
        return vincularUseCase.desvincular(id, participanteId);
    }

    @PostMapping("/{id}/averbacoes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ESCRITURACAO)
    @Operation(summary = "Digita averbação marginal no Batismo", description = "Fica pendente até homologação do Pároco.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Averbação digitada"),
            @ApiResponse(responseCode = "400", description = R_400), @ApiResponse(responseCode = "401", description = R_401),
            @ApiResponse(responseCode = "403", description = R_403), @ApiResponse(responseCode = "404", description = R_404),
            @ApiResponse(responseCode = "409", description = R_409)})
    public AssentoResponse averbar(@Parameter(description = ID_ASSENTO) @PathVariable UUID id,
                                   @Valid @RequestBody AverbacaoRequest request) {
        return averbarUseCase.inserir(id, request);
    }

    @PostMapping("/{id}/averbacoes/{averbacaoId}/homologacao")
    @PreAuthorize(PAROCO)
    @Operation(summary = "Homologa averbação", description = "Ato privativo do Pároco.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = R_OK),
            @ApiResponse(responseCode = "401", description = R_401), @ApiResponse(responseCode = "403", description = R_403),
            @ApiResponse(responseCode = "404", description = R_404), @ApiResponse(responseCode = "409", description = R_409)})
    public AssentoResponse homologarAverbacao(@Parameter(description = ID_ASSENTO) @PathVariable UUID id,
                                              @Parameter(description = "Identificador da averbação") @PathVariable UUID averbacaoId) {
        return averbarUseCase.homologar(id, averbacaoId);
    }

    @PostMapping("/{id}/retificacoes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(PAROCO)
    @Operation(summary = "Retifica termo selado por ressalva", description = "Não sobrescreve o teor original.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Ressalva registrada"),
            @ApiResponse(responseCode = "400", description = R_400), @ApiResponse(responseCode = "401", description = R_401),
            @ApiResponse(responseCode = "403", description = R_403), @ApiResponse(responseCode = "404", description = R_404),
            @ApiResponse(responseCode = "409", description = R_409)})
    public AssentoResponse retificar(@Parameter(description = ID_ASSENTO) @PathVariable UUID id,
                                     @Valid @RequestBody RetificacaoRequest request) {
        return retificarUseCase.execute(id, request);
    }

    @PostMapping("/{id}/cancelamento")
    @PreAuthorize(PAROCO)
    @Operation(summary = "Cancela termo", description = "Duplicidade ou erro formal grave; exige motivo. Nunca exclui.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = R_OK),
            @ApiResponse(responseCode = "400", description = R_400), @ApiResponse(responseCode = "401", description = R_401),
            @ApiResponse(responseCode = "403", description = R_403), @ApiResponse(responseCode = "404", description = R_404),
            @ApiResponse(responseCode = "409", description = R_409)})
    public AssentoResponse cancelar(@Parameter(description = ID_ASSENTO) @PathVariable UUID id,
                                    @Valid @RequestBody CancelamentoRequest request) {
        return cancelarUseCase.execute(id, request);
    }

    @PostMapping("/{id}/notificacoes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ESCRITURACAO)
    @Operation(summary = "Registra notificação canônica", description = "Comunicação à paróquia de batismo.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Notificação registrada"),
            @ApiResponse(responseCode = "400", description = R_400), @ApiResponse(responseCode = "401", description = R_401),
            @ApiResponse(responseCode = "403", description = R_403), @ApiResponse(responseCode = "404", description = R_404),
            @ApiResponse(responseCode = "409", description = R_409)})
    public AssentoResponse notificar(@Parameter(description = ID_ASSENTO) @PathVariable UUID id,
                                     @Valid @RequestBody NotificacaoRequest request) {
        return notificarUseCase.execute(id, request);
    }

    @GetMapping("/{id}/certidao")
    @PreAuthorize(ESCRITURACAO)
    @Operation(summary = "Emite certidão narrativa", description = "Apenas de assento homologado; omite dados sensíveis.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = R_OK),
            @ApiResponse(responseCode = "401", description = R_401), @ApiResponse(responseCode = "403", description = R_403),
            @ApiResponse(responseCode = "404", description = R_404), @ApiResponse(responseCode = "409", description = R_409)})
    public CertidaoResponse certidao(@Parameter(description = ID_ASSENTO) @PathVariable UUID id) {
        return certidaoUseCase.execute(id);
    }

    @GetMapping("/{id}/historico")
    @PreAuthorize(PAROCO)
    @Operation(summary = "Trilha de auditoria do assento", description = "Quem, perfil, IP, ação e valores prévios/posteriores.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = R_OK),
            @ApiResponse(responseCode = "401", description = R_401), @ApiResponse(responseCode = "403", description = R_403),
            @ApiResponse(responseCode = "404", description = R_404)})
    public List<HistoricoResponse> historico(@Parameter(description = ID_ASSENTO) @PathVariable UUID id) {
        return consultar.historico(id);
    }
}
