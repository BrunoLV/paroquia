package br.com.nsfatima.gestao.calendario.infrastructure.time;

import br.com.nsfatima.gestao.support.domain.time.RelogioNegocio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Centraliza a definição de fuso horário usada pelo contexto de calendário. Todos os instantes
 * de eventos são persistidos e calculados internamente em UTC (via {@link #utcClock()}) para
 * evitar ambiguidades de horário de verão, e convertidos para o fuso de exibição da paróquia
 * (padrão {@code America/Sao_Paulo}) na borda de apresentação e em toda decisão de calendário
 * civil (hoje, mês, ano paroquial), via {@link RelogioNegocio}, garantindo consistência nos
 * cálculos de agenda independentemente do fuso do servidor.
 */
@Configuration
public class TimezoneConfig {

    /**
     * Fornece o relógio de referência da aplicação, sempre em UTC, usado para gerar e comparar
     * instantes de eventos de forma consistente.
     *
     * @return relógio do sistema em UTC
     */
    @Bean
    Clock utcClock() {
        return Clock.systemUTC();
    }

    /**
     * Fornece o fuso horário de saída usado para exibir datas/horas ao usuário final, configurável
     * via propriedade e com padrão {@code America/Sao_Paulo}.
     *
     * @param zone identificador do fuso horário, configurável via {@code app.timezone.output}
     * @return o {@link ZoneId} correspondente ao fuso de exibição configurado
     */
    @Bean
    ZoneId outputZoneId(@Value("${app.timezone.output:America/Sao_Paulo}") String zone) {
        return ZoneId.of(zone);
    }

    /**
     * Fornece o relógio de negócio único da aplicação, que combina o relógio de referência e o fuso
     * da paróquia. Todo cálculo de "hoje", mês ou ano paroquial deve usá-lo em vez de
     * {@code LocalDate.now()} ou de fusos fixos.
     *
     * @param utcClock relógio de referência
     * @param outputZoneId fuso civil da paróquia
     * @return relógio de negócio
     */
    @Bean
    RelogioNegocio relogioNegocio(Clock utcClock, ZoneId outputZoneId) {
        return new RelogioNegocio(utcClock, outputZoneId);
    }
}
