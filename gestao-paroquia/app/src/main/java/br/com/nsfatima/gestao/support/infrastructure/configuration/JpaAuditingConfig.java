package br.com.nsfatima.gestao.support.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuracao de ativacao do suporte a auditoria automatica do Spring Data JPA.
 *
 * <p>Registra o gerenciamento automatico dos campos anotados com {@code @CreatedDate},
 * {@code @LastModifiedDate}, {@code @CreatedBy} e {@code @LastModifiedBy} presentes
 * na superclasse {@link br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity}.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    /**
     * Provedor do auditor atual baseado no contexto de seguranca do Spring Security.
     *
     * @return instancia de {@link AuditorAware} resolvendo o usuario autenticado ou fallback "sistema"
     */
    @Bean
    public AuditorAware<String> auditorAware() {
        return new SpringSecurityAuditorAware();
    }
}

