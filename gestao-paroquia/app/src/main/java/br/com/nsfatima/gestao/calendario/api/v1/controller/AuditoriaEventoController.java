package br.com.nsfatima.gestao.calendario.api.v1.controller;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.AuditoriaFiltroRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.AuditoriaOperacaoResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.IndicadorRetrabalhoResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.TaxaEventosExtraResponse;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.ObterTaxaEventosExtraUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.metrics.ObterIndicadorRetrabalhoUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.metrics.ListAuditTrailUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

/**
 * Endpoints REST para consulta de trilhas de auditoria e métricas de execução de eventos.
 */
@RestController
@RequestMapping("/api/v1/auditoria/eventos")
@Tag(name = "Auditoria e Métricas", description = "Endpoints para consulta de trilhas de auditoria e métricas de " +
        "desempenho")
@PreAuthorize("isAuthenticated()")
public class AuditoriaEventoController {

    private final ObterTaxaEventosExtraUseCase getTaxaEventosExtraUseCase;
    private final ListAuditTrailUseCase listAuditTrailUseCase;
    private final ObterIndicadorRetrabalhoUseCase getIndicadorRetrabalhoUseCase;

    public AuditoriaEventoController(
            ObterTaxaEventosExtraUseCase getTaxaEventosExtraUseCase,
            ListAuditTrailUseCase listAuditTrailUseCase,
            ObterIndicadorRetrabalhoUseCase getIndicadorRetrabalhoUseCase) {
        this.getTaxaEventosExtraUseCase = getTaxaEventosExtraUseCase;
        this.listAuditTrailUseCase = listAuditTrailUseCase;
        this.getIndicadorRetrabalhoUseCase = getIndicadorRetrabalhoUseCase;
    }

    /**
     * Permite supervisão administrativa fornecendo uma métrica de atividades não planejadas, ajudando a avaliar
     * a eficácia do planejamento inicial do calendário.
     * <p>
     * Exemplo de uso:
     * {@code
     * TaxaEventosExtraResponse response = controller.getTaxaExtras(grupoParoquialId, "anual", null, null);
     * }
     */
    @GetMapping("/extras")
    @Operation(summary = "Obtém taxa de eventos extras", description = "Calcula o percentual de eventos adicionados " +
            "extra-ordinariamente no período informado.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Taxa de eventos extras calculada com sucesso",
                    content = @Content(schema = @Schema(implementation = TaxaEventosExtraResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public TaxaEventosExtraResponse getTaxaExtras(
            @Parameter(description = "ID da organização") @RequestParam UUID grupoParoquialId,
            @Parameter(description = "Período de análise (ex: anual, mensal)") @RequestParam(required = false)
            String periodo,
            @Parameter(description = "Data de início (ISO 8601)") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant inicio,
            @Parameter(description = "Data de término (ISO 8601)") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fim) {
        return getTaxaEventosExtraUseCase.execute(grupoParoquialId, periodo, inicio, fim);
    }

    /**
     * Dá suporte a auditorias de segurança e responsabilização, permitindo inspeção detalhada das operações do
     * sistema, filtradas por ator, ação e IDs de correlação.
     * <p>
     * Exemplo de uso:
     * {@code
     * AuditoriaOperacaoResponse response = controller.getTrilha(new AuditoriaFiltroRequest(...));
     * }
     */
    @GetMapping("/trilha")
    @Operation(summary = "Consulta trilha de auditoria", description = "Retorna os registros de auditoria baseados " +
            "nos filtros informados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trilha de auditoria recuperada com sucesso",
                    content = @Content(schema = @Schema(implementation = AuditoriaOperacaoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Filtros inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public AuditoriaOperacaoResponse getTrilha(AuditoriaFiltroRequest filters) {
        return listAuditTrailUseCase.execute(
                filters.grupoParoquialId(),
                filters.granularidade(),
                filters.inicio(),
                filters.fim(),
                filters.ator(),
                filters.acao(),
                filters.resultado(),
                filters.correlationId());
    }

    /**
     * Identifica potenciais ineficiências no processo de planejamento, quantificando quantos eventos foram
     * modificados ou cancelados após a criação inicial.
     * <p>
     * Exemplo de uso:
     * {@code
     * IndicadorRetrabalhoResponse response = controller.getRetrabalho(grupoParoquialId, "mensal", null, null);
     * }
     */
    @GetMapping("/retrabalho")
    @Operation(summary = "Calcula indicador de retrabalho", description = "Retorna o percentual de retrabalho baseado" +
            " em modificações e cancelamentos no período.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Indicador de retrabalho calculado com sucesso",
                    content = @Content(schema = @Schema(implementation = IndicadorRetrabalhoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Parâmetros inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public IndicadorRetrabalhoResponse getRetrabalho(
            @Parameter(description = "ID da organização") @RequestParam UUID grupoParoquialId,
            @Parameter(description = "Granularidade temporal") @RequestParam(required = false) String granularidade,
            @Parameter(description = "Data de início (ISO 8601)") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant inicio,
            @Parameter(description = "Data de término (ISO 8601)") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fim) {
        return getIndicadorRetrabalhoUseCase.execute(grupoParoquialId, granularidade, inicio, fim);
    }
}
