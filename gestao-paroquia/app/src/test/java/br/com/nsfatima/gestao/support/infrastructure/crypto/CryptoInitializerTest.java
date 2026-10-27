package br.com.nsfatima.gestao.support.infrastructure.crypto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CryptoInitializerTest {

    @Test
    @DisplayName("Deve falhar o boot quando a chave de criptografia estiver ausente ou em branco")
    void deveRejeitarChaveAusente() {
        assertThatThrownBy(() -> new CryptoInitializer(null)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new CryptoInitializer("   ")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deve falhar o boot quando a chave de criptografia for curta demais")
    void deveRejeitarChaveCurta() {
        assertThatThrownBy(() -> new CryptoInitializer("curta"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("APP_CRYPTO_KEY");
    }
}
