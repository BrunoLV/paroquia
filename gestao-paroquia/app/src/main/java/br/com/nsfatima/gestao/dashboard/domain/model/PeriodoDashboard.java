package br.com.nsfatima.gestao.dashboard.domain.model;

import br.com.nsfatima.gestao.calendario.api.v1.dto.support.EnumRequestNormalizer;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Recorte temporal do filtro global do dashboard. Define a janela sobre a qual as métricas de
 * volume (eventos do período, distribuição por organização, eventos órfãos, logística) são
 * recalculadas no servidor. Métricas de estado corrente (fiéis ativos, projetos por status,
 * novos fiéis no mês) ignoram este recorte por definição.
 */
public enum PeriodoDashboard {

    /** Dia corrente (00:00 até 24:00 UTC de hoje). */
    HOJE,

    /** Próximos sete dias a partir de agora — usado pelo indicador "eventos da semana". */
    SEMANA,

    /** Mês calendário vigente (primeiro ao último dia). */
    MES;

    /**
     * Normaliza um valor bruto recebido via query string (variações de caixa/acento) para a
     * constante correspondente, servindo de ponto de entrada de desserialização do Jackson e da
     * conversão de {@code @RequestParam}.
     *
     * @param rawValue valor textual recebido na requisição
     * @return o {@link PeriodoDashboard} normalizado correspondente
     */
    @JsonCreator
    public static PeriodoDashboard fromJson(String rawValue) {
        return EnumRequestNormalizer.normalize(rawValue, PeriodoDashboard.class);
    }
}
