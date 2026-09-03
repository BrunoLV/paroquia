package br.com.nsfatima.gestao.dashboard.api.v1.controller;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.DashboardLogisticaDTO;
import br.com.nsfatima.gestao.dashboard.application.usecase.GetDashboardLogisticaUseCase;
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
 * Expõe a seção de logística do dashboard (choque de horário por local, ocupação de ambientes,
 * eventos por responsável e pendências de homologação), delegando o cálculo a
 * {@link GetDashboardLogisticaUseCase} sob o mesmo recorte temporal e escopo organizacional
 * validado do endpoint de métricas.
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard", description = "Endpoints para consulta de métricas e dados gerenciais")
public class DashboardLogisticaController {

    private final GetDashboardLogisticaUseCase getDashboardLogisticaUseCase;
    private final EventoActorContextResolver actorContextResolver;

    public DashboardLogisticaController(
            GetDashboardLogisticaUseCase getDashboardLogisticaUseCase,
            EventoActorContextResolver actorContextResolver) {
        this.getDashboardLogisticaUseCase = getDashboardLogisticaUseCase;
        this.actorContextResolver = actorContextResolver;
    }

    /**
     * Retorna os indicadores de logística, espaços e agenda para o recorte temporal e a
     * organização informados.
     *
     * @param periodo recorte temporal (HOJE, SEMANA ou MES); padrão SEMANA
     * @param grupoParoquialId organização a filtrar, ou ausente para o escopo do próprio ator
     * @return snapshot de logística do dashboard
     */
    @GetMapping("/logistica")
    @Operation(summary = "Obtém as métricas de logística do dashboard",
            description = "Choque de horário por local, ocupação estimada dos ambientes, eventos por responsável e "
                    + "pendências de homologação por tipo.")
    public DashboardLogisticaDTO getLogistica(
            @Parameter(description = "Recorte temporal do filtro global")
            @RequestParam(defaultValue = "SEMANA") PeriodoDashboard periodo,
            @Parameter(description = "Organização a filtrar; ausente usa o escopo do ator")
            @RequestParam(required = false) UUID grupoParoquialId) {
        var context = actorContextResolver.resolveRequired();
        return getDashboardLogisticaUseCase.execute(context, periodo, grupoParoquialId);
    }
}
