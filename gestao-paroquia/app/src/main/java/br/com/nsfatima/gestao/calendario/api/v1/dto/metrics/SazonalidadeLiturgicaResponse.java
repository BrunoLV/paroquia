package br.com.nsfatima.gestao.calendario.api.v1.dto.metrics;

import br.com.nsfatima.gestao.calendario.domain.model.TipoCicloLiturgico;

import java.math.BigDecimal;

/**
 * DTO com indicador de Sazonalidade Litúrgica e Preditividade Pastoral.
 */
public record SazonalidadeLiturgicaResponse(
        String cicloNome,
        TipoCicloLiturgico tipoCiclo,
        BigDecimal fatorDemanda,
        Long quantidadeEventosHistoricos,
        Long quantidadeEventosPrevistos,
        BigDecimal indiceSazonalidade,
        String nivelDemanda
) {
}
