package br.com.nsfatima.gestao.dashboard.infrastructure.config;

import br.com.nsfatima.gestao.dashboard.domain.service.JanelaTemporalResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registra a regra temporal pura fora da camada de domínio. */
@Configuration
public class DashboardDomainConfig {

    @Bean
    public JanelaTemporalResolver janelaTemporalResolver() {
        return new JanelaTemporalResolver();
    }
}
