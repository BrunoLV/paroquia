package br.com.nsfatima.gestao.calendario.api.v1.dto.metrics;

import br.com.nsfatima.gestao.calendario.domain.model.TipoCicloLiturgico;

/**
 * DTO com indicador de Sazonalidade Litúrgica e Preditividade Pastoral.
 */
public record SazonalidadeLiturgicaResponse(
        String cicloNome,
        TipoCicloLiturgico tipoCiclo,
        Double fatorDemanda,
        Long quantidadeEventosHistoricos,
        Long quantidadeEventosPrevistos,
        Double indiceSazonalidade,
        String nivelDemanda
) {
}
