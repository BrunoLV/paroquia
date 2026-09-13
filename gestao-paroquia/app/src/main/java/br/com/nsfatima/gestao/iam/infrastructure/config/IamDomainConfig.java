package br.com.nsfatima.gestao.iam.infrastructure.config;

import br.com.nsfatima.gestao.iam.domain.policy.PadraoPapelGlobalMappingPolicy;
import br.com.nsfatima.gestao.iam.domain.policy.PadraoUsuarioGerenciamentoAutorizacaoPolicy;
import br.com.nsfatima.gestao.iam.domain.policy.PapelGlobalMappingPolicy;
import br.com.nsfatima.gestao.iam.domain.policy.UsuarioGerenciamentoAutorizacaoPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra as políticas do domínio puro de IAM no container Spring.
 */
@Configuration
public class IamDomainConfig {

    @Bean
    public PapelGlobalMappingPolicy papelGlobalMappingPolicy() {
        return new PadraoPapelGlobalMappingPolicy();
    }

    @Bean
    public UsuarioGerenciamentoAutorizacaoPolicy usuarioGerenciamentoAutorizacaoPolicy() {
        return new PadraoUsuarioGerenciamentoAutorizacaoPolicy();
    }
}
