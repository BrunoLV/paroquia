package br.com.nsfatima.gestao.organizacao.api.v1.controller;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.MandatoCoordenacaoRequest;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.MandatoCoordenacaoResponse;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.VetarMandatoRequest;
import br.com.nsfatima.gestao.organizacao.application.service.MandatoCoordenacaoService;
import br.com.nsfatima.gestao.organizacao.domain.model.MandatoCoordenacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * API do mandato de coordenação das organizações. A coordenação é um encargo temporário: o grupo ou o
 * conselho propõe, e somente o Pároco homologa, veta ou encerra (Cân. 519 e 317).
 */
@RestController
@RequestMapping("/api/v1/organizacoes/{organizacaoId}/mandatos")
@Tag(name = "Mandatos de Coordenação", description = "Proposta, homologação e encerramento de mandatos")
public class MandatoCoordenacaoController {

    private final MandatoCoordenacaoService service;
    private final FielJpaRepository fielRepository;
    private final Clock clock;

    public MandatoCoordenacaoController(MandatoCoordenacaoService service, FielJpaRepository fielRepository,
                                        Clock clock) {
        this.service = service;
        this.fielRepository = fielRepository;
        this.clock = clock;
    }

    /**
     * Lista os mandatos de uma organização, do mais recente ao mais antigo.
     *
     * @param organizacaoId identificador da organização
     * @return mandatos da organização
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista os mandatos de coordenação de uma organização")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mandatos listados"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    public List<MandatoCoordenacaoResponse> listar(
            @Parameter(description = "Identificador da organização", required = true)
            @PathVariable UUID organizacaoId) {
        return service.listar(organizacaoId).stream().map(this::toResponse).toList();
    }

    /**
     * Registra a proposta de um mandato, pendente de homologação.
     *
     * @param organizacaoId identificador da organização
     * @param request dados da proposta
     * @return o mandato proposto
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('PAROCO', 'BISPO_DIOCESANO', 'SECRETARIO_CONSELHO', 'MEMBRO_CONSELHO_PASTORAL')")
    @Operation(summary = "Propõe um mandato de coordenação")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mandato proposto"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "409", description = "Proposta viola as regras do mandato")
    })
    public MandatoCoordenacaoResponse propor(
            @Parameter(description = "Identificador da organização", required = true)
            @PathVariable UUID organizacaoId,
            @RequestBody @Valid MandatoCoordenacaoRequest request) {
        return toResponse(service.propor(organizacaoId, request.fielId(), request.funcao(),
                request.inicio(), request.fim()));
    }

    /**
     * Homologa a proposta, tornando legítimo o exercício da função.
     *
     * @param organizacaoId identificador da organização
     * @param mandatoId identificador do mandato proposto
     * @return o mandato homologado
     */
    @PostMapping("/{mandatoId}/homologacao")
    @PreAuthorize("hasAnyRole('PAROCO')")
    @Operation(summary = "Homologa um mandato de coordenação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mandato homologado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "409", description = "Mandato não pode ser homologado")
    })
    public MandatoCoordenacaoResponse homologar(
            @PathVariable UUID organizacaoId,
            @Parameter(description = "Identificador do mandato", required = true)
            @PathVariable UUID mandatoId) {
        return toResponse(service.homologar(mandatoId));
    }

    /**
     * Veta a proposta, informando o motivo.
     *
     * @param organizacaoId identificador da organização
     * @param mandatoId identificador do mandato proposto
     * @param request motivo do veto
     * @return o mandato vetado
     */
    @PostMapping("/{mandatoId}/veto")
    @PreAuthorize("hasAnyRole('PAROCO')")
    @Operation(summary = "Veta um mandato de coordenação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mandato vetado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "409", description = "Mandato não pode ser vetado")
    })
    public MandatoCoordenacaoResponse vetar(
            @PathVariable UUID organizacaoId,
            @Parameter(description = "Identificador do mandato", required = true)
            @PathVariable UUID mandatoId,
            @RequestBody @Valid VetarMandatoRequest request) {
        return toResponse(service.vetar(mandatoId, request.motivo()));
    }

    /**
     * Encerra um mandato homologado.
     *
     * @param organizacaoId identificador da organização
     * @param mandatoId identificador do mandato homologado
     * @return o mandato encerrado
     */
    @PostMapping("/{mandatoId}/encerramento")
    @PreAuthorize("hasAnyRole('PAROCO')")
    @Operation(summary = "Encerra um mandato de coordenação")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mandato encerrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "409", description = "Mandato não pode ser encerrado")
    })
    public MandatoCoordenacaoResponse encerrar(
            @PathVariable UUID organizacaoId,
            @Parameter(description = "Identificador do mandato", required = true)
            @PathVariable UUID mandatoId) {
        return toResponse(service.encerrar(mandatoId));
    }

    private MandatoCoordenacaoResponse toResponse(MandatoCoordenacao m) {
        String nome = fielRepository.findById(m.getFielId()).map(FielEntity::getNome).orElse(null);
        return new MandatoCoordenacaoResponse(m.getId(), m.getOrganizacaoId(), m.getFielId(), nome, m.getFuncao(),
                m.getInicio(), m.getFim(), m.getStatus(), m.vigenteEm(LocalDate.now(clock)), m.getMotivoVeto(),
                m.getDecididoEmUtc());
    }
}
