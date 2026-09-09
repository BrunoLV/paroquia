package br.com.nsfatima.gestao.calendario.api.v1.controller;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.CoeficienteImpactoSocialResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.ConcorrenciaInterpastoralResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.MetricasEstrategicasPainelResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.OciosidadePatrimonialResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.SazonalidadeLiturgicaResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.SlaDespachoPastoralResponse;
import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.TaxaConclusaoProjetosResponse;
import br.com.nsfatima.gestao.calendario.application.usecase.metrics.CalcularImpactoSocialUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.metrics.CalcularOciosidadePatrimonialUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.metrics.CalcularSazonalidadeLiturgicaUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.metrics.CalcularSlaDespachoPastoralUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.metrics.CalcularTaxaProjetosUseCase;
import br.com.nsfatima.gestao.calendario.application.usecase.metrics.VerificarConcorrenciaInterpastoralUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Expõe as métricas estratégicas e preditivas calculadas sobre o calendário de eventos e os
 * projetos pastorais, consumidas pelos painéis de governança da Matriz, do CPP (Conselho Pastoral
 * Paroquial) e do CAE (Conselho de Assuntos Econômicos). Cada endpoint delega o cálculo a um
 * use case dedicado; o controller apenas orquestra e monta a resposta.
 */
@RestController
@RequestMapping("/api/v1/metricas/estrategicas")
@Tag(
        name = "Métricas Estratégicas e Preditivas",
        description = "Endpoints para consulta de inteligência pastoral e governança paroquial (Clero, CPP e CAE)")
@PreAuthorize("isAuthenticated()")
public class MetricaEstrategicaController {

    private final CalcularSazonalidadeLiturgicaUseCase calcularSazonalidadeLiturgicaUseCase;
    private final CalcularTaxaProjetosUseCase calcularTaxaProjetosUseCase;
    private final CalcularSlaDespachoPastoralUseCase calcularSlaDespachoPastoralUseCase;
    private final CalcularImpactoSocialUseCase calcularImpactoSocialUseCase;
    private final VerificarConcorrenciaInterpastoralUseCase verificarConcorrenciaInterpastoralUseCase;
    private final CalcularOciosidadePatrimonialUseCase calcularOciosidadePatrimonialUseCase;

    public MetricaEstrategicaController(
            CalcularSazonalidadeLiturgicaUseCase calcularSazonalidadeLiturgicaUseCase,
            CalcularTaxaProjetosUseCase calcularTaxaProjetosUseCase,
            CalcularSlaDespachoPastoralUseCase calcularSlaDespachoPastoralUseCase,
            CalcularImpactoSocialUseCase calcularImpactoSocialUseCase,
            VerificarConcorrenciaInterpastoralUseCase verificarConcorrenciaInterpastoralUseCase,
            CalcularOciosidadePatrimonialUseCase calcularOciosidadePatrimonialUseCase) {
        this.calcularSazonalidadeLiturgicaUseCase = calcularSazonalidadeLiturgicaUseCase;
        this.calcularTaxaProjetosUseCase = calcularTaxaProjetosUseCase;
        this.calcularSlaDespachoPastoralUseCase = calcularSlaDespachoPastoralUseCase;
        this.calcularImpactoSocialUseCase = calcularImpactoSocialUseCase;
        this.verificarConcorrenciaInterpastoralUseCase = verificarConcorrenciaInterpastoralUseCase;
        this.calcularOciosidadePatrimonialUseCase = calcularOciosidadePatrimonialUseCase;
    }

    /**
     * Consolida em uma única resposta as seis métricas estratégicas do painel de governança,
     * evitando múltiplas chamadas do frontend para montar o dashboard executivo.
     *
     * @return o painel consolidado com todas as métricas estratégicas
     */
    @GetMapping("/painel")
    @Operation(summary = "Obtém painel consolidado com as 6 métricas estratégicas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Painel consolidado retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = MetricasEstrategicasPainelResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public MetricasEstrategicasPainelResponse getPainelConsolidado() {
        return new MetricasEstrategicasPainelResponse(
                calcularSazonalidadeLiturgicaUseCase.execute(),
                calcularTaxaProjetosUseCase.execute(),
                calcularSlaDespachoPastoralUseCase.execute(),
                calcularImpactoSocialUseCase.execute(),
                verificarConcorrenciaInterpastoralUseCase.execute(),
                calcularOciosidadePatrimonialUseCase.execute()
        );
    }

    /**
     * Calcula o índice de sazonalidade litúrgica, indicando padrões de concentração de eventos ao
     * longo do ano eclesiástico para apoiar o planejamento preditivo de recursos pastorais.
     *
     * @return a lista de indicadores de sazonalidade por período litúrgico
     */
    @GetMapping("/sazonalidade-liturgica")
    @Operation(summary = "Índice de Sazonalidade Litúrgica e Preditividade Pastoral")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Índice de sazonalidade retornado com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = SazonalidadeLiturgicaResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public List<SazonalidadeLiturgicaResponse> getSazonalidadeLiturgica() {
        return calcularSazonalidadeLiturgicaUseCase.execute();
    }

    /**
     * Calcula a taxa de conclusão e de abandono dos projetos pastorais, usada pelo CPP e pelo CAE
     * para avaliar a efetividade da execução de projetos ao longo do tempo.
     *
     * @return a taxa de conclusão/abandono de projetos
     */
    @GetMapping("/taxa-projetos")
    @Operation(summary = "Taxa de Conclusão e Abandono de Projetos (CPP/CAE)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Taxa de projetos retornada com sucesso",
                    content = @Content(schema = @Schema(implementation = TaxaConclusaoProjetosResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public TaxaConclusaoProjetosResponse getTaxaProjetos() {
        return calcularTaxaProjetosUseCase.execute();
    }

    /**
     * Calcula o SLA (tempo médio de homologação) do despacho pastoral, medindo quanto tempo leva
     * para uma solicitação de aprovação ser decidida — indicador de governança do fluxo de aprovação.
     *
     * @return o SLA médio de despacho pastoral
     */
    @GetMapping("/sla-despacho")
    @Operation(summary = "SLA de Despacho Pastoral (Tempo Médio de Homologação)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SLA de despacho pastoral retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = SlaDespachoPastoralResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public SlaDespachoPastoralResponse getSlaDespacho() {
        return calcularSlaDespachoPastoralUseCase.execute();
    }

    /**
     * Calcula o coeficiente de impacto social, refletindo o volume e o alcance de atividades da
     * pastoral em saída (ações voltadas à comunidade externa) no período analisado.
     *
     * @return o coeficiente de impacto social calculado
     */
    @GetMapping("/impacto-social")
    @Operation(summary = "Coeficiente de Impacto Social (Pastoral em Saída)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Coeficiente de impacto social retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = CoeficienteImpactoSocialResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public CoeficienteImpactoSocialResponse getImpactoSocial() {
        return calcularImpactoSocialUseCase.execute();
    }

    /**
     * Verifica a ocorrência de concorrência interpastoral — sobreposição de eventos de diferentes
     * pastorais disputando o mesmo espaço/horário — sinalizando conflitos de agenda para mediação
     * pela governança.
     *
     * @return o alerta de concorrência interpastoral, com os conflitos identificados
     */
    @GetMapping("/concorrencia-interpastoral")
    @Operation(summary = "Alerta de Concorrência Interpastoral (Overlap Conflict)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Concorrência interpastoral avaliada com sucesso",
                    content = @Content(schema = @Schema(implementation = ConcorrenciaInterpastoralResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public ConcorrenciaInterpastoralResponse getConcorrenciaInterpastoral() {
        return verificarConcorrenciaInterpastoralUseCase.execute();
    }

    /**
     * Calcula o índice de ociosidade patrimonial, indicando o grau de subutilização de espaços e
     * salas da paróquia com base na ocupação registrada no calendário de eventos.
     *
     * @return o índice de ociosidade patrimonial de espaços e salas
     */
    @GetMapping("/ociosidade-patrimonial")
    @Operation(summary = "Índice de Ociosidade Patrimonial de Espaços e Salas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Índice de ociosidade patrimonial retornado com sucesso",
                    content = @Content(schema = @Schema(implementation = OciosidadePatrimonialResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public OciosidadePatrimonialResponse getOciosidadePatrimonial() {
        return calcularOciosidadePatrimonialUseCase.execute();
    }
}
