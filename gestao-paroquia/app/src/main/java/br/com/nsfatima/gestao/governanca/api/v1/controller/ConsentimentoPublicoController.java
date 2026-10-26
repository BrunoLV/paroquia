package br.com.nsfatima.gestao.governanca.api.v1.controller;

import br.com.nsfatima.gestao.governanca.api.v1.dto.ComprovanteConsentimentoResponse;
import br.com.nsfatima.gestao.governanca.api.v1.dto.CriarSolicitacaoDpoRequest;
import br.com.nsfatima.gestao.governanca.api.v1.dto.HistoricoConsentimentoResponse;
import br.com.nsfatima.gestao.governanca.api.v1.dto.ManifestarConsentimentoRequest;
import br.com.nsfatima.gestao.governanca.api.v1.dto.ResponderSolicitacaoDpoRequest;
import br.com.nsfatima.gestao.governanca.api.v1.dto.SolicitacaoDpoResponse;
import br.com.nsfatima.gestao.governanca.api.v1.dto.SolicitarCodigoConsentimentoRequest;
import br.com.nsfatima.gestao.governanca.api.v1.dto.SolicitarCodigoConsentimentoResponse;
import br.com.nsfatima.gestao.governanca.api.v1.dto.ValidarCodigoConsentimentoRequest;
import br.com.nsfatima.gestao.governanca.api.v1.dto.ValidarCodigoConsentimentoResponse;
import br.com.nsfatima.gestao.governanca.application.usecase.GerenciarConsentimentoPublicoUseCase;
import br.com.nsfatima.gestao.governanca.application.usecase.GerenciarSolicitacoesDpoUseCase;
import br.com.nsfatima.gestao.governanca.domain.model.TipoTitularConsentimento;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints públicos e administrativos para consentimento, revogação e solicitações ao DPO sob a LGPD.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Consentimento e Governança LGPD", description = "Endpoints de autoatendimento, revogação e atendimento DPO")
public class ConsentimentoPublicoController {

    private final GerenciarConsentimentoPublicoUseCase consentimentoUseCase;
    private final GerenciarSolicitacoesDpoUseCase dpoUseCase;

    public ConsentimentoPublicoController(
            GerenciarConsentimentoPublicoUseCase consentimentoUseCase,
            GerenciarSolicitacoesDpoUseCase dpoUseCase) {
        this.consentimentoUseCase = consentimentoUseCase;
        this.dpoUseCase = dpoUseCase;
    }

    @PostMapping("/publico/consentimento/solicitar-codigo")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Solicita código OTP de uso único para validação de titularidade de consentimento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Código solicitado com sucesso",
                    content = @Content(schema = @Schema(implementation = SolicitarCodigoConsentimentoResponse.class))),
            @ApiResponse(responseCode = "400", description = "CPF inválido ou não informado")
    })
    public SolicitarCodigoConsentimentoResponse solicitarCodigo(
            @RequestBody @Valid SolicitarCodigoConsentimentoRequest request) {
        return consentimentoUseCase.solicitarCodigo(request);
    }

    @PostMapping("/publico/consentimento/validar-codigo")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Valida código OTP e emite token efêmero para manifestação de consentimento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Código validado com sucesso",
                    content = @Content(schema = @Schema(implementation = ValidarCodigoConsentimentoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Código inválido ou número de tentativas excedido")
    })
    public ValidarCodigoConsentimentoResponse validarCodigo(
            @RequestBody @Valid ValidarCodigoConsentimentoRequest request) {
        return consentimentoUseCase.validarCodigo(request);
    }

    @PostMapping("/publico/consentimento/manifestar")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Manifesta expressamente a concessão ou atualização de preferências de consentimento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consentimento manifestado e registrado com sucesso",
                    content = @Content(schema = @Schema(implementation = ComprovanteConsentimentoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Token expirado ou dados de menor inválidos")
    })
    public ComprovanteConsentimentoResponse manifestar(
            @RequestHeader("X-Consent-Token") String token,
            @RequestBody @Valid ManifestarConsentimentoRequest request,
            HttpServletRequest httpRequest) {
        String ip = extrairIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        return consentimentoUseCase.manifestar(token, request, ip, userAgent);
    }

    @PostMapping("/publico/consentimento/revogar")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Revoga o consentimento pastoral em procedimento facilitado e gratuito (Art. 8º, § 5º da LGPD)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consentimento revogado com sucesso e comprovante emitido",
                    content = @Content(schema = @Schema(implementation = ComprovanteConsentimentoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Token expirado ou inválido")
    })
    public ComprovanteConsentimentoResponse revogar(
            @RequestHeader("X-Consent-Token") String token,
            HttpServletRequest httpRequest) {
        String ip = extrairIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        return consentimentoUseCase.revogar(token, ip, userAgent);
    }

    @PostMapping("/publico/dpo/solicitar")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("permitAll()")
    @Operation(summary = "Registra solicitação de exercício de direitos de titular encaminhada ao DPO (Arts. 18 e 19 LGPD)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Solicitação registrada com sucesso gerando protocolo",
                    content = @Content(schema = @Schema(implementation = SolicitacaoDpoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados da solicitação inválidos")
    })
    public SolicitacaoDpoResponse solicitarAoDpo(@RequestBody @Valid CriarSolicitacaoDpoRequest request) {
        return dpoUseCase.criarSolicitacao(request);
    }

    @GetMapping("/publico/dpo/solicitacao/{protocolo}")
    @PreAuthorize("permitAll()")
    @Operation(summary = "Consulta o andamento de uma solicitação de titular pelo número de protocolo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitação localizada",
                    content = @Content(schema = @Schema(implementation = SolicitacaoDpoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Protocolo não encontrado")
    })
    public SolicitacaoDpoResponse consultarProtocolo(
            @Parameter(description = "Número de protocolo gerado na abertura") @PathVariable String protocolo) {
        return dpoUseCase.obterPorProtocolo(protocolo);
    }

    @GetMapping("/consentimentos/{titularTipo}/{titularId}/historico")
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO', 'SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Consulta a trilha imutável de auditoria de consentimento de um titular")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trilha recuperada com sucesso",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = HistoricoConsentimentoResponse.class)))),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public List<HistoricoConsentimentoResponse> listarHistorico(
            @Parameter(description = "Tipo do titular: FIEL ou CLERIGO") @PathVariable TipoTitularConsentimento titularTipo,
            @Parameter(description = "Identificador único do titular") @PathVariable UUID titularId) {
        return consentimentoUseCase.listarHistorico(titularTipo, titularId);
    }

    @GetMapping("/governanca/dpo/solicitacoes")
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO', 'SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Lista todas as solicitações de titulares endereçadas ao DPO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de solicitações recuperada com sucesso",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = SolicitacaoDpoResponse.class)))),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public List<SolicitacaoDpoResponse> listarSolicitacoesDpo() {
        return dpoUseCase.listarTodas();
    }

    @PostMapping("/governanca/dpo/solicitacoes/{id}/responder")
    @PreAuthorize("hasAnyRole('ADMIN', 'PAROCO', 'BISPO_DIOCESANO', 'SECRETARIA_PAROQUIAL')")
    @Operation(summary = "Registra a resposta formal do DPO para a solicitação do titular")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resposta registrada com sucesso",
                    content = @Content(schema = @Schema(implementation = SolicitacaoDpoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Solicitação não encontrada")
    })
    public SolicitacaoDpoResponse responderSolicitacaoDpo(
            @Parameter(description = "Identificador da solicitação") @PathVariable UUID id,
            @RequestBody @Valid ResponderSolicitacaoDpoRequest request) {
        return dpoUseCase.responder(id, request);
    }

    /**
     * IP de origem para a trilha de consentimento. Usa somente o endereço da conexão: o cabeçalho
     * {@code X-Forwarded-For} é forjável por qualquer cliente e só é confiável quando um proxy conhecido o
     * sobrescreve — caso em que {@code server.forward-headers-strategy=native} faz o contêiner devolver o
     * IP real em {@code getRemoteAddr()}.
     */
    private String extrairIp(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
