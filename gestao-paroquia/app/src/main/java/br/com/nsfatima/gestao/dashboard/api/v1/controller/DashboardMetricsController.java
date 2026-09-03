package br.com.nsfatima.gestao.dashboard.api.v1.controller;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.DashboardMetricsDTO;
import br.com.nsfatima.gestao.dashboard.application.usecase.GetDashboardMetricsUseCase;
import br.com.nsfatima.gestao.dashboard.domain.model.PeriodoDashboard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Expõe o endpoint de métricas consolidadas das seções crítica e de projetos do dashboard.
 * Delega o cálculo ao {@link GetDashboardMetricsUseCase}, aplicando o recorte temporal e o escopo
 * organizacional recebidos no filtro global — este último sempre validado contra o que o ator
 * autenticado pode ver.
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Endpoints para consulta de métricas e dados gerenciais")
public class DashboardMetricsController {

    private final GetDashboardMetricsUseCase getDashboardMetricsUseCase;
    private final EventoActorContextResolver actorContextResolver;

    public DashboardMetricsController(
            GetDashboardMetricsUseCase getDashboardMetricsUseCase,
            EventoActorContextResolver actorContextResolver) {
        this.getDashboardMetricsUseCase = getDashboardMetricsUseCase;
        this.actorContextResolver = actorContextResolver;
    }

    /**
     * Retorna resumo geral, distribuição de eventos e projetos, progresso dos projetos ativos,
     * eventos órfãos e a agenda da semana, filtrados pelo recorte temporal e pela organização
     * informados.
     *
     * @param periodo recorte temporal (HOJE, SEMANA ou MES); padrão SEMANA
     * @param grupoParoquialId organização a filtrar, ou ausente para o escopo do próprio ator
     * @return métricas consolidadas do dashboard
     */
    @GetMapping("/metrics")
    @Operation(summary = "Obtém as métricas do dashboard",
            description = "Resumo geral, distribuição por organização e status, projetos ativos, eventos órfãos e "
                    + "agenda da semana, no recorte temporal e organização selecionados.")
    public DashboardMetricsDTO getMetrics(
            @Parameter(description = "Recorte temporal do filtro global")
            @RequestParam(defaultValue = "SEMANA") PeriodoDashboard periodo,
            @Parameter(description = "Organização a filtrar; ausente usa o escopo do ator")
            @RequestParam(required = false) UUID grupoParoquialId) {
        var context = actorContextResolver.resolveRequired();
        return getDashboardMetricsUseCase.execute(context, periodo, grupoParoquialId);
    }
}
