package br.com.nsfatima.gestao.iam.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class LimitadorTentativasTest {

    private final AtomicLong relogioNanos = new AtomicLong();
    private LimitadorTentativas limitador;

    @BeforeEach
    void setUp() {
        limitador = new LimitadorTentativas(3, Duration.ofMinutes(15), relogioNanos::get);
    }

    @Test
    @DisplayName("Deve bloquear a chave somente ao atingir o máximo de tentativas")
    void deveBloquearAoAtingirMaximo() {
        limitador.registrarTentativa("ip|maria");
        limitador.registrarTentativa("ip|maria");
        assertThat(limitador.bloqueado("ip|maria")).isFalse();

        limitador.registrarTentativa("ip|maria");

        assertThat(limitador.bloqueado("ip|maria")).isTrue();
    }

    @Test
    @DisplayName("Deve isolar o bloqueio por combinação de IP e usuário")
    void deveIsolarChaves() {
        for (int i = 0; i < 3; i++) {
            limitador.registrarTentativa(LimitadorTentativas.chave("10.0.0.1", "Maria"));
        }

        assertThat(limitador.bloqueado(LimitadorTentativas.chave("10.0.0.1", "maria "))).isTrue();
        assertThat(limitador.bloqueado(LimitadorTentativas.chave("10.0.0.2", "maria"))).isFalse();
        assertThat(limitador.bloqueado(LimitadorTentativas.chave("10.0.0.1", "joao"))).isFalse();
    }

    @Test
    @DisplayName("Deve liberar a chave após a janela expirar")
    void deveLiberarAposJanela() {
        for (int i = 0; i < 3; i++) {
            limitador.registrarTentativa("ip|maria");
        }
        relogioNanos.addAndGet(Duration.ofMinutes(16).toNanos());

        assertThat(limitador.bloqueado("ip|maria")).isFalse();
    }

    @Test
    @DisplayName("Deve zerar o contador ao limpar a chave após sucesso")
    void deveLimparChave() {
        for (int i = 0; i < 3; i++) {
            limitador.registrarTentativa("ip|maria");
        }

        limitador.limpar("ip|maria");

        assertThat(limitador.bloqueado("ip|maria")).isFalse();
    }
}
