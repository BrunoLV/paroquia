package br.com.nsfatima.gestao.dashboard.infrastructure.config;

import br.com.nsfatima.gestao.dashboard.domain.service.JanelaTemporalResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.ZoneId;

/** Registra a regra temporal pura fora da camada de domínio. */
@Configuration
public class DashboardDomainConfig {

    @Bean
    public JanelaTemporalResolver janelaTemporalResolver(ZoneId outputZoneId) {
        return new JanelaTemporalResolver(outputZoneId);
    }
}
