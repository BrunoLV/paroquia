package br.com.nsfatima.gestao.calendario.api.v1.controller;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.AuditoriaFiltroRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.AuditoriaOperacaoResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.IndicadorRetrabalhoResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.TaxaEventosExtraResponse;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.GetTaxaEventosExtraUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.metrics.GetIndicadorRetrabalhoUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.metrics.ListAuditTrailUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auditoria/eventos")
@Tag(name = "Auditoria e Métricas", description = "Endpoints para consulta de trilhas de auditoria e métricas de desempenho")
public class AuditoriaEventoController {

    private final GetTaxaEventosExtraUseCase getTaxaEventosExtraUseCase;
    private final ListAuditTrailUseCase listAuditTrailUseCase;
    private final GetIndicadorRetrabalhoUseCase getIndicadorRetrabalhoUseCase;

    public AuditoriaEventoController(
            GetTaxaEventosExtraUseCase getTaxaEventosExtraUseCase,
            ListAuditTrailUseCase listAuditTrailUseCase,
            GetIndicadorRetrabalhoUseCase getIndicadorRetrabalhoUseCase) {
        this.getTaxaEventosExtraUseCase = getTaxaEventosExtraUseCase;
        this.listAuditTrailUseCase = listAuditTrailUseCase;
        this.getIndicadorRetrabalhoUseCase = getIndicadorRetrabalhoUseCase;
    }

    /**
     * Enables administrative oversight by providing a metric of unplanned activities, helping to evaluate the effectiveness of the initial calendar planning.
     * 
     * Usage Example:
     * {@code
     * TaxaEventosExtraResponse response = controller.getTaxaExtras(organizacaoId, "anual", null, null);
     * }
     */
    @GetMapping("/extras")
    @Operation(summary = "Obtém taxa de eventos extras", description = "Calcula o percentual de eventos adicionados extra-ordinariamente no período informado.")
    public TaxaEventosExtraResponse getTaxaExtras(
            @Parameter(description = "ID da organização") @RequestParam UUID organizacaoId,
            @Parameter(description = "Período de análise (ex: anual, mensal)") @RequestParam(required = false) String periodo,
            @Parameter(description = "Data de início (ISO 8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant inicio,
            @Parameter(description = "Data de término (ISO 8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fim) {
        return getTaxaEventosExtraUseCase.execute(organizacaoId, periodo, inicio, fim);
    }

    /**
     * Supports security and accountability audits by allowing detailed inspection of system operations, filtered by actor, action, and correlation IDs.
     * 
     * Usage Example:
     * {@code
     * AuditoriaOperacaoResponse response = controller.getTrilha(new AuditoriaFiltroRequest(...));
     * }
     */
    @GetMapping("/trilha")
    @Operation(summary = "Consulta trilha de auditoria", description = "Retorna os registros de auditoria baseados nos filtros informados.")
    public AuditoriaOperacaoResponse getTrilha(AuditoriaFiltroRequest filters) {
        return listAuditTrailUseCase.execute(
                filters.organizacaoId(),
                filters.granularidade(),
                filters.inicio(),
                filters.fim(),
                filters.ator(),
                filters.acao(),
                filters.resultado(),
                filters.correlationId());
    }

    /**
     * Identifies potential inefficiencies in the planning process by quantifying how many events were modified or cancelled after their initial creation.
     * 
     * Usage Example:
     * {@code
     * IndicadorRetrabalhoResponse response = controller.getRetrabalho(organizacaoId, "mensal", null, null);
     * }
     */
    @GetMapping("/retrabalho")
    @Operation(summary = "Calcula indicador de retrabalho", description = "Retorna o percentual de retrabalho baseado em modificações e cancelamentos no período.")
    public IndicadorRetrabalhoResponse getRetrabalho(
            @Parameter(description = "ID da organização") @RequestParam UUID organizacaoId,
            @Parameter(description = "Granularidade temporal") @RequestParam(required = false) String granularidade,
            @Parameter(description = "Data de início (ISO 8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant inicio,
            @Parameter(description = "Data de término (ISO 8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant fim) {
        return getIndicadorRetrabalhoUseCase.execute(organizacaoId, granularidade, inicio, fim);
    }
}
