package br.com.nsfatima.gestao.dashboard.domain.service;

import br.com.nsfatima.gestao.dashboard.domain.model.JanelaTemporal;
import br.com.nsfatima.gestao.dashboard.domain.model.PeriodoDashboard;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

/**
 * Traduz o recorte temporal escolhido no filtro global ({@link PeriodoDashboard}) na
 * {@link JanelaTemporal} correspondente, sempre ancorada em UTC. Concentrado em um serviço puro
 * para que todos os cálculos de métrica compartilhem exatamente a mesma definição de janela e
 * para permitir testar as bordas (virada de dia/mês) isoladamente.
 */
public class JanelaTemporalResolver {

    /**
     * Resolve a janela para o recorte informado a partir do instante de referência.
     *
     * @param periodo recorte escolhido; {@code null} é tratado como {@link PeriodoDashboard#SEMANA}
     * @param referencia instante "agora" usado como âncora dos cálculos de borda
     * @return janela {@code [inicio, fim)} correspondente ao recorte
     */
    public JanelaTemporal resolver(PeriodoDashboard periodo, Instant referencia) {
        ZonedDateTime agora = referencia.atZone(ZoneOffset.UTC);
        return switch (periodo == null ? PeriodoDashboard.SEMANA : periodo) {
            case HOJE -> hoje(agora);
            case SEMANA -> proximosSeteDias(referencia, agora);
            case MES -> mesVigente(agora);
        };
    }

    private JanelaTemporal hoje(ZonedDateTime agora) {
        ZonedDateTime inicio = agora.truncatedTo(ChronoUnit.DAYS);
        return new JanelaTemporal(inicio.toInstant(), inicio.plusDays(1).toInstant());
    }

    private JanelaTemporal proximosSeteDias(Instant referencia, ZonedDateTime agora) {
        return new JanelaTemporal(referencia, agora.plusDays(7).toInstant());
    }

    private JanelaTemporal mesVigente(ZonedDateTime agora) {
        ZonedDateTime inicio = agora.with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime fim = inicio.plusMonths(1);
        return new JanelaTemporal(inicio.toInstant(), fim.toInstant());
    }
}
