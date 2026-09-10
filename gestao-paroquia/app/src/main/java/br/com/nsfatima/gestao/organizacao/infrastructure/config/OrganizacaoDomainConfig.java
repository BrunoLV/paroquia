package br.com.nsfatima.gestao.organizacao.infrastructure.config;

import br.com.nsfatima.gestao.organizacao.domain.policy.AutorizacaoEscopoUsuarioPolicy;
import br.com.nsfatima.gestao.organizacao.domain.policy.PadraoAutorizacaoEscopoUsuarioPolicy;
import br.com.nsfatima.gestao.organizacao.domain.repository.GrupoParoquialRepository;
import br.com.nsfatima.gestao.organizacao.domain.repository.PastoralMovimentoOficialRepository;
import br.com.nsfatima.gestao.organizacao.application.service.ValidadorHierarquiaOrganizacional;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra os serviços e políticas do domínio puro de organização no container Spring.
 */
@Configuration
public class OrganizacaoDomainConfig {

    @Bean
    public ValidadorHierarquiaOrganizacional validadorHierarquiaOrganizacional(
            GrupoParoquialRepository grupoParoquialRepository,
            PastoralMovimentoOficialRepository pastoralOficialRepository,
            UnidadeTerritorialJpaRepository unidadeTerritorialRepository) {
        return new ValidadorHierarquiaOrganizacional(
                grupoParoquialRepository, pastoralOficialRepository, unidadeTerritorialRepository);
    }

    @Bean
    public AutorizacaoEscopoUsuarioPolicy autorizacaoEscopoUsuarioPolicy() {
        return new PadraoAutorizacaoEscopoUsuarioPolicy();
    }
}
