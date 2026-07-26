package br.com.nsfatima.gestao.dashboard.api.v1.controller;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.DashboardMetricsDTO;
import br.com.nsfatima.gestao.dashboard.application.usecase.GetDashboardMetricsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/metrics")
    @Operation(summary = "Obtém as métricas do dashboard", description = "Retorna o resumo geral de eventos, projetos" +
            " e solicitações no período corrente.")
    public DashboardMetricsDTO getMetrics() {
        var context = actorContextResolver.resolveRequired();
        return getDashboardMetricsUseCase.execute(context);
    }
}
