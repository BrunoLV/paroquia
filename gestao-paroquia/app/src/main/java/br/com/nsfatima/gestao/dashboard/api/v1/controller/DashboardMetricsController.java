package br.com.nsfatima.gestao.dashboard.api.v1.controller;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.DashboardMetricsDTO;
import br.com.nsfatima.gestao.dashboard.application.usecase.GetDashboardMetricsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expõe o endpoint de métricas consolidadas do dashboard gerencial. Delega o cálculo ao
 * {@link GetDashboardMetricsUseCase}, aplicando o escopo de organização do ator autenticado
 * (visão restrita à organização do usuário ou visão global, conforme seu papel).
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
     * Retorna o resumo geral de eventos do mês, projetos ativos e solicitações pendentes,
     * já filtrado pelo escopo organizacional do ator autenticado.
     *
     * @return métricas consolidadas do dashboard para o ator corrente
     */
    @GetMapping("/metrics")
    @Operation(summary = "Obtém as métricas do dashboard", description = "Retorna o resumo geral de eventos, projetos" +
            " e solicitações no período corrente.")
    public DashboardMetricsDTO getMetrics() {
        var context = actorContextResolver.resolveRequired();
        return getDashboardMetricsUseCase.execute(context);
    }
}
