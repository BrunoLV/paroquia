package br.com.nsfatima.gestao.calendario.api.v1.dto.metrics;

/**
 * DTO para o Coeficiente de Impacto Social (Pastoral em Saída).
 */
public record CoeficienteImpactoSocialResponse(
        Long totalEventos,
        Long eventosSociaisCaridade,
        Long reunioesInternas,
        Double coeficienteImpactoSocialPercentual,
        String diagnosticoPastoral
) {
}
