package br.com.nsfatima.gestao.support.infrastructure.configuration;

import java.util.Optional;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Provedor do identificador do ator corrente para o mecanismo de auditoria do Spring Data JPA.
 *
 * <p>Recupera o {@code principal} autenticado do {@link SecurityContextHolder}. Quando a chamada
 * ocorrer em contexto anônimo, assíncrono ou sem autenticação formal, retorna {@code "sistema"}
 * para garantir consistência e rastreabilidade nos registros de banco de dados.
 */
public class SpringSecurityAuditorAware implements AuditorAware<String> {

    private static final String SISTEMA = "sistema";

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName)
                .filter(name -> !name.isBlank())
                .or(() -> Optional.of(SISTEMA));
    }
}
