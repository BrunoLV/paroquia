package br.com.nsfatima.gestao.fiel.infrastructure.config;

import br.com.nsfatima.gestao.fiel.domain.service.ValidacaoDocumentoCivil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra os serviços do domínio puro do contexto {@code fiel} no container Spring.
 */
@Configuration
public class FielDomainConfig {

    @Bean
    public ValidacaoDocumentoCivil validacaoDocumentoCivil() {
        return new ValidacaoDocumentoCivil();
    }
}
