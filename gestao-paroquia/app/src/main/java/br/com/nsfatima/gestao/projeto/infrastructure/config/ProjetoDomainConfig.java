package br.com.nsfatima.gestao.projeto.infrastructure.config;

import br.com.nsfatima.gestao.projeto.domain.policy.AutorizacaoProjetoPolicy;
import br.com.nsfatima.gestao.projeto.domain.policy.PadraoAutorizacaoProjetoPolicy;
import br.com.nsfatima.gestao.projeto.domain.policy.PeriodoProjetoVincularPolicy;
import br.com.nsfatima.gestao.projeto.domain.policy.ProjetoVincularPolicy;
import br.com.nsfatima.gestao.projeto.domain.service.ProjetoAgregacaoDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registra Policies puras do contexto de projetos. */
@Configuration
public class ProjetoDomainConfig {

    @Bean
    public ProjetoAgregacaoDomainService projetoAgregacaoDomainService() {
        return new ProjetoAgregacaoDomainService();
    }

    @Bean
    public AutorizacaoProjetoPolicy autorizacaoProjetoPolicy() {
        return new PadraoAutorizacaoProjetoPolicy();
    }

    @Bean
    public ProjetoVincularPolicy projetoVincularPolicy() {
        return new PeriodoProjetoVincularPolicy();
    }
}
