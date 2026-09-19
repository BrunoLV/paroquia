package br.com.nsfatima.gestao.dashboard.api.v1.controller;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.AniversarianteDTO;
import br.com.nsfatima.gestao.dashboard.api.v1.dto.DashboardComunidadeDTO;
import br.com.nsfatima.gestao.dashboard.application.usecase.ObterRetencaoPastoraisUseCase;
import br.com.nsfatima.gestao.dashboard.application.usecase.ListarAniversariantesUseCase;
import br.com.nsfatima.gestao.dashboard.domain.model.PeriodoDashboard;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Expõe a seção de vida comunitária do dashboard: aniversariantes do período e índice de retenção
 * de pastorais. O endpoint de aniversariantes exige autenticação (dado sensível de LGPD) e
 * devolve apenas primeiro nome + dia/mês.
 */
@RestController
@RequestMapping("/api/v1/painel")
@Tag(name = "Painel", description = "Endpoints para consulta de métricas e dados gerenciais")
public class DashboardComunidadeController {

    private final ListarAniversariantesUseCase listarAniversariantesUseCase;
    private final ObterRetencaoPastoraisUseCase getRetencaoPastoraisUseCase;
    private final EventoActorContextResolver actorContextResolver;

    public DashboardComunidadeController(
            ListarAniversariantesUseCase listarAniversariantesUseCase,
            ObterRetencaoPastoraisUseCase getRetencaoPastoraisUseCase,
            EventoActorContextResolver actorContextResolver) {
        this.listarAniversariantesUseCase = listarAniversariantesUseCase;
        this.getRetencaoPastoraisUseCase = getRetencaoPastoraisUseCase;
        this.actorContextResolver = actorContextResolver;
    }

    /**
     * Lista os aniversariantes do período (primeiro nome + dia/mês), apenas de fiéis ativos que
     * autorizaram a divulgação.
     *
     * @param janela recorte: HOJE, SEMANA (próximos 7 dias) ou MES; padrão SEMANA
     * @return aniversariantes do período
     */
    @GetMapping("/aniversariantes")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lista os aniversariantes do período",
            description = "Retorna primeiro nome e dia/mês dos fiéis ativos que autorizaram a divulgação do "
                    + "aniversário e cujo dia/mês cai na janela informada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Aniversariantes listados com sucesso",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = AniversarianteDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public List<AniversarianteDTO> listarAniversariantes(
            @Parameter(description = "Recorte temporal (HOJE, SEMANA ou MES)")
            @RequestParam(defaultValue = "SEMANA") PeriodoDashboard janela) {
        return listarAniversariantesUseCase.execute(janela);
    }

    /**
     * Retorna as pastorais em situação de atenção quanto à retenção (poucos voluntários ou sem
     * liderança), no escopo do ator ou da organização informada.
     *
     * @param grupoParoquialId organização a filtrar, ou ausente para o escopo do próprio ator
     * @return índice de retenção de pastorais
     */
    @GetMapping("/comunidade")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Índice de retenção de pastorais",
            description = "Pastorais/movimentos/ministérios com número crítico de voluntários ou sem coordenador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Métricas de comunidade retornadas com sucesso",
                    content = @Content(schema = @Schema(implementation = DashboardComunidadeDTO.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public DashboardComunidadeDTO obterComunidade(
            @Parameter(description = "Organização a filtrar; ausente usa o escopo do ator")
            @RequestParam(required = false) UUID grupoParoquialId) {
        var context = actorContextResolver.resolveRequired();
        return getRetencaoPastoraisUseCase.execute(context, grupoParoquialId);
    }
}
