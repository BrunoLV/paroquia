package br.com.nsfatima.gestao.iam.infrastructure.config;

import br.com.nsfatima.gestao.iam.infrastructure.security.LimitadorTentativas;
import com.github.benmanes.caffeine.cache.Ticker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Registra os limitadores de tentativas do IAM, um por superfície pública sensível: o login
 * (contado apenas em falhas de credencial) e o pedido de redefinição de senha (contado a cada
 * pedido, já que a resposta é sempre genérica). Limites e janelas são configuráveis porque o
 * equilíbrio entre proteção e usabilidade (ex.: secretaria compartilhando um IP) varia por paróquia.
 */
@Configuration
public class LimitadoresTentativasConfig {

    @Bean
    LimitadorTentativas limitadorLogin(
            @Value("${app.security.rate-limit.login.max-attempts:5}") int maxTentativas,
            @Value("${app.security.rate-limit.login.window-minutes:15}") long janelaMinutos) {
        return new LimitadorTentativas(maxTentativas, Duration.ofMinutes(janelaMinutos), Ticker.systemTicker());
    }

    @Bean
    LimitadorTentativas limitadorResetSenha(
            @Value("${app.security.rate-limit.reset-senha.max-attempts:3}") int maxTentativas,
            @Value("${app.security.rate-limit.reset-senha.window-minutes:15}") long janelaMinutos) {
        return new LimitadorTentativas(maxTentativas, Duration.ofMinutes(janelaMinutos), Ticker.systemTicker());
    }
}
