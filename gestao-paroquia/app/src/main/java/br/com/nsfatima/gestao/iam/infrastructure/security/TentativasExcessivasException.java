package br.com.nsfatima.gestao.iam.infrastructure.security;

import org.springframework.security.core.AuthenticationException;

import java.time.Duration;

/**
 * Sinaliza que a combinação IP + usuário excedeu o limite de tentativas de login. Estende
 * {@link AuthenticationException} para atravessar o fluxo de falha do filtro de login, que a traduz
 * em HTTP 429 (e não 401/503) com {@code Retry-After}.
 */
public class TentativasExcessivasException extends AuthenticationException {

    private final transient Duration aguardar;

    public TentativasExcessivasException(Duration aguardar) {
        super("Muitas tentativas. Tente novamente mais tarde.");
        this.aguardar = aguardar;
    }

    public Duration getAguardar() {
        return aguardar;
    }
}
