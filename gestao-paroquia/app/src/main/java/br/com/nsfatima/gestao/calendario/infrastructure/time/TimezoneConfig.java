package br.com.nsfatima.gestao.calendario.infrastructure.time;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TimezoneConfig {

    @Bean
    Clock utcClock() {
        return Clock.systemUTC();
    }

    @Bean
    ZoneId outputZoneId(@Value("${app.timezone.output:America/Sao_Paulo}") String zone) {
        return ZoneId.of(zone);
    }
}
