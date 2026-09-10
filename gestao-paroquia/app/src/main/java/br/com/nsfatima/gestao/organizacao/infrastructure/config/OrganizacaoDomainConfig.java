package br.com.nsfatima.gestao.organizacao.infrastructure.config;

import br.com.nsfatima.gestao.organizacao.domain.repository.GrupoParoquialRepository;
import br.com.nsfatima.gestao.organizacao.domain.repository.PastoralMovimentoOficialRepository;
import br.com.nsfatima.gestao.organizacao.domain.service.HierarquiaOrganizacionalPolicy;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra os serviços/políticas do domínio puro de organização no container Spring.
 */
@Configuration
public class OrganizacaoDomainConfig {

    @Bean
    public HierarquiaOrganizacionalPolicy hierarquiaOrganizacionalPolicy(
            GrupoParoquialRepository grupoParoquialRepository,
            PastoralMovimentoOficialRepository pastoralOficialRepository,
            UnidadeTerritorialJpaRepository unidadeTerritorialRepository) {
        return new HierarquiaOrganizacionalPolicy(
                grupoParoquialRepository, pastoralOficialRepository, unidadeTerritorialRepository);
    }
}
