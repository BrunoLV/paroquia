package br.com.nsfatima.gestao.governanca.infrastructure.config;

import br.com.nsfatima.gestao.governanca.domain.repository.AnoParoquialRepository;
import br.com.nsfatima.gestao.governanca.domain.policy.AutorizacaoAnoParoquialPolicy;
import br.com.nsfatima.gestao.governanca.domain.policy.ConselhoOuParocoAutorizacaoAnoParoquialPolicy;
import br.com.nsfatima.gestao.governanca.domain.service.SucessaoAnoParoquialService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra os serviços do domínio puro de governança no container Spring, preservando o
 * isolamento de frameworks da camada de domínio.
 */
@Configuration
public class GovernancaDomainConfig {

    /** Expõe a regra canônica pura sem acoplar o domínio ao container Spring. */
    @Bean
    public AutorizacaoAnoParoquialPolicy autorizacaoAnoParoquialPolicy() {
        return new ConselhoOuParocoAutorizacaoAnoParoquialPolicy();
    }

    @Bean
    public SucessaoAnoParoquialService sucessaoAnoParoquialService(AnoParoquialRepository repository) {
        return new SucessaoAnoParoquialService(repository);
    }
}
