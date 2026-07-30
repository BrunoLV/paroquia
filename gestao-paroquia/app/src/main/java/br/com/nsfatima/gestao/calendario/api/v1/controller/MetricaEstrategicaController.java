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
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/metricas/estrategicas")
@Tag(name = "Métricas Estratégicas e Preditivas", description = "Endpoints para consulta de inteligência pastoral e governança paroquial (Clero, CPP e CAE)")
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

    @GetMapping("/painel")
    @Operation(summary = "Obtém painel consolidado com as 6 métricas estratégicas")
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

    @GetMapping("/sazonalidade-liturgica")
    @Operation(summary = "Índice de Sazonalidade Litúrgica e Preditividade Pastoral")
    public List<SazonalidadeLiturgicaResponse> getSazonalidadeLiturgica() {
        return calcularSazonalidadeLiturgicaUseCase.execute();
    }

    @GetMapping("/taxa-projetos")
    @Operation(summary = "Taxa de Conclusão e Abandono de Projetos (CPP/CAE)")
    public TaxaConclusaoProjetosResponse getTaxaProjetos() {
        return calcularTaxaProjetosUseCase.execute();
    }

    @GetMapping("/sla-despacho")
    @Operation(summary = "SLA de Despacho Pastoral (Tempo Médio de Homologação)")
    public SlaDespachoPastoralResponse getSlaDespacho() {
        return calcularSlaDespachoPastoralUseCase.execute();
    }

    @GetMapping("/impacto-social")
    @Operation(summary = "Coeficiente de Impacto Social (Pastoral em Saída)")
    public CoeficienteImpactoSocialResponse getImpactoSocial() {
        return calcularImpactoSocialUseCase.execute();
    }

    @GetMapping("/concorrencia-interpastoral")
    @Operation(summary = "Alerta de Concorrência Interpastoral (Overlap Conflict)")
    public ConcorrenciaInterpastoralResponse getConcorrenciaInterpastoral() {
        return verificarConcorrenciaInterpastoralUseCase.execute();
    }

    @GetMapping("/ociosidade-patrimonial")
    @Operation(summary = "Índice de Ociosidade Patrimonial de Espaços e Salas")
    public OciosidadePatrimonialResponse getOciosidadePatrimonial() {
        return calcularOciosidadePatrimonialUseCase.execute();
    }
}
