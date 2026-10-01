package br.com.nsfatima.gestao.sacramento.infrastructure.config;

import br.com.nsfatima.gestao.sacramento.domain.policy.AlcadaSacramentalPolicy;
import br.com.nsfatima.gestao.sacramento.domain.policy.BatismoRequisitosPolicy;
import br.com.nsfatima.gestao.sacramento.domain.policy.ConfirmacaoRequisitosPolicy;
import br.com.nsfatima.gestao.sacramento.domain.policy.MatrimonioRequisitosPolicy;
import br.com.nsfatima.gestao.sacramento.domain.policy.OrdemRequisitosPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registra as Policies puras do contexto de sacramentos fora da camada de domínio. */
@Configuration
public class SacramentoDomainConfig {

    @Bean
    public AlcadaSacramentalPolicy alcadaSacramentalPolicy() {
        return new AlcadaSacramentalPolicy();
    }

    @Bean
    public BatismoRequisitosPolicy batismoRequisitosPolicy() {
        return new BatismoRequisitosPolicy();
    }

    @Bean
    public ConfirmacaoRequisitosPolicy confirmacaoRequisitosPolicy() {
        return new ConfirmacaoRequisitosPolicy();
    }

    @Bean
    public MatrimonioRequisitosPolicy matrimonioRequisitosPolicy() {
        return new MatrimonioRequisitosPolicy();
    }

    @Bean
    public OrdemRequisitosPolicy ordemRequisitosPolicy() {
        return new OrdemRequisitosPolicy();
    }
}
