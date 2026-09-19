package br.com.nsfatima.gestao.calendario.api.v1.dto.metricas;

import java.util.List;

/**
 * Payload consolidado das 6 métricas estratégicas para exibição em dashboard/relatórios.
 */
public record MetricasEstrategicasPainelResponse(
        List<SazonalidadeLiturgicaResponse> sazonalidadeLiturgica,
        TaxaConclusaoProjetosResponse taxaProjetos,
        SlaDespachoPastoralResponse slaDespacho,
        CoeficienteImpactoSocialResponse impactoSocial,
        ConcorrenciaInterpastoralResponse concorrenciaInterpastoral,
        OciosidadePatrimonialResponse ociosidadePatrimonial
) {
}
