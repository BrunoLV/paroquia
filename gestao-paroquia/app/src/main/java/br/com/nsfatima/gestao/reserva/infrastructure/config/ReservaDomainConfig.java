package br.com.nsfatima.gestao.reserva.infrastructure.config;

import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaAuthorizationService;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaConflitoService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra serviços do domínio puro no container de injeção de dependência do Spring,
 * preservando a integridade do isolamento de frameworks na camada de domínio.
 */
@Configuration
public class ReservaDomainConfig {

    @Bean
    public ReservaConflitoService reservaConflitoService(ReservaRepository reservaRepository) {
        return new ReservaConflitoService(reservaRepository);
    }

    @Bean
    public ReservaAuthorizationService reservaAuthorizationService() {
        return new ReservaAuthorizationService();
    }
}
