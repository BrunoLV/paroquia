package br.com.nsfatima.gestao.patrimonio.infrastructure.config;

import br.com.nsfatima.gestao.patrimonio.domain.policy.AutorizacaoPatrimonioPolicy;
import br.com.nsfatima.gestao.patrimonio.domain.policy.MatrizOuComunidadePropriedadePolicy;
import br.com.nsfatima.gestao.patrimonio.domain.policy.PadraoAutorizacaoPatrimonioPolicy;
import br.com.nsfatima.gestao.patrimonio.domain.policy.PropriedadeUnidadeTerritorialPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra Policies patrimoniais puras fora da camada de domínio.
 */
@Configuration
public class PatrimonioDomainConfig {

    @Bean
    public PropriedadeUnidadeTerritorialPolicy propriedadeUnidadeTerritorialPolicy() {
        return new MatrizOuComunidadePropriedadePolicy();
    }

    @Bean
    public AutorizacaoPatrimonioPolicy autorizacaoPatrimonioPolicy() {
        return new PadraoAutorizacaoPatrimonioPolicy();
    }
}
