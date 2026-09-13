package br.com.nsfatima.gestao.calendario.api.v1.dto.metrics;

import java.math.BigDecimal;

/**
 * DTO para o Coeficiente de Impacto Social (Pastoral em Saída).
 */
public record CoeficienteImpactoSocialResponse(
        Long totalEventos,
        Long eventosSociaisCaridade,
        Long reunioesInternas,
        BigDecimal coeficienteImpactoSocialPercentual,
        String diagnosticoPastoral
) {
}
